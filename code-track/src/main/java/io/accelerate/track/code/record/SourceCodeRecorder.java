package io.accelerate.track.code.record;

import org.eclipse.jgit.api.Git;
import org.slf4j.Logger;
import io.accelerate.track.code.content.SourceCodeProvider;
import io.accelerate.track.code.metrics.SourceCodeRecordingListener;
import io.accelerate.track.code.metrics.SourceCodeRecordingMetricsCollector;
import io.accelerate.track.code.snapshot.Snapshot;
import io.accelerate.track.code.snapshot.SnapshotRecorderException;
import io.accelerate.track.code.snapshot.file.Writer;
import io.accelerate.track.code.time.SystemMonotonicTimeSource;
import io.accelerate.track.code.time.TimeSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.file.StandardOpenOption.CREATE;
import static org.slf4j.LoggerFactory.getLogger;

public class SourceCodeRecorder {
    private static final Logger log = getLogger(SourceCodeRecorder.class);

    private final SourceCodeProvider sourceCodeProvider;
    private final Path outputRecordingFilePath;
    private final TimeSource timeSource;
    private final long snapshotIntervalMillis;
    private final long recordingStartTimestamp;
    private final int keySnapshotSpacing;
    private final AtomicBoolean shouldStopJob;
    private final SourceCodeRecordingListener sourceCodeRecordingListener;
    private Queue<String> tagQueue;

    /**
     * If this method fails, stop everything
     */
    public static void runSanityCheck() {
        try {
            Path gitDirectory = Files.createTempDirectory("sanity_check");
            Git.init().setDirectory(gitDirectory.toFile()).call();
        } catch (Exception e) {
            throw new IllegalStateException("Not able to run the \"git init\" on temporary folder.", e);
        }
    }

    private SourceCodeRecorder(SourceCodeProvider sourceCodeProvider,
                               Path outputRecordingFilePath,
                               TimeSource timeSource,
                               long recordedTimestamp,
                               long snapshotIntervalMillis,
                               int keySnapshotSpacing, SourceCodeRecordingListener sourceCodeRecordingListener) {
        this.sourceCodeProvider = sourceCodeProvider;
        this.outputRecordingFilePath = outputRecordingFilePath;
        this.timeSource = timeSource;
        this.recordingStartTimestamp = recordedTimestamp;
        this.snapshotIntervalMillis = snapshotIntervalMillis;
        this.keySnapshotSpacing = keySnapshotSpacing;
        this.sourceCodeRecordingListener = sourceCodeRecordingListener;
        shouldStopJob = new AtomicBoolean(false);
        tagQueue = new ConcurrentLinkedQueue<>();
    }

    @SuppressWarnings("SameParameterValue")
    public static class Builder {

        private final SourceCodeProvider bSourceCodeProvider;
        private final Path bOutputRecordingFilePath;
        private TimeSource bTimeSource;
        private long bSnapshotIntervalMillis;
        private long bRecordingStartTimestampSec;
        private int bKeySnapshotSpacing;
        private SourceCodeRecordingListener bSourceCodeRecordingListener;

        public Builder(SourceCodeProvider sourceCodeProvider, Path outputRecordingFilePath) {
            bSourceCodeProvider = sourceCodeProvider;
            bOutputRecordingFilePath = outputRecordingFilePath;
            bRecordingStartTimestampSec = System.currentTimeMillis() / 1000;
            bTimeSource = new SystemMonotonicTimeSource();
            bSnapshotIntervalMillis = TimeUnit.MINUTES.toMillis(5);
            bKeySnapshotSpacing = 5;
            bSourceCodeRecordingListener = new SourceCodeRecordingMetricsCollector();
        }

        public Builder withRecordingStartTimestampSec(long recordingStartTimestampSec) {
            this.bRecordingStartTimestampSec = recordingStartTimestampSec;
            return this;
        }

        public Builder withTimeSource(TimeSource timeSource) {
            this.bTimeSource = timeSource;
            return this;
        }

        public Builder withSnapshotEvery(int interval, TimeUnit timeUnit) {
            this.bSnapshotIntervalMillis = timeUnit.toMillis(interval);
            return this;
        }

        public Builder withKeySnapshotSpacing(int keySnapshotSpacing) {
            this.bKeySnapshotSpacing = keySnapshotSpacing;
            return this;
        }

        public Builder withRecordingListener(SourceCodeRecordingListener sourceCodeRecordingListener) {
            this.bSourceCodeRecordingListener = sourceCodeRecordingListener;
            return this;
        }

        public SourceCodeRecorder build() {
            return new SourceCodeRecorder(
                    bSourceCodeProvider,
                    bOutputRecordingFilePath,
                    bTimeSource,
                    bRecordingStartTimestampSec,
                    bSnapshotIntervalMillis,
                    bKeySnapshotSpacing,
                    bSourceCodeRecordingListener
            );
        }
    }

    public void start(Duration recordingDuration) throws SourceCodeRecorderException {
        Writer writer;
        Path lockFilePath = Paths.get(outputRecordingFilePath + ".lock");
        try {
            Files.write(lockFilePath, new byte[0], CREATE);
            //TODO Initialise inside constructor once SnapshotsFileWriter::new is free from exceptions
            writer = new Writer(
                    outputRecordingFilePath,
                    sourceCodeProvider,
                    timeSource,
                    recordingStartTimestamp,
                    keySnapshotSpacing,
                    false
            );
        } catch (IOException | SnapshotRecorderException e) {
            throw new SourceCodeRecorderException("Failed to open destination", e);
        }

        try {
            sourceCodeRecordingListener.notifyRecordingStart(outputRecordingFilePath);
            doRecord(writer, recordingDuration);
        } catch (SnapshotRecorderException e) {
            throw new SourceCodeRecorderException("Exception encountered while recording", e);
        } finally {
            sourceCodeRecordingListener.notifyRecordingEnd();
        }
    }

    public void tagCurrentState(String tag) {
        log.info("Tag state with: " + tag);
        tagQueue.offer(tag);
        timeSource.wakeUpNow();
    }

    private void doRecord(Writer writer, Duration recordingDuration) throws SnapshotRecorderException {
        while (timeSource.currentTimeNano() < recordingDuration.toNanos()) {
            long timestampBeforeProcessing = timeSource.currentTimeNano();
            sourceCodeRecordingListener.notifySnapshotStart(timestampBeforeProcessing, TimeUnit.NANOSECONDS);
            Snapshot snapshot = writer.takeSnapshot();
            writer.writeSnapshotWithTags(snapshot, getAllEnqueuedTags());
            sourceCodeRecordingListener.notifySnapshotEnd(timeSource.currentTimeNano(), TimeUnit.NANOSECONDS);

            // Allow a different thread to stop the recording
            // This operation should be before wakeUp to allow a final snapshot to be taken ( if the time allows it )
            if (shouldStopJob.get() && !hasTags()) {
                break;
            }

            // Prepare the next timestamp
            long timeToSleep = TimeUnit.MILLISECONDS.toNanos(snapshotIntervalMillis);
            long nextTimestamp = timestampBeforeProcessing + timeToSleep;
            try {
                timeSource.wakeUpAt(nextTimestamp, TimeUnit.NANOSECONDS);
            } catch (InterruptedException | BrokenBarrierException e) {
                log.debug("Interrupted while sleeping", e);
            }
        }
    }

    private List<String> getAllEnqueuedTags() {
        List<String> tags = new ArrayList<>();
        while(tagQueue.peek() != null) {
            tags.add(tagQueue.poll());
        }
        return tags;
    }

    private boolean hasTags() {
        return tagQueue.size() > 0;
    }

    public void stop() {
        if (!shouldStopJob.get()) {
            log.info("Stopping recording");
            shouldStopJob.set(true);
            timeSource.wakeUpNow();
        } else {
            log.info("Recording already stopping");
        }
    }

    public void close() {
        try {
            log.info("Closing the source code stream");
            //TODO Ensure the file is finalised correctly

            //delete lock file after closing writing
            Path lockFilePath = Paths.get(outputRecordingFilePath + ".lock");
            Files.delete(lockFilePath);
        } catch (IOException e) {
            throw new RuntimeException("Can't delete *.lock file.", e);
        }
    }
}

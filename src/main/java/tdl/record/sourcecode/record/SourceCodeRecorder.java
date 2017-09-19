package tdl.record.sourcecode.record;

import lombok.extern.slf4j.Slf4j;
import tdl.record.sourcecode.App;
import tdl.record.sourcecode.content.SourceCodeProvider;
import tdl.record.sourcecode.snapshot.file.SnapshotsFileWriter;
import tdl.record.sourcecode.time.SystemMonotonicTimeSource;
import tdl.record.sourcecode.time.TimeSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.file.StandardOpenOption.CREATE;

@Slf4j
public class SourceCodeRecorder {

    private final SourceCodeProvider sourceCodeProvider;
    private final Path outputRecordingFilePath;
    private final TimeSource timeSource;
    private final long snapshotIntervalMillis;
    private final long recordingStartTimestamp;
    private final int keySnapshotSpacing;
    private final AtomicBoolean shouldStopJob = new AtomicBoolean(false);

    SourceCodeRecorder(SourceCodeProvider sourceCodeProvider,
            Path outputRecordingFilePath,
            TimeSource timeSource,
            long recordedTimestamp,
            long snapshotIntervalMillis,
            int keySnapshotSpacing) {
        this.sourceCodeProvider = sourceCodeProvider;
        this.outputRecordingFilePath = outputRecordingFilePath;
        this.timeSource = timeSource;
        this.recordingStartTimestamp = recordedTimestamp;
        this.snapshotIntervalMillis = snapshotIntervalMillis;
        this.keySnapshotSpacing = keySnapshotSpacing;
    }

    @SuppressWarnings("SameParameterValue")
    public static class Builder {

        private SourceCodeProvider bSourceCodeProvider;
        private Path bOutputRecordingFilePath;
        private TimeSource bTimeSource;
        private long bSnapshotIntervalMillis;
        private long bRecordingStartTimestamp;
        private int bKeySnapshotSpacing;

        public Builder(SourceCodeProvider sourceCodeProvider, Path outputRecordingFilePath) {
            bSourceCodeProvider = sourceCodeProvider;
            bOutputRecordingFilePath = outputRecordingFilePath;
            bTimeSource = new SystemMonotonicTimeSource();
            bSnapshotIntervalMillis = TimeUnit.MINUTES.toMillis(5);
            bKeySnapshotSpacing = 5;
        }

        public Builder withRecordingStartTime(long recordedTimestamp) {
            this.bRecordingStartTimestamp = recordedTimestamp;
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

        public SourceCodeRecorder build() {
            return new SourceCodeRecorder(
                    bSourceCodeProvider,
                    bOutputRecordingFilePath,
                    bTimeSource,
                    bRecordingStartTimestamp,
                    bSnapshotIntervalMillis,
                    bKeySnapshotSpacing
            );
        }
    }

    public void start(Duration recordingDuration) throws SourceCodeRecorderException {
        SnapshotsFileWriter writer;
        Path lockFilePath = Paths.get(outputRecordingFilePath + ".lock");
        try {
            Files.write(lockFilePath, new byte[0], CREATE);
            //TODO Initialise inside constructor once SnapshotsFileWriter::new is free from exceptions
            writer = new SnapshotsFileWriter(
                    outputRecordingFilePath,
                    sourceCodeProvider,
                    timeSource,
                    recordingStartTimestamp,
                    keySnapshotSpacing,
                    true
            );
        } catch (IOException e) {
            throw new SourceCodeRecorderException("Failed to open destination", e);
        }

        doRecord(writer, recordingDuration);
    }

    private void doRecord(SnapshotsFileWriter writer, Duration recordingDuration) {
        double totalNumberOfFrames = recordingDuration.toMillis() / snapshotIntervalMillis;
        for (long frameIndex = 0; frameIndex < totalNumberOfFrames; frameIndex++) {
            long timestampBeforeProcessing = timeSource.currentTimeNano();

            Logger.getLogger(App.class.getName()).log(Level.INFO, "Snapshot");
            writer.takeSnapshot();

            long nextTimestamp = timestampBeforeProcessing + TimeUnit.MILLISECONDS.toNanos(snapshotIntervalMillis);
            try {
                timeSource.wakeUpAt(nextTimestamp, TimeUnit.NANOSECONDS);
            } catch (InterruptedException | BrokenBarrierException e) {
                log.debug("Interrupted while sleeping", e);
            }

            // Allow a different thread to stop the recording
            if (shouldStopJob.get()) {
                break;
            }
        }
    }

    public void stop() {
        log.info("Stopping recording");
        shouldStopJob.set(true);
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

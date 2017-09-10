package tdl.record.sourcecode;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import lombok.extern.slf4j.Slf4j;
import tdl.record.sourcecode.content.CopyFromDirectorySourceCodeProvider;
import tdl.record.sourcecode.snapshot.file.SnapshotsFileWriter;
import tdl.record.sourcecode.time.SystemTimeSource;
import tdl.record.sourcecode.time.TimeSource;

@Parameters
@Slf4j
class RecordCommand {

    @Parameter(names = "--dir", description = "The target sourceCodeProvider that you want to record")
    private String dirPath;

    @Parameter(names = "--out", description = "The destination file")
    private String outputPath;

    @Parameter(names = "--delay", description = "In seconds")
    private Integer delay = 300; //in seconds

    @Parameter(names = "--one-time", description = "Whether one time or continuous")
    private Boolean isOneTime = false;

    @Parameter(names = "--append", description = "Append existing output")
    private Boolean append = false;

    private SnapshotsFileWriter writer;
    private final AtomicBoolean shouldStopJob = new AtomicBoolean(false);


    void run() throws IOException, InterruptedException {
        CopyFromDirectorySourceCodeProvider sourceCodeProvider = new CopyFromDirectorySourceCodeProvider(Paths.get(dirPath));
        writer = new SnapshotsFileWriter(Paths.get(outputPath), sourceCodeProvider, 5, append);
        if (isOneTime) {
            runOneTime();
        } else {
            runContinuous();
        }
    }

    private void runOneTime() {
        writer.takeSnapshot();
    }

    private void runContinuous() throws InterruptedException {
        registerSigtermHandler();
        TimeSource timeSource = new SystemTimeSource();
        Integer timeBetweenFramesMillis = delay;

        //noinspection InfiniteLoopStatement
        while (true) {
            long timestampBeforeProcessing = timeSource.currentTimeNano();

            Logger.getLogger(App.class.getName()).log(Level.INFO, "Snapshot");
            writer.takeSnapshot();


            long nextTimestamp = timestampBeforeProcessing + TimeUnit.MILLISECONDS.toNanos((long) timeBetweenFramesMillis);
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

    private void registerSigtermHandler() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> writer.takeSnapshot()));
    }
}

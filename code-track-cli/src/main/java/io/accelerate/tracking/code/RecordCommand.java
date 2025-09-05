package io.accelerate.tracking.code;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import io.accelerate.tracking.code.content.CopyFromDirectorySourceCodeProvider;
import io.accelerate.tracking.code.record.SourceCodeRecorder;
import io.accelerate.tracking.code.time.SystemMonotonicTimeSource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

@Parameters(commandDescription = "Start a recording session")
class RecordCommand extends Command {

    @Parameter(names = {"-s", "--source"}, description = "The target sourceCodeProvider that you want to record")
    private String sourceCodePath;

    @Parameter(names = {"-o", "--output"}, description = "The destination file")
    private String outputPath;

    @Parameter(names = {"-y", "--delay"}, description = "The delay between two consecutive snapshots")
    private Integer delay = 5; //in seconds

    @Parameter(names = {"-ks", "--key-spacing"}, description = "The spacing between two key snapshots")
    private Integer keySnapshotSpacing = 5;

    @Parameter(names = {"-mxfs", "--max-allowed-file-size-mb"}, description = "Maximum allowed file size of source files to capture")
    private int maximumFileSizeLimitInMB = 1;

    private SourceCodeRecorder sourceCodeRecorder;
    private Scanner stdin;

    public void run() {
        try {
            doRun();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doRun() throws InterruptedException {
        CopyFromDirectorySourceCodeProvider sourceCodeProvider = new CopyFromDirectorySourceCodeProvider(Paths.get(sourceCodePath), maximumFileSizeLimitInMB);
        Path outputRecordingFilePath = Paths.get(outputPath);
        sourceCodeRecorder = new SourceCodeRecorder.Builder(sourceCodeProvider, outputRecordingFilePath)
                .withTimeSource(new SystemMonotonicTimeSource())
                .withSnapshotEvery(delay, TimeUnit.SECONDS)
                .withKeySnapshotSpacing(keySnapshotSpacing)
                .build();

        registerSigtermHandler();

        //Run the recorder on a different Thread
        Thread recordingThread = new Thread(() ->  {  try {
            sourceCodeRecorder.start(Duration.of(999, ChronoUnit.HOURS));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }});

        recordingThread.start();
        recordingThread.join();
    }

    private void registerSigtermHandler() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            sourceCodeRecorder.stop();
            sourceCodeRecorder.close();
        }));
    }

}

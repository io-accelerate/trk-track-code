package tdl.record.sourcecode;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import lombok.extern.slf4j.Slf4j;
import tdl.record.sourcecode.content.CopyFromDirectorySourceCodeProvider;
import tdl.record.sourcecode.record.SourceCodeRecorder;
import tdl.record.sourcecode.record.SourceCodeRecorderException;
import tdl.record.sourcecode.time.SystemMonotonicTimeSource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

@Parameters(commandDescription = "Start a recording session")
@Slf4j
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
    private int maximumFileSizeLimitInMB = 2;

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

        // Read commands from standard input
        System.out.println("Enter tag name to trigger a tagged snapshot or \"exit\" to stop recording");
        stdin = new Scanner(System.in);
        while(stdin.hasNextLine()) {
            String line = stdin.nextLine();

            if (line == null || line.startsWith("exit")) {
                sourceCodeRecorder.stop();
                break;
            }
            sourceCodeRecorder.tagCurrentState(line.trim());
        }

        //Wait for the recording to finish
        recordingThread.join();
    }

    private void registerSigtermHandler() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            sourceCodeRecorder.stop();
            stdin.close();
            sourceCodeRecorder.close();
        }));
    }
}

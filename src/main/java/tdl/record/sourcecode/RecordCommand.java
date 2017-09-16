package tdl.record.sourcecode;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import tdl.record.sourcecode.content.CopyFromDirectorySourceCodeProvider;
import tdl.record.sourcecode.record.SourceCodeRecorder;
import tdl.record.sourcecode.record.SourceCodeRecorderException;
import tdl.record.sourcecode.time.SystemMonotonicTimeSource;

@Parameters(commandDescription = "Start a recording session")
@Slf4j
class RecordCommand extends Command {

    @Parameter(names = {"-s", "--source"}, description = "The target sourceCodeProvider that you want to record")
    private String sourceCodePath;

    @Parameter(names = {"-o", "--output"}, description = "The destination file")
    private String outputPath;

    @Parameter(names = {"-d", "--duration"}, description = "Duration of the recording in seconds")
    private Integer duration = 60; //in seconds

    @Parameter(names = {"-y", "--delay"}, description = "The delay between two consecutive snapshots")
    private Integer delay = 5; //in seconds

    @Parameter(names = {"-ks", "--key-spacing"}, description = "The spacing between two key snapshots")
    private Integer keySnapshotSpacing = 1; // All snaps are key snapshots

    private SourceCodeRecorder sourceCodeRecorder;

    public void run() {
        try {
            CopyFromDirectorySourceCodeProvider sourceCodeProvider = new CopyFromDirectorySourceCodeProvider(Paths.get(sourceCodePath));
            Path outputRecordingFilePath = Paths.get(outputPath);
            sourceCodeRecorder = new SourceCodeRecorder.Builder(sourceCodeProvider, outputRecordingFilePath)
                    .withTimeSource(new SystemMonotonicTimeSource())
                    .withSnapshotEvery(delay, TimeUnit.SECONDS)
                    .withKeySnapshotSpacing(keySnapshotSpacing)
                    .build();

            registerSigtermHandler();
            sourceCodeRecorder.start(Duration.of(duration, ChronoUnit.SECONDS));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void registerSigtermHandler() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> sourceCodeRecorder.close()));
    }
}

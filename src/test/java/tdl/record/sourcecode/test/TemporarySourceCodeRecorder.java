package tdl.record.sourcecode.test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;
import support.content.MultiStepSourceCodeProvider;
import support.time.FakeTimeSource;
import tdl.record.sourcecode.content.SourceCodeProvider;
import tdl.record.sourcecode.record.SourceCodeRecorder;

public class TemporarySourceCodeRecorder extends ExternalResource {

    private final TemporaryFolder temp = new TemporaryFolder();

    private Path outputFilePath;

    private final List<SourceCodeProvider> history;

    public TemporarySourceCodeRecorder(List<SourceCodeProvider> history) {
        this.history = history;
    }

    public Path getOutputFilePath() {
        return outputFilePath;
    }

    @Override
    protected void before() throws Throwable {
        temp.create();
        outputFilePath = temp.newFile("output.bin").toPath();
        SourceCodeProvider provider = new MultiStepSourceCodeProvider(history);

        SourceCodeRecorder recorder;
        recorder = new SourceCodeRecorder.Builder(provider, outputFilePath)
                .withTimeSource(new FakeTimeSource())
                .withSnapshotEvery(1, TimeUnit.SECONDS)
                .withKeySnapshotSpacing(3)
                .build();
        recorder.start(Duration.of(history.size(), ChronoUnit.SECONDS));
        recorder.close();
    }

    @Override
    protected void after() {
        FileUtils.deleteQuietly(outputFilePath.toFile());
        temp.delete();
    }

    public static void writeFile(Path destinationFolder, String childFile, String content) {
        try {
            File newFile1 = destinationFolder.resolve(childFile).toFile();
            FileUtils.writeStringToFile(newFile1, content, StandardCharsets.US_ASCII);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}

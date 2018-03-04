package support;

import org.apache.commons.io.FileUtils;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;
import support.content.MultiStepSourceCodeProvider;
import support.time.FakeTimeSource;
import tdl.record.sourcecode.content.SourceCodeProvider;
import tdl.record.sourcecode.record.SourceCodeRecorder;

import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TestGeneratedSrcsFile extends ExternalResource {

    private final TemporaryFolder temp = new TemporaryFolder();

    private Path outputFilePath;

    private final List<SourceCodeProvider> history;

    private final List<String> tags;


    public TestGeneratedSrcsFile(List<SourceCodeProvider> history) {
        this(history, new ArrayList<>());
    }

    public TestGeneratedSrcsFile(List<SourceCodeProvider> history, List<String> tags) {
        this.history = history;
        this.tags = tags;
    }


    public Path getFilePath() {
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

        for (String tag : tags) {
            recorder.tagCurrentState(tag);
        }

        recorder.start(Duration.of(history.size(), ChronoUnit.SECONDS));
        recorder.close();
    }

    @Override
    protected void after() {
        FileUtils.deleteQuietly(outputFilePath.toFile());
        temp.delete();
    }

}

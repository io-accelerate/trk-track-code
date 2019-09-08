package support;

import org.apache.commons.io.FileUtils;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;
import support.recording.TestRecordingFrame;
import support.recording.TestSourceCodeRecorder;

import java.nio.file.Path;
import java.util.List;

public class TestGeneratedSrcsFile extends ExternalResource {

    private final TemporaryFolder temp = new TemporaryFolder();
    private final List<TestRecordingFrame> recordingFrames;

    private Path outputFilePath;

    public TestGeneratedSrcsFile(List<TestRecordingFrame> recordingFrames) {
        this.recordingFrames = recordingFrames;
    }


    public Path getFilePath() {
        return outputFilePath;
    }

    @Override
    protected void before() throws Throwable {
        temp.create();
        outputFilePath = temp.newFile("output.bin").toPath();

        TestSourceCodeRecorder recorder =
                new TestSourceCodeRecorder(outputFilePath, recordingFrames);

        recorder.start();
        recorder.close();
    }

    @Override
    protected void after() {
        FileUtils.deleteQuietly(outputFilePath.toFile());
        temp.delete();
    }

}

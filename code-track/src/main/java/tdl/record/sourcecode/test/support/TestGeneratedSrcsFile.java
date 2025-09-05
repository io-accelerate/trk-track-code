package tdl.record.sourcecode.test.support;

import org.apache.commons.io.FileUtils;
import tdl.record.sourcecode.test.support.recording.TestRecordingFrame;
import tdl.record.sourcecode.test.support.recording.TestSourceCodeRecorder;
import tdl.record.sourcecode.record.SourceCodeRecorderException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TestGeneratedSrcsFile {

    private final List<TestRecordingFrame> recordingFrames;

    private Path outputFilePath;

    public Path temp;

    public TestGeneratedSrcsFile(List<TestRecordingFrame> recordingFrames) throws IOException {
        this.recordingFrames = recordingFrames;
        this.temp = Files.createTempDirectory("TestGeneratedSrcsFile_");
    }

    public Path getFilePath() {
        return outputFilePath;
    }

    public void beforeEach() throws SourceCodeRecorderException {
        outputFilePath = temp.resolve("output.bin");

        TestSourceCodeRecorder recorder =
                new TestSourceCodeRecorder(outputFilePath, recordingFrames);

        recorder.start();
        recorder.close();
    }

    public void afterEach() throws IOException {
        FileUtils.deleteQuietly(outputFilePath.toFile());
    }
}

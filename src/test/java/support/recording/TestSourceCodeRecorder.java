package support.recording;

import support.content.ChangeableWrappedSourceCodeProvider;
import support.content.EmptySourceCodeProvider;
import support.time.FakeTimeSource;
import tdl.record.sourcecode.record.SourceCodeRecorder;
import tdl.record.sourcecode.record.SourceCodeRecorderException;

import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TestSourceCodeRecorder {
    private int currentFrameIndex;
    private final List<TestRecordingFrame> recordingFrames;
    private final SourceCodeRecorder sourceCodeRecorder;
    private final ChangeableWrappedSourceCodeProvider changeableWrappedSourceCodeProvider;

    public TestSourceCodeRecorder(Path outputFilePath, List<TestRecordingFrame> recordingFrames) {
        this.recordingFrames = recordingFrames;

        FakeTimeSource fakeTimeSource = new FakeTimeSource();

        changeableWrappedSourceCodeProvider = new ChangeableWrappedSourceCodeProvider();
        sourceCodeRecorder = new SourceCodeRecorder
                .Builder(changeableWrappedSourceCodeProvider, outputFilePath)
                .withTimeSource(fakeTimeSource)
                .withSnapshotEvery(1, TimeUnit.SECONDS)
                .withKeySnapshotSpacing(3)
                .build();

        seedRecorderWithFrame(getAndAdvance(recordingFrames));
        fakeTimeSource.addWakeUpAtListener(() -> seedRecorderWithFrame(getAndAdvance(recordingFrames)));
    }

    private TestRecordingFrame getAndAdvance(List<TestRecordingFrame> recordingFrames) {
        if (currentFrameIndex < recordingFrames.size()) {
            TestRecordingFrame testRecordingFrame = recordingFrames.get(currentFrameIndex);
            currentFrameIndex += 1;
            return testRecordingFrame;
        } else {
            return new TestRecordingFrame(new EmptySourceCodeProvider());
        }
    }

    private void seedRecorderWithFrame(TestRecordingFrame currentFrame) {
        changeableWrappedSourceCodeProvider.setSourceCodeProvider(currentFrame.getSourceCodeProvider());
        for (String tag : currentFrame.getTags()) {
            sourceCodeRecorder.tagCurrentState(tag);
        }
    }

    public void start() throws SourceCodeRecorderException {
        sourceCodeRecorder.start(Duration.of(recordingFrames.size(), ChronoUnit.SECONDS));
    }

    public void close() {
        sourceCodeRecorder.close();
        currentFrameIndex = 0;
    }
}

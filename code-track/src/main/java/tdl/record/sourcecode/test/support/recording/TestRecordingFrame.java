package tdl.record.sourcecode.test.support.recording;

import tdl.record.sourcecode.content.SourceCodeProvider;

import java.util.Collections;
import java.util.List;

public class TestRecordingFrame {
    private final List<String> tags;
    private final SourceCodeProvider sourceCodeProvider;

    public TestRecordingFrame(SourceCodeProvider sourceCodeProvider) {
        this(Collections.emptyList(), sourceCodeProvider);
    }

    public TestRecordingFrame(List<String> tags, SourceCodeProvider sourceCodeProvider) {
        this.tags = tags;
        this.sourceCodeProvider = sourceCodeProvider;
    }

    public static TestRecordingFrame asFrame(SourceCodeProvider sourceCodeProvider) {
        return new TestRecordingFrame(sourceCodeProvider);
    }

    public static TestRecordingFrame asFrame(List<String> tags, SourceCodeProvider sourceCodeProvider) {
        return new TestRecordingFrame(tags, sourceCodeProvider);
    }

    List<String> getTags() {
        return tags;
    }

    SourceCodeProvider getSourceCodeProvider() {
        return sourceCodeProvider;
    }
}

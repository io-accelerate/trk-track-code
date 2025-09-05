package tdl.record.sourcecode.test.support.content;

import tdl.record.sourcecode.content.SourceCodeProvider;
import tdl.record.sourcecode.snapshot.SnapshotTypeHint;

import java.io.IOException;
import java.nio.file.Path;

public class ChangeableWrappedSourceCodeProvider implements SourceCodeProvider {
    private SourceCodeProvider sourceCodeProvider;

    public ChangeableWrappedSourceCodeProvider() {
        this.sourceCodeProvider = new EmptySourceCodeProvider();
    }

    public void setSourceCodeProvider(SourceCodeProvider sourceCodeProvider) {
        this.sourceCodeProvider = sourceCodeProvider;
    }

    @Override
    public SnapshotTypeHint retrieveAndSaveTo(Path destinationFolder) throws IOException {
        return sourceCodeProvider.retrieveAndSaveTo(destinationFolder);
    }
}

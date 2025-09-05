package tdl.record.sourcecode.test.support.content;

import tdl.record.sourcecode.content.SourceCodeProvider;
import tdl.record.sourcecode.snapshot.SnapshotTypeHint;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class MultiStepSourceCodeProvider implements SourceCodeProvider {
    private List<SourceCodeProvider> sourceCodeProviders;
    private int index;

    public MultiStepSourceCodeProvider(List<SourceCodeProvider> sourceCodeProviders) {
        this.sourceCodeProviders = sourceCodeProviders;
        index = 0;
    }

    @Override
    public SnapshotTypeHint retrieveAndSaveTo(Path destinationFolder) throws IOException {
        SnapshotTypeHint snapshotTypeHint = SnapshotTypeHint.ANY;
        if (index < sourceCodeProviders.size()) {
            snapshotTypeHint = sourceCodeProviders.get(index).retrieveAndSaveTo(destinationFolder);
            index++;
        }
        return snapshotTypeHint;
    }
}

package support.content;

import tdl.record.sourcecode.content.SourceCodeProvider;
import tdl.record.sourcecode.snapshot.SnapshotTypeHint;

import java.nio.file.Path;

public class EmptySourceCodeProvider implements SourceCodeProvider {

    @Override
    public SnapshotTypeHint retrieveAndSaveTo(Path destinationFolder)  {
        return SnapshotTypeHint.ANY;
    }
}

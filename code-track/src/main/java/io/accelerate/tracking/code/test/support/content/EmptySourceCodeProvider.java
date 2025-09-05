package io.accelerate.tracking.code.test.support.content;

import io.accelerate.tracking.code.content.SourceCodeProvider;
import io.accelerate.tracking.code.snapshot.SnapshotTypeHint;

import java.nio.file.Path;

public class EmptySourceCodeProvider implements SourceCodeProvider {

    @Override
    public SnapshotTypeHint retrieveAndSaveTo(Path destinationFolder)  {
        return SnapshotTypeHint.ANY;
    }
}

package io.accelerate.track.code.test.support.content;

import io.accelerate.track.code.content.SourceCodeProvider;
import io.accelerate.track.code.snapshot.SnapshotTypeHint;

import java.nio.file.Path;

public class EmptySourceCodeProvider implements SourceCodeProvider {

    @Override
    public SnapshotTypeHint retrieveAndSaveTo(Path destinationFolder)  {
        return SnapshotTypeHint.ANY;
    }
}

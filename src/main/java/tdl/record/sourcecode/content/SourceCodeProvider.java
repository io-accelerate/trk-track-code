package tdl.record.sourcecode.content;

import tdl.record.sourcecode.snapshot.SnapshotTypeHint;

import java.io.IOException;
import java.nio.file.Path;

@FunctionalInterface
public interface SourceCodeProvider {

    /**
     * Retrive sourceode and save it to the provided destination folder.
     *
     * Based on the frequency and size of the transfer, a provider might be able to offer hints
     * in regards to how this source files should be stored.
     *
     * The provider should return SnapshotTypeHint.ANY if not hints are provided.
     *
     * @param destinationFolder where to store the files
     * @return a hint in regards to the storage type
     * @throws IOException this provider is usually interacting with the disk
     */
    SnapshotTypeHint retrieveAndSaveTo(Path destinationFolder) throws IOException;
}

package net.petrabarus.java.record_dir_and_upload.snapshot.git;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import net.petrabarus.java.record_dir_and_upload.snapshot.DirectorySnapshot;
import org.eclipse.jgit.api.Git;

public class KeySnapshot extends Snapshot {

    private byte[] data;

    private final Path directory;

    public KeySnapshot(Git git, Path directory) throws IOException {
        super(git);
        this.directory = directory;
        takeSnapshot();
    }

    private void takeSnapshot() throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream();
                DirectorySnapshot snapshot = new DirectorySnapshot(directory, os);) {
            snapshot.compress();
            data = os.toByteArray();
        }
    }

    @Override
    public byte[] asBytes() {
        return data;
    }

}

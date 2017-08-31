package net.petrabarus.java.record_dir_and_upload.snapshot.naive;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import net.petrabarus.java.record_dir_and_upload.snapshot.DirectorySnapshot;

public class KeySnapshot extends Snapshot {

    public static KeySnapshot takeFromRecorder(SnapshotRecorder recorder) throws IOException {
        KeySnapshot snapshot = new KeySnapshot();
        snapshot.takeSnapshot(recorder.currentDirectorySnapshot);
        return snapshot;
    }

    private void takeSnapshot(Path directory) throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream();
                DirectorySnapshot snapshot = new DirectorySnapshot(directory, os);) {
            snapshot.compress();
            data = os.toByteArray();
        }
    }
}

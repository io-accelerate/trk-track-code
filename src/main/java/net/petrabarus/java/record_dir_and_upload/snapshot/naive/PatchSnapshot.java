package net.petrabarus.java.record_dir_and_upload.snapshot.naive;

import difflib.Patch;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.util.Map;
import net.petrabarus.java.record_dir_and_upload.diff.DirectoryDiffUtils;

public class PatchSnapshot extends Snapshot {

    public static PatchSnapshot takeSnapshotFromDirectories(Path previous, Path current) throws IOException {
        PatchSnapshot snapshot = new PatchSnapshot();
        Map<String, Patch> patches = DirectoryDiffUtils.diffDirectories(previous, current);
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(patches);
            snapshot.data = bos.toByteArray();
        }
        return snapshot;
    }

    public static PatchSnapshot createSnapshotFromBytes(byte[] data) {
        return null;
    }

    public void restoreSnapshot(Path destinationDirectory) {

    }
}

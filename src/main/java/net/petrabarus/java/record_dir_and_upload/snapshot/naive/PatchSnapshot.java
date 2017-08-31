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

    public static PatchSnapshot takeFromRecorder(SnapshotRecorder recorder) throws IOException {
        PatchSnapshot snapshot = new PatchSnapshot();
        snapshot.takeSnapshot(recorder.previousDirectorySnapshot, recorder.currentDirectorySnapshot);
        return snapshot;
    }

    private void takeSnapshot(Path previous, Path current) throws IOException {
        Map<String, Patch> patches = DirectoryDiffUtils.diffDirectories(previous, current);
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(patches);
            data = bos.toByteArray();
        }
    }
}

package net.petrabarus.java.record_dir_and_upload.snapshot.naive;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;

public class SnapshotRecorderTest {

    @Test
    public void takeSnapshot() {
        Path directory = Paths.get("./src/test/resources/diff/test1/dir1/");
        SnapshotRecorder recorder = new SnapshotRecorder(directory);
    }
}

package net.petrabarus.java.record_dir_and_upload.snapshot.file;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;

public class SnapshotsFileWriterTest {

    @Test
    public void run() throws IOException {
        Path output = Paths.get("tmp/snapshot.bin");
        Path dirPath = Paths.get("src/test/resources/directory_snapshot/dir1");
        try (SnapshotsFileWriter writer = new SnapshotsFileWriter(output, dirPath, false)) {
            writer.takeSnapshot();
            writer.takeSnapshot();
            writer.takeSnapshot();
        }
    }
}

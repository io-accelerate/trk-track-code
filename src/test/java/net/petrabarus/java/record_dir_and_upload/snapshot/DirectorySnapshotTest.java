package net.petrabarus.java.record_dir_and_upload.snapshot;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Test;

public class DirectorySnapshotTest {

    @Test
    public void run() throws IOException {
        Path dirPath = Paths.get("src/test/resources/directory_snapshot/dir1");
        Path outputPath = Paths.get("tmp/test.zip");
        try (FileOutputStream out = new FileOutputStream(outputPath.toFile());
                DirectorySnapshot snapshot = new DirectorySnapshot(dirPath, out)) {
            snapshot.compress();
        }
        Assert.assertEquals(1091, outputPath.toFile().length());
    }
}

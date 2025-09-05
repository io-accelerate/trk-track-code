package io.accelerate.track.code.snapshot.helpers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DirectoryZipTest {

    @TempDir
    Path destinationFolder;

    @Test
    public void run() throws IOException {
        Path dirPath = Paths.get("src/test/resources/directory_snapshot/dir1");
        Path outputPath = destinationFolder.resolve("test.zip");
        try (FileOutputStream out = new FileOutputStream(outputPath.toFile());
             DirectoryZip snapshot = new DirectoryZip(dirPath, out)) {
            snapshot.compress();
        }
        Assertions.assertEquals(1091, outputPath.toFile().length());
    }
}

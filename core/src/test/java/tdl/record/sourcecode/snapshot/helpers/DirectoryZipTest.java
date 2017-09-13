package tdl.record.sourcecode.snapshot.helpers;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class DirectoryZipTest {

    @Rule
    public TemporaryFolder destinationFolder = new TemporaryFolder();


    @Test
    public void run() throws IOException {
        Path dirPath = Paths.get("src/test/resources/directory_snapshot/dir1");
        Path outputPath = destinationFolder.newFile("test.zip").toPath();
        try (FileOutputStream out = new FileOutputStream(outputPath.toFile());
                DirectoryZip snapshot = new DirectoryZip(dirPath, out)) {
            snapshot.compress();
        }
        Assert.assertEquals(1091, outputPath.toFile().length());
    }
}

package tdl.record.sourcecode.snapshot;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import tdl.record.sourcecode.test.FileTestHelper;
import org.apache.commons.io.FileUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class PatchSnapshotTest {

    @Rule
    public TemporaryFolder temporary = new TemporaryFolder();

    @Test
    public void takeAndRestoreSnapshot() throws IOException, ClassNotFoundException {
        Path originalContent = Paths.get("./src/test/resources/diff/test1/dir1/");

        File originalCopy = temporary.newFolder();
        FileUtils.copyDirectory(originalContent.toFile(), originalCopy);
        File revisedCopy = temporary.newFolder();
        FileUtils.copyDirectory(originalContent.toFile(), revisedCopy);

        FileUtils.writeStringToFile(
                new File(revisedCopy, "file1.txt"),
                "Lorem Ipsum Dolor Sit Amet!",
                StandardCharsets.US_ASCII,
                true
        );
        PatchSnapshot snapshot = PatchSnapshot.takeSnapshotFromDirectories(originalCopy.toPath(), revisedCopy.toPath());
        snapshot.restoreSnapshot(originalCopy.toPath());
        assertTrue(FileTestHelper.isDirectoryEquals(originalCopy.toPath(), revisedCopy.toPath()));
    }

    @Test
    public void compressAndDecompress() throws IOException {
        String string = "Lorem ipsum dolor sit amet, consectetur adipiscing "
                + "elit. Nam viverra erat a quam ultrices, vel dapibus augue "
                + "hendrerit. Aliquam id suscipit enim. Morbi dui mi, "
                + "sodales ac erat nec, pretium eleifend metus. Aliquam "
                + "sodales consequat felis, sit amet dictum sem vehicula sed. "
                + "Vivamus nec leo eget lectus dignissim porttitor non nec "
                + "odio. Pellentesque blandit magna eu diam imperdiet luctus."
                + " Nam non tempus nibh. Aliquam rutrum elementum faucibus. "
                + "Proin consequat erat a magna malesuada hendrerit ac sit amet "
                + "sem. Donec elementum porttitor quam, et efficitur leo "
                + "varius non. ";
        byte[] compressed = PatchSnapshot.compress(string);
        assertTrue(compressed.length < string.length());
        String decompressed = PatchSnapshot.decompress(compressed);
        assertEquals(decompressed, string);
    }

}

package tdl.record.sourcecode.snapshot;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.codec.binary.Hex;
import tdl.record.sourcecode.test.FileTestHelper;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import tdl.record.sourcecode.test.GitTestHelper;

public class PatchSnapshotTest {

    @Rule
    public TemporaryFolder temporary = new TemporaryFolder();

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
        byte[] compressed = PatchSnapshot.compress(string.getBytes());
        assertTrue(compressed.length < string.length());
        byte[] decompressed = PatchSnapshot.decompress(compressed);
        assertEquals(new String(decompressed), string);
    }

    @Test
    public void takeAndRestoreSnapshotFromGit() throws GitAPIException, IOException, Exception {
        File directory1 = temporary.newFolder();
        File directory2 = temporary.newFolder();

        Git git1 = Git.init().setDirectory(directory1).call();
        GitTestHelper.addAndCommit(git1);

        Git git2 = Git.init().setDirectory(directory2).call();
        GitTestHelper.addAndCommit(git2);

        FileTestHelper.appendStringToFile(directory1.toPath(), "file1.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory1.toPath(), "file2.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory1.toPath(), "file3.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory2.toPath(), "file1.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory2.toPath(), "file2.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory2.toPath(), "file3.txt", "Test\n");

        GitTestHelper.addAndCommit(git1);
        GitTestHelper.addAndCommit(git2);

        FileTestHelper.appendStringToFile(directory1.toPath(), "file1.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory1.toPath(), "file2.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory1.toPath(), "file3.txt", "Test\n");

        GitTestHelper.addAndCommit(git1);

        PatchSnapshot snapshot = PatchSnapshot.takeSnapshotFromGit(git1);
        snapshot.restoreSnapshot(git2);

        FileFilter filter = (file) -> {
            return !file.getAbsolutePath().contains(".git/");
        };
        assertTrue(FileTestHelper.isDirectoryEquals(directory1.toPath(), directory2.toPath(), filter));
    }

}

package tdl.record.sourcecode.snapshot;

import java.io.File;
import java.io.IOException;

import tdl.record.sourcecode.snapshot.helpers.GitHelper;
import tdl.record.sourcecode.test.FileTestHelper;
import org.eclipse.jgit.api.Git;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

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
    public void takeAndRestoreSnapshotFromGit() throws Exception {
        File directory1 = temporary.newFolder();
        File directory2 = temporary.newFolder();

        Git git1 = Git.init().setDirectory(directory1).call();
        GitHelper.addAndCommit(git1);

        Git git2 = Git.init().setDirectory(directory2).call();
        GitHelper.addAndCommit(git2);

        FileTestHelper.appendStringToFile(directory1.toPath(), "file1.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory1.toPath(), "file2.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory1.toPath(), "file3.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory2.toPath(), "file1.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory2.toPath(), "file2.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory2.toPath(), "file3.txt", "Test\n");

        GitHelper.addAndCommit(git1);
        GitHelper.addAndCommit(git2);

        FileTestHelper.appendStringToFile(directory1.toPath(), "file1.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory1.toPath(), "file2.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory1.toPath(), "file3.txt", "Test\n");

        GitHelper.addAndCommit(git1);

        PatchSnapshot snapshot = PatchSnapshot.takeSnapshotFromGit(git1);
        snapshot.restoreSnapshot(git2);

        assertTrue(FileTestHelper.isDirectoryEqualsWithoutGit(directory1.toPath(), directory2.toPath()));
    }

}

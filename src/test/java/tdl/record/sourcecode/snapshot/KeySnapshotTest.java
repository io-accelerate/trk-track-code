package tdl.record.sourcecode.snapshot;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.jgit.api.Git;

import tdl.record.sourcecode.test.FileTestHelper;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import tdl.record.sourcecode.test.GitTestHelper;

public class KeySnapshotTest {

    @Rule
    public TemporaryFolder temporary = new TemporaryFolder();

    @Test
    public void takeAndRestoreSnapshot() throws Exception {
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

        assertTrue(FileTestHelper.isDirectoryEqualsWithoutGit(directory1.toPath(), directory2.toPath()));
    }
}

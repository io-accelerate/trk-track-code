package tdl.record.sourcecode.snapshot;

import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tdl.record.sourcecode.snapshot.helpers.GitHelper;
import tdl.record.sourcecode.test.FileTestHelper;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class KeySnapshotTest {

    @TempDir
    Path temporary;

    @Test
    public void takeAndRestoreSnapshot() throws Exception {
        File directory1 = temporary.resolve("directory1").toFile();
        File directory2 = temporary.resolve("directory2").toFile();

        Git git1 = Git.init().setDirectory(directory1).call();
        GitHelper.addAndCommit(git1);

        Git git2 = Git.init().setDirectory(directory2).call();
        GitHelper.addAndCommit(git2);

        FileTestHelper.appendStringToFile(directory1.toPath(), "file1.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory1.toPath(), "file2.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory1.toPath(), "subdir1/file3.txt", "Test\n");
        
        FileTestHelper.appendStringToFile(directory2.toPath(), "file1.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory2.toPath(), "file2.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory2.toPath(), "subdir1/file3.txt", "Test\n");

        GitHelper.addAndCommit(git1);
        GitHelper.addAndCommit(git2);

        FileTestHelper.appendStringToFile(directory1.toPath(), "file1.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory1.toPath(), "file2.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory1.toPath(), "subdir1/file3.txt", "Test\n");

        GitHelper.addAndCommit(git1);

        KeySnapshot snapshot = KeySnapshot.takeSnapshotFromGit(git1);
        snapshot.restoreSnapshot(git2);

        assertTrue(FileTestHelper.isDirectoryEqualsWithoutGit(directory1.toPath(), directory2.toPath()));
    }
}

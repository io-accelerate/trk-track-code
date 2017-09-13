package tdl.record.sourcecode.snapshot;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import tdl.record.sourcecode.content.CopyFromDirectorySourceCodeProvider;
import tdl.record.sourcecode.test.FileTestHelper;

public class SnapshotRecorderTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void takeSnapshot() throws IOException {
        Path directory = Paths.get("./src/test/resources/diff/test1/dir1/");
        Path tmpDir = folder.getRoot().toPath();
        FileUtils.copyDirectory(directory.toFile(), tmpDir.toFile());
        SnapshotRecorder recorder = new SnapshotRecorder(new CopyFromDirectorySourceCodeProvider(tmpDir), 5);

        Snapshot snapshot1 = recorder.takeSnapshot();
        assertTrue(snapshot1 instanceof KeySnapshot);
        printSnapshot(snapshot1);

        FileTestHelper.appendStringToFile(tmpDir, "file1.txt", "\ndata1");
        Snapshot snapshot2 = recorder.takeSnapshot();
        assertTrue(snapshot2 instanceof PatchSnapshot);
        printSnapshot(snapshot2);

        FileTestHelper.appendStringToFile(tmpDir, "file2.txt", "\nLOREM");
        Snapshot snapshot3 = recorder.takeSnapshot();
        assertTrue(snapshot3 instanceof PatchSnapshot);
        printSnapshot(snapshot3);

        FileTestHelper.appendStringToFile(tmpDir, "subdir1/file1.txt", "\nIPSUM");
        Snapshot snapshot4 = recorder.takeSnapshot();
        assertTrue(snapshot4 instanceof PatchSnapshot);
        printSnapshot(snapshot4);

        FileTestHelper.appendStringToFile(tmpDir, "subdir1/file1.txt", "SIT");
        Snapshot snapshot5 = recorder.takeSnapshot();
        assertTrue(snapshot5 instanceof PatchSnapshot);
        printSnapshot(snapshot5);

        FileTestHelper.appendStringToFile(tmpDir, "subdir1/file1.txt", "AMENT");
        Snapshot snapshot6 = recorder.takeSnapshot();
        assertTrue(snapshot6 instanceof KeySnapshot);
        printSnapshot(snapshot6);
    }

    private static void printSnapshot(Snapshot snapshot) {
        //System.out.println(new String(snapshot.getData()));
        //do nothing
    }

    private SnapshotRecorder createDefaultRecorder(Path tmpDir) {
        return new SnapshotRecorder(new CopyFromDirectorySourceCodeProvider(tmpDir), 5);
    }

    @Test
    public void constructShouldCreateGitDirectory() {
        Path tmpDir = folder.getRoot().toPath();
        try (SnapshotRecorder recorder = createDefaultRecorder(tmpDir)) {
            Path gitDir = recorder.getGitDirectory();
            assertTrue(gitDir.toFile().exists());
            assertTrue(gitDir.resolve(".git").toFile().exists());
        }
    }

    @Test
    public void syncToGitDirectoryShouldCopyDirectory() throws IOException {
        Path tmpDir = folder.getRoot().toPath();
        try (SnapshotRecorder recorder = createDefaultRecorder(tmpDir)) {
            Path gitDir = recorder.getGitDirectory();

            assertFalse(gitDir.resolve("file1.txt").toFile().exists());
            assertFalse(gitDir.resolve("file2.txt").toFile().exists());
            FileTestHelper.appendStringToFile(tmpDir, "file1.txt", "Hello World!");
            FileTestHelper.appendStringToFile(tmpDir, "file2.txt", "Lorem Ipsum!");

            recorder.syncToGitDirectory();
            assertTrue(gitDir.resolve("file1.txt").toFile().exists());
            assertTrue(gitDir.resolve("file2.txt").toFile().exists());

            assertFalse(gitDir.resolve("subdir/file1.txt").toFile().exists());
            FileTestHelper.appendStringToFile(tmpDir, "subdir/file1.txt", "Hello World!");

            recorder.syncToGitDirectory();
            assertTrue(gitDir.resolve("subdir").toFile().isDirectory());
            assertTrue(gitDir.resolve("subdir/file1.txt").toFile().exists());

            Files.delete(tmpDir.resolve("file1.txt"));
            recorder.syncToGitDirectory();
            assertFalse(gitDir.resolve("file1.txt").toFile().exists());

            Files.delete(tmpDir.resolve("subdir/file1.txt"));
            recorder.syncToGitDirectory();
            assertFalse(gitDir.resolve("subdir/file1.txt").toFile().exists());
        }
    }

    @Test
    public void syncToGitDirectoryShouldCopyDirectoryWithDotGitignore() throws IOException {
        Path tmpDir = folder.getRoot().toPath();
        try (SnapshotRecorder recorder = createDefaultRecorder(tmpDir)) {
            Path gitDir = recorder.getGitDirectory();

            assertFalse(gitDir.resolve(".gitignore").toFile().exists());
            FileTestHelper.appendStringToFile(tmpDir, ".gitignore", "*.orig");

            recorder.syncToGitDirectory();
            assertTrue(gitDir.resolve(".gitignore").toFile().exists());
        }
    }

    @Test
    public void syncToGitDirectoryShouldCopyDirectoryWithoutDotGitDirectory() throws IOException, GitAPIException {
        Path tmpDir = folder.getRoot().toPath();
        try (SnapshotRecorder recorder = createDefaultRecorder(tmpDir)) {
            Path gitDir = recorder.getGitDirectory();

            assertFalse(tmpDir.resolve(".git").toFile().exists());
            Git.init().setDirectory(tmpDir.toFile()).call();
            FileTestHelper.appendStringToFile(tmpDir, ".git/randomfile", "TEST");
            assertTrue(tmpDir.resolve(".git").toFile().exists());

            recorder.syncToGitDirectory();
            assertTrue(gitDir.resolve(".git").toFile().exists());
            assertFalse(gitDir.resolve(".git/randomfile").toFile().exists());
        }
    }

    @Test
    public void commitAllChanges() throws Exception {
        Path tmpDir = folder.getRoot().toPath();
        try (SnapshotRecorder recorder = createDefaultRecorder(tmpDir)) {
            Git git = recorder.getGit();

            assertEquals(1, getCommitCount(git));
            FileTestHelper.appendStringToFile(tmpDir, "file1.txt", "Hello World!");
            FileTestHelper.appendStringToFile(tmpDir, "file2.txt", "Lorem Ipsum!");

            recorder.syncToGitDirectory();
            recorder.commitAllChanges();
            assertEquals(2, getCommitCount(git));

            recorder.commitAllChanges();
            assertEquals(3, getCommitCount(git));
        }
    }

    public static int getCommitCount(Git git) throws GitAPIException {
        Iterable<RevCommit> commits = git.log().call();
        int count = 0;
        Iterator it = commits.iterator();
        while (it.hasNext()) {
            it.next();
            count++;
        }
        return count;
    }
}

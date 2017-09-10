package tdl.record.sourcecode.snapshot.helpers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import tdl.record.sourcecode.snapshot.SnapshotRecorderTest;
import tdl.record.sourcecode.test.FileTestHelper;

public class GitHelperTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void isGitDirectoryShouldReturnTrue() throws IOException, GitAPIException {
        File directory = folder.newFolder();
        Git.init().setDirectory(directory).call();

        assertTrue(GitHelper.isGitDirectory(directory.toPath()));
    }

    @Test
    public void isGitDirectoryShouldReturnFalse() throws IOException {
        File directory = folder.newFolder();

        assertFalse(GitHelper.isGitDirectory(directory.toPath()));

        FileTestHelper.appendStringToFile(directory.toPath(), ".git/test", "Test");

        assertFalse(GitHelper.isGitDirectory(directory.toPath()));
    }

    @Test
    public void exportGitArchive() throws IOException, GitAPIException {
        File directory = folder.newFolder();
        Git git = Git.init().setDirectory(directory).call();
        git.commit().setAll(true).setMessage("Commit").call();

        FileTestHelper.appendStringToFile(directory.toPath(), "file1.txt", "Test");
        FileTestHelper.appendStringToFile(directory.toPath(), "file2.txt", "Test");
        FileTestHelper.appendStringToFile(directory.toPath(), "file3.txt", "Test");
        git.add()
                .addFilepattern(".")
                .call();
        git.commit().setAll(true).setMessage("Commit").call();

        File archive = folder.newFile();
        try (OutputStream fos = new FileOutputStream(archive)) {
            GitHelper.exportArchive(git, fos);
        }
        assertTrue(archive.length() > 0);
        ZipInputStream zis
                = new ZipInputStream(new FileInputStream(archive));
        ZipEntry ze = zis.getNextEntry();
        assertNotNull(ze);
        assertEquals(ze.getName(), "file1.txt");
    }

    @Test
    public void exportPatchAndApply() throws IOException, GitAPIException, Exception {
        File directory = folder.newFolder();
        Git git = Git.init().setDirectory(directory).call();
        addAndCommit(git);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            GitHelper.exportDiff(git, os);
            assertTrue(os.toByteArray().length == 0);
        }

        FileTestHelper.appendStringToFile(directory.toPath(), "file1.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory.toPath(), "file2.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory.toPath(), "file3.txt", "Test\n");
        addAndCommit(git);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            GitHelper.exportDiff(git, os);
            assertTrue(os.toByteArray().length > 0);
        }

        FileTestHelper.appendStringToFile(directory.toPath(), "file1.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory.toPath(), "file2.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory.toPath(), "file3.txt", "Test\n");
        addAndCommit(git);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            GitHelper.exportDiff(git, os);
            assertTrue(os.toByteArray().length > 0);
        }
    }

    private static void addAndCommit(Git git) throws GitAPIException {
        git.add()
                .addFilepattern(".")
                .call();
        git.commit().setAll(true).setMessage("Commit").call();
    }
}

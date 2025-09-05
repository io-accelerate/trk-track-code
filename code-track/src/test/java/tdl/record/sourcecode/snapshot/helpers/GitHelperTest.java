package tdl.record.sourcecode.snapshot.helpers;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.util.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tdl.record.sourcecode.test.FileTestHelper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;
import static tdl.record.sourcecode.snapshot.helpers.GitHelper.addAndCommit;

public class GitHelperTest {

    @TempDir
    Path folder;

    @Test
    public void isGitDirectoryShouldReturnTrue() throws IOException, GitAPIException {
        File directory = Files.createTempDirectory(folder, "dir").toFile();
        Git.init().setDirectory(directory).call();

        assertTrue(GitHelper.isGitDirectory(directory.toPath()));
    }

    @Test
    public void isGitDirectoryShouldReturnFalse() throws IOException {
        File directory = Files.createTempDirectory(folder, "dir").toFile();

        assertFalse(GitHelper.isGitDirectory(directory.toPath()));

        FileTestHelper.appendStringToFile(directory.toPath(), ".git/test", "Test");

        assertFalse(GitHelper.isGitDirectory(directory.toPath()));
    }

    @Test
    public void exportGitArchive() throws IOException, GitAPIException {
        File directory = Files.createTempDirectory(folder, "dir").toFile();
        Git git = Git.init().setDirectory(directory).call();
        git.commit().setAll(true).setMessage("Commit").call();

        FileTestHelper.appendStringToFile(directory.toPath(), "file1.txt", "Test");
        FileTestHelper.appendStringToFile(directory.toPath(), "file2.txt", "Test");
        FileTestHelper.appendStringToFile(directory.toPath(), "file3.txt", "Test");
        git.add()
                .addFilepattern(".")
                .call();
        git.commit().setAll(true).setMessage("Commit").call();

        File archive = Files.createTempFile(folder, "someFile", "test").toFile();
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
    public void exportPatchAndApply() throws Exception {
        File directory = Files.createTempDirectory(folder, "dir").toFile();
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

    @Test
    public void exportDiffOnEmptyFiles() throws Exception {
        File directory = Files.createTempDirectory(folder, "dir").toFile();
        Git git = Git.init()
                .setDirectory(directory)
                .call();
        addAndCommit(git);
        //
        File newFile = new File(directory, "testfile.txt");
        FileUtils.createNewFile(newFile);
        assertTrue(newFile.length() == 0);
        addAndCommit(git);
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            GitHelper.exportDiff(git, os);
            //System.out.println(os.toString());
        }
    }

    @Test
    public void applyPatch() throws Exception {
        File directory1 = folder.resolve("directory1").toFile();
        File directory2 = folder.resolve("directory2").toFile();

        Git git1 = Git.init().setDirectory(directory1).call();
        addAndCommit(git1);

        Git git2 = Git.init().setDirectory(directory2).call();
        addAndCommit(git2);

        FileTestHelper.appendStringToFile(directory1.toPath(), "file1.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory1.toPath(), "file2.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory1.toPath(), "file3.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory2.toPath(), "file1.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory2.toPath(), "file2.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory2.toPath(), "file3.txt", "Test\n");

        addAndCommit(git1);
        addAndCommit(git2);

        FileTestHelper.appendStringToFile(directory1.toPath(), "file1.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory1.toPath(), "file2.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory1.toPath(), "file3.txt", "Test\n");

        addAndCommit(git1);

        byte[] diff;
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            GitHelper.exportDiff(git1, os);
            diff = os.toByteArray();
        }

        try (ByteArrayInputStream is = new ByteArrayInputStream(diff)) {
            GitHelper.applyDiff(git2, is);
        }

        assertTrue(FileTestHelper.isDirectoryEqualsWithoutGit(directory1.toPath(), directory2.toPath()));
    }
}

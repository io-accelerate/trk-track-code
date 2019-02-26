package tdl.record.sourcecode.content;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import tdl.record.sourcecode.test.FileTestHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CopyFromDirectorySourceCodeProviderTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Git git;
    private CopyFromDirectorySourceCodeProvider provider;
    private Path sourceFolderPath;
    private Path destination;
    private SourceFolder sourceFolder;


    @Before
    public void setUp() throws Exception {
        File sourceFolderFile = folder.newFolder();
        this.sourceFolderPath = sourceFolderFile.toPath();
        destination = folder.newFolder().toPath();
        sourceFolder = new SourceFolder(sourceFolderPath);

        git = Git.init().setDirectory(sourceFolderFile).call();

        provider = new CopyFromDirectorySourceCodeProvider(this.sourceFolderPath);
    }

    @Test
    public void shouldWorkWithEmptyRepo() throws IOException {
        sourceFolder.createFiles("file1.txt");

        provider.retrieveAndSaveTo(destination);

        assertNotExistsInDestination(".git");
        assertExistsInDestination("file1.txt");
    }

    @Test
    public void shouldWorkWithSubFolders() throws IOException, GitAPIException {
        sourceFolder.createFiles("subdir1/file1.txt");

        git.add().addFilepattern(".").call();
        git.commit().setMessage("commit1").call();

        sourceFolder.createFiles("subdir1/untracked.txt");
        sourceFolder.createFiles("untracked_dir/untracked.txt");

        provider.retrieveAndSaveTo(destination);

        assertExistsInDestination(
                "subdir1/file1.txt",
                "subdir1/untracked.txt",
                "untracked_dir/untracked.txt"
        );
    }


    @Test
    public void shouldHonourGitignoreContent() throws IOException, GitAPIException {
        sourceFolder.createFiles(
                "ok.txt",
                "sourceFile.~java",
                "file1.bak");

        sourceFolder.createFilesInFolder("ignoreFolder",
                "ok.txt",
                "sourceFile.~java",
                "file1.bak");

        sourceFolder.createFilesInFolder("includeFolder",
                "include-ok.txt",
                "ignore-sourceFile.~java",
                "ignore-file1.bak");

        sourceFolder.appendTo(".gitignore", "*.bak\n*.~*\nignoreFolder");

        git.add().addFilepattern(".").call();
        git.commit().setMessage("commit1").call();

        sourceFolder.createFiles("file2.bak");

        provider.retrieveAndSaveTo(destination);

        assertExistsInDestination(
                "ok.txt",
                "includeFolder" + File.separator + "include-ok.txt",
                ".gitignore");
        assertNotExistsInDestination(
                "file1.bak",
                "file2.bak",
                "sourceFile.~java",
                "ignoreFolder" + File.separator + "ok.txt",
                "ignoreFolder" + File.separator + "sourceFile.~java",
                "ignoreFolder" + File.separator + "file1.bak",
                "includeFolder" + File.separator + "ignore-sourceFile.~java",
                "includeFolder" + File.separator + "ignore-file1.bak"
                );
    }

    @Test
    public void ignoreLargeFiles2MBOrLargerInSize()  throws IOException, GitAPIException {
        sourceFolder.appendTo("file-size-1MB.txt",
            FileTestHelper.readFileFromResource("large-files/file-size-1MB.txt")
        );
        sourceFolder.appendTo("file-size-2MB.txt",
            FileTestHelper.readFileFromResource("large-files/file-size-2MB.txt")
        );
        sourceFolder.appendTo("file-size-slightly-smaller-than-2MB.txt",
            FileTestHelper.readFileFromResource("large-files/file-size-slightly-smaller-than-2MB.txt")
        );
        sourceFolder.appendTo("file-size-slightly-bigger-than-2MB.txt",
            FileTestHelper.readFileFromResource("large-files/file-size-slightly-bigger-than-2MB.txt")
        );
        sourceFolder.appendTo("file-size-3MB.txt",
            FileTestHelper.readFileFromResource("large-files/file-size-3MB.txt")
        );
        sourceFolder.appendTo("file-size-193K.txt",
            FileTestHelper.readFileFromResource("large-files/file-size-193K.txt")
        );

        git.add().addFilepattern(".").call();
        git.commit().setMessage("commit1").call();

        sourceFolder.createFiles("file2.bak");

        provider.retrieveAndSaveTo(destination);

        assertExistsInDestination(
            "file2.bak",
            "file-size-1MB.txt",
            "file-size-slightly-smaller-than-2MB.txt",
            "file-size-2MB.txt",
            "file-size-193K.txt"
        );
        assertNotExistsInDestination(
            "file-size-slightly-bigger-than-2MB.txt",
            "file-size-3MB.txt"
        );
    }

    @Test
    public void shouldWorkWithDeletedFiles() throws IOException, GitAPIException {
        sourceFolder.createFiles(
                "file.to.keep.txt",
                "file.to.remove.tracked.txt",
                "file.to.remove.untracked.txt");
        git.add().addFilepattern(".").call();
        git.commit().setMessage("commit1").call();

        sourceFolder.deleteFiles("file.to.remove.tracked.txt");
        git.add().addFilepattern(".").call();

        sourceFolder.deleteFiles("file.to.remove.untracked.txt");


        provider.retrieveAndSaveTo(destination);

        assertExistsInDestination("file.to.keep.txt");
        assertNotExistsInDestination(
                "file.to.remove.tracked.txt",
                "file.to.remove.untracked.txt"
        );
    }


    @Test
    public void shouldWorkWithUncommitedFiles() throws IOException, GitAPIException {
        git.commit().setMessage("initialCommit").call();

        sourceFolder.createFiles("uncommited.txt");
        git.add().addFilepattern(".").call();

        sourceFolder.createFiles("untracked.txt");

        provider.retrieveAndSaveTo(destination);

        assertExistsInDestination(
                "uncommited.txt",
                "untracked.txt");
    }


    //~~~~~~~~~~~ Helpers
    static class SourceFolder {

        private Path sourceFolderPath;
        SourceFolder(Path sourceFolderPath) {
            this.sourceFolderPath = sourceFolderPath;
        }

        private void createFiles(String ... filesToCreate) throws IOException {
            for (String file : filesToCreate) {
                FileTestHelper.appendStringToFile(sourceFolderPath, file, "TEST");
            }
        }

        private void createFilesInFolder(String folderName, String ... filesToCreate) throws IOException {
            Path targetFolderPath = sourceFolderPath.resolve(sourceFolderPath.toString() + File.separator + folderName);
            FileTestHelper.createDirectory(targetFolderPath);
            for (String file : filesToCreate) {
                FileTestHelper.appendStringToFile(targetFolderPath, file, "TEST");
            }
        }

        @SuppressWarnings("SameParameterValue")
        private void appendTo(String file, String content) throws IOException {
            FileTestHelper.appendStringToFile(sourceFolderPath, file, content);
        }

        private void deleteFiles(String ... filesToRemove) {
            for (String fileToRemove : filesToRemove) {
                FileTestHelper.deleteFile(sourceFolderPath, fileToRemove);
            }
        }
    }


    private void assertNotExistsInDestination(String ... filesToCheck) {
        for (String fileToCheck : filesToCheck) {
            assertFalse("File "+fileToCheck+" found in destination",
                    exists(destination, fileToCheck));
        }
        assertFalse(exists(destination, ".git"));
    }

    private void assertExistsInDestination(String ... filesToCheck) {
        for (String fileToCheck : filesToCheck) {
            assertTrue("File "+fileToCheck+" not present in destination",
                    exists(destination, fileToCheck));
        }
    }

    private static boolean exists(Path parent, String filename) {
        return Files.exists(parent.resolve(filename));
    }

}

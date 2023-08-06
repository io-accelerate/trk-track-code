package tdl.record.sourcecode.content;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tdl.record.sourcecode.test.FileTestHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CopyFromDirectorySourceCodeProviderTest {

    @TempDir
    public Path folder;

    private CopyFromDirectorySourceCodeProvider provider;
    private Path destination;
    private SourceFolder sourceFolder;

    @BeforeEach
    public void setUp() throws Exception {
        File sourceFolderFile = folder.resolve("source").toFile();
        Path sourceFolderPath = sourceFolderFile.toPath();
        destination = folder.resolve("destination");
        sourceFolder = new SourceFolder(sourceFolderPath);

        provider = new CopyFromDirectorySourceCodeProvider(sourceFolderPath, 2);
    }

    @Test
    public void shouldWorkWithSimpleFolder() throws IOException {
        sourceFolder.createFiles("file1.txt");

        provider.retrieveAndSaveTo(destination);

        assertExistsInDestination("file1.txt");
    }

    @Test
    public void shouldWorkWithGitFolder() throws IOException, GitAPIException {
        Git.init().setDirectory(sourceFolder.sourceFolderPath.toFile()).call();

        sourceFolder.createFiles("file1.txt");

        provider.retrieveAndSaveTo(destination);

        assertNotExistsInDestination(".git");
    }


    @Test
    public void shouldWorkWithSubFolders() throws IOException {
        sourceFolder.createFiles("subdir1/file1.txt");
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
    public void shouldHonourGitignoreContent() throws IOException {
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
    public void ignoreLargeFiles2MBOrLargerInSize()  throws IOException {
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

    }


    private void assertNotExistsInDestination(String ... filesToCheck) {
        for (String fileToCheck : filesToCheck) {
            assertFalse(exists(destination, fileToCheck), "File "+fileToCheck+" found in destination");
        }
    }

    private void assertExistsInDestination(String ... filesToCheck) {
        for (String fileToCheck : filesToCheck) {
            assertTrue(exists(destination, fileToCheck),"File "+fileToCheck+" not present in destination");
        }
    }

    private static boolean exists(Path parent, String filename) {
        return Files.exists(parent.resolve(filename));
    }

}

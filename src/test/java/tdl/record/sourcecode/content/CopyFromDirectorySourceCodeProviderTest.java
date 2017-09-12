package tdl.record.sourcecode.content;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import tdl.record.sourcecode.test.FileTestHelper;

public class CopyFromDirectorySourceCodeProviderTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void retrieveAndSaveToShouldHonourGitignoreContent() throws IOException, GitAPIException {
        File original = folder.newFolder();
        Path originalPath = original.toPath();
        File actual = folder.newFolder();

        Git git = Git.init().setDirectory(original).call();
        FileTestHelper.appendStringToFile(originalPath, "file1.txt", "TEST");
        FileTestHelper.appendStringToFile(originalPath, "subdir1/file1.txt", "TEST");
        FileTestHelper.appendStringToFile(originalPath, "file1.bak", "TEST");
        FileTestHelper.appendStringToFile(originalPath, ".gitignore", "*.bak");

        git.add().addFilepattern(".").call();
        git.commit().setMessage("commit1").call();

        CopyFromDirectorySourceCodeProvider provider = new CopyFromDirectorySourceCodeProvider(originalPath);
        assertTrue(provider.isGit());
        assertTrue(exists(original, ".git"));
        provider.retrieveAndSaveTo(actual.toPath());

        assertTrue(exists(actual, "file1.txt"));
        assertTrue(exists(actual, "subdir1/file1.txt"));
        assertTrue(exists(actual, ".gitignore"));
        assertFalse(exists(actual, "file1.bak"));
        assertFalse(exists(actual, ".git"));
    }

    private boolean exists(File parent, String path) {
        return Files.exists(parent.toPath().resolve(path));
    }
}

package tdl.record.sourcecode.content;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import static org.junit.Assert.assertFalse;
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
        FileTestHelper.appendStringToFile(originalPath, "file1.bak", "TEST");
        FileTestHelper.appendStringToFile(originalPath, ".gitignore", "*.bak");

        git.add().addFilepattern(".").call();
        git.commit().setMessage("commit1").call();

        CopyFromDirectorySourceCodeProvider provider = new CopyFromDirectorySourceCodeProvider(originalPath);
        provider.retrieveAndSaveTo(actual.toPath());
        
        assertFalse(actual.toPath().resolve("file1.bak").toFile().exists());
    }
}

package tdl.record.sourcecode.snapshot.helpers;

import java.io.File;
import java.io.IOException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
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
}

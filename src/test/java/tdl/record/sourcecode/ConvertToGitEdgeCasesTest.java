package tdl.record.sourcecode;

import org.eclipse.jgit.api.Git;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import support.TestGeneratedSrcsFile;
import tdl.record.sourcecode.snapshot.helpers.GitHelper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static support.TestUtils.writeFile;

public class ConvertToGitEdgeCasesTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public TestGeneratedSrcsFile srcsFile = new TestGeneratedSrcsFile(Arrays.asList(
            dst -> writeFile(dst, "test.txt", "MSG1"), //key
            // Case 1 = Patch with rename
            dst -> writeFile(dst, "Test.txt", "MSG2")
    ), Collections.emptyList());

    @Test
    public void patchWithRenameShouldIgnoreTheRename() throws Exception {
        ConvertToGitCommand command = new ConvertToGitCommand();
        File outputDir = folder.newFolder();

        command.inputFilePath = srcsFile.getFilePath().toString();
        command.outputDirectoryPath = outputDir.toString();
        command.run();

        Git git = Git.init().setDirectory(outputDir).call();
        assertThat("Does not have the commits",
                GitHelper.getCommitCount(git), equalTo(2));

        String commit = "HEAD";
        git.checkout().setName("master").setStartPoint(commit).call();
        Path testFile = outputDir.toPath().resolve("Test.txt");
        assertTrue("File  "+testFile + " does not exist in "+commit,
                testFile.toFile().exists());

        assertThat(Files.readAllLines(testFile), hasItems("MSG2"));
    }
}

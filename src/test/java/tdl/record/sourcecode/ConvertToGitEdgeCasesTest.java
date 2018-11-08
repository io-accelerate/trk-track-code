package tdl.record.sourcecode;

import org.eclipse.jgit.api.Git;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import support.TestGeneratedSrcsFile;
import tdl.record.sourcecode.snapshot.helpers.GitHelper;

import java.io.File;
import java.io.IOException;
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
        assertTrue("File  " + testFile + " does not exist in " + commit,
                testFile.toFile().exists());

        assertThat(Files.readAllLines(testFile), hasItems("MSG2"));
    }

    @Test
    public void applyPatchToSourceWhenTheLastLineIsMissingFromTheHunk() throws Exception {
//  Export segment 7222L from the sourcecode_20180405T111942.srcs
//        ExportSegmentsCommand exportSegmentsCommand = new ExportSegmentsCommand(
//                "/path/to/sourcecode_20180405T111942.srcs",
//                "/path/to/dev-sourcecode-record/src/test/resources/array_index_out_of_bounds_folder/git_issue_24/snapshots.srcs",
//                Arrays.asList(7402L));
//        exportSegmentsCommand.run();
        replayApplyingCommitUsingSrcsSegementsFor("git_issue_24");
    }

    @Test
    public void applyPatchToSourceWhenTheLastLineIsMissingBeforeHunkCanBeApplied() throws Exception {
//  Export segment 3224L from the sourcecode_20180811T125110.srcs
//        ExportSegmentsCommand exportSegmentsCommand = new ExportSegmentsCommand(
//                "/path/to/sourcecode_20180811T125110.srcs",
//                "/path/to/dev-sourcecode-record/src/test/resources/array_index_out_of_bounds_folder/git_issue_25/snapshots.srcs",
//                Arrays.asList(3224L));
//        exportSegmentsCommand.run();
        replayApplyingCommitUsingSrcsSegementsFor("git_issue_25");
    }

    private void replayApplyingCommitUsingSrcsSegementsFor(String gitIssue) throws IOException {
        ListCommand listCommand = new ListCommand();
        String srcsFilePath = getSrcsFileFor(gitIssue);
        listCommand.inputFilePath = srcsFilePath;
        listCommand.run();

        ConvertToGitCommand command = new ConvertToGitCommand();
        File outputDir = folder.newFolder();

        command.inputFilePath = srcsFilePath;
        command.outputDirectoryPath = outputDir.toString();

        try {
            command.run();
        } catch (Exception ex) {
            Assert.fail("Did not complete commit, due to exception: " + ex.getMessage());
        }
    }

    private String getSrcsFileFor(final String gitIssue) {
        ClassLoader classLoader = ConvertToGitEdgeCasesTest.class.getClassLoader();
        File file = new File(classLoader.getResource("array_index_out_of_bounds_folder/" + gitIssue + "/snapshots.srcs").getFile());
        return file.toPath().toString();
    }
}

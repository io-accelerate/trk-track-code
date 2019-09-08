package tdl.record.sourcecode;

import org.eclipse.jgit.api.Git;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import support.TestGeneratedSrcsFile;
import tdl.record.sourcecode.snapshot.SnapshotTypeHint;
import tdl.record.sourcecode.snapshot.helpers.GitHelper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static support.TestUtils.exportToGit;
import static support.TestUtils.writeFile;
import static support.recording.TestRecordingFrame.asFrame;

public class ConvertToGit_Rename_EdgeTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public TestGeneratedSrcsFile srcsFile = new TestGeneratedSrcsFile(Arrays.asList(
            asFrame((Path dst) -> {
                writeFile(dst, "test.txt", "MSG1");
                return SnapshotTypeHint.KEY;
            }),
            // Case 1 = Patch with rename
            asFrame((Path dst) -> {
                writeFile(dst, "Test.txt", "MSG2");
                return SnapshotTypeHint.PATCH;
            })
    ));


    @Test
    public void patchWithRenameShouldIgnoreTheRename() throws Exception {
        File outputDir = folder.newFolder();
        exportToGit(srcsFile, outputDir);

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

}

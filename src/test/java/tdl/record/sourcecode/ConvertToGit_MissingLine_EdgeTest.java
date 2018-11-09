package tdl.record.sourcecode;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import support.TestGeneratedSrcsFile;
import tdl.record.sourcecode.snapshot.SnapshotTypeHint;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import static support.TestUtils.writeFile;

public class ConvertToGit_MissingLine_EdgeTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public TestGeneratedSrcsFile srcsFileWithMissingLineRemoval = new TestGeneratedSrcsFile(Arrays.asList(
            // Step 1 - content with one line of text
            (Path dst) -> {
                writeFile(dst, "test.txt", "BODY\n");
                return SnapshotTypeHint.KEY;
            },
            // Step 2 - content with 3 empty lines and then a line of text
            (Path dst) -> {
                writeFile(dst, "test.txt", "BODY\n\n\n\ncheckout");
                return SnapshotTypeHint.KEY;
            },
            // Step 3 - content with 1 empty line removed from the middle
            (Path dst) -> {
                writeFile(dst, "test.txt", "BODY\n\n\ncheckout");
                return SnapshotTypeHint.PATCH;
            },
            // Step 4 - content with last two lines removed (new line and a line of text)
            (Path dst) -> {
                writeFile(dst, "test.txt", "BODY\n\n");
                return SnapshotTypeHint.PATCH;
            },
            // Step 5 - content with new line removed from the end (back to original file)
            (Path dst) -> {
                writeFile(dst, "test.txt", "BODY\n");
                return SnapshotTypeHint.PATCH;
            }
    ), Collections.emptyList());


    @Test
    public void usersShouldBeAbleToSaveFilesWithNoLineEndings() throws Exception {
        ConvertToGitCommand command = new ConvertToGitCommand();
        File outputDir = folder.newFolder();

        command.inputFilePath = srcsFileWithMissingLineRemoval.getFilePath().toString();
        command.outputDirectoryPath = outputDir.toString();
        command.run();
    }
}

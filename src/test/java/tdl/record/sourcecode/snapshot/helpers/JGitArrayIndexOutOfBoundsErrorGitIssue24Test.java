package tdl.record.sourcecode.snapshot.helpers;

import org.eclipse.jgit.api.Git;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import tdl.record.sourcecode.test.FileTestHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import static org.junit.Assert.assertTrue;
import static tdl.record.sourcecode.snapshot.helpers.GitHelper.addAndCommit;

public class JGitArrayIndexOutOfBoundsErrorGitIssue24Test {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void applyPatchToSourceWhenTheLastLineIsMissingFromTheHunk() throws Exception {
        File sourceDirectory = folder.newFolder();
        File targetDirectory = folder.newFolder();

        Git sourceGitRepo = Git.init().setDirectory(sourceDirectory).call();
        addAndCommit(sourceGitRepo);

        Git targetGitRepo = Git.init().setDirectory(targetDirectory).call();
        addAndCommit(targetGitRepo);

        FileTestHelper.appendStringToFile(sourceDirectory.toPath(), "file1.txt", BLOCK_OF_CODE_WITH_EXTRA_NEW_LINE());
        FileTestHelper.appendStringToFile(targetDirectory.toPath(), "file1.txt", BLOCK_OF_CODE_WITH_EXTRA_NEW_LINE());
        addAndCommit(sourceGitRepo);
        addAndCommit(targetGitRepo);

        FileTestHelper.changeContentOfFile(sourceDirectory.toPath(), "file1.txt", BLOCK_OF_CODE());
        addAndCommit(sourceGitRepo);

        byte[] exportedDiffAsByteArray;
        try (ByteArrayOutputStream exportDiffAsStream = new ByteArrayOutputStream()) {
            GitHelper.exportDiff(sourceGitRepo, exportDiffAsStream);
            exportedDiffAsByteArray = exportDiffAsStream.toByteArray();
        }

        // >>>>>>>>>>>>> This block is necessary to be able to reproduce the issue 25 at hand <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        // Root cause of the Issue 24: The api that reads the file is reading files incorrectly. If a file ends with a carriage return on reading
        // it this carriage return disappears and not counted as a line. While the file still contains that line.
        // This is seen from the presence of two functions trying to correct the situation - i.e. isNoNewlineAtEndOfFile and isMissingNewlineAtEnd
        FileTestHelper.changeContentOfFile(targetDirectory.toPath(), "file1.txt", BLOCK_OF_CODE_WITH_EXACT_NUMBER_OF_LINES());
        addAndCommit(targetGitRepo);
        // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

        try (ByteArrayInputStream patch = new ByteArrayInputStream(exportedDiffAsByteArray)) {
            try {
                GitHelper.applyDiff(targetGitRepo, patch);
                assertTrue(FileTestHelper.isDirectoryEqualsWithoutGit(
                        sourceDirectory.toPath(),
                        targetDirectory.toPath())
                );
            } catch (Exception ex) {
                Assert.fail("Should not have thrown exception: " + ex.getMessage());
            }
        }
    }

    private static final String RESOURCE_FOLDER = "array_index_out_of_bounds_folder/git_issue_24/";

    private String BLOCK_OF_CODE_WITH_EXACT_NUMBER_OF_LINES() {
        return FileTestHelper.readFileFromResource(RESOURCE_FOLDER + "oldfile-exact-number-of-lines.txt");
    }

    private String BLOCK_OF_CODE() {
        return FileTestHelper.readFileFromResource(RESOURCE_FOLDER + "newfile.txt");
    }

    private String BLOCK_OF_CODE_WITH_EXTRA_NEW_LINE() {
        return FileTestHelper.readFileFromResource(RESOURCE_FOLDER + "oldfile-one-extra-line.txt");
    }
}

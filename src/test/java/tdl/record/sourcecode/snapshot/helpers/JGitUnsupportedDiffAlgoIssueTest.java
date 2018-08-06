package tdl.record.sourcecode.snapshot.helpers;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.util.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tdl.record.sourcecode.test.FileTestHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertTrue;
import static tdl.record.sourcecode.snapshot.helpers.GitHelper.addAndCommit;

@RunWith(Parameterized.class)
public class JGitUnsupportedDiffAlgoIssueTest {

    private File directory;
    private Git git;
    private final String diffAlgorithm;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                { "patience" },
                { "myers" },
                { "histogram" },
                { "xxxx" },
                { "" },
                { " " },
                { null },
        });
    }

    public JGitUnsupportedDiffAlgoIssueTest(String diffAlgorithm) {
        this.diffAlgorithm = diffAlgorithm;
    }

    @Before
    public void setup() throws IOException, GitAPIException {
        directory = folder.newFolder();
        git = Git.init().setDirectory(directory).call();
        Repository repository = git.getRepository();
        repository.getConfig().setString(
            ConfigConstants.CONFIG_DIFF_SECTION,
            null,
            ConfigConstants.CONFIG_KEY_ALGORITHM,
                diffAlgorithm);
    }

    @Test
    public void exportPatchAndApply() throws Exception {
        addAndCommit(git);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            GitHelper.exportDiff(git, os);
            assertTrue(os.toByteArray().length == 0);
        }

        FileTestHelper.appendStringToFile(directory.toPath(), "file1.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory.toPath(), "file2.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory.toPath(), "file3.txt", "Test\n");
        addAndCommit(git);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            GitHelper.exportDiff(git, os);
            assertTrue(os.toByteArray().length > 0);
        }

        FileTestHelper.appendStringToFile(directory.toPath(), "file1.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory.toPath(), "file2.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory.toPath(), "file3.txt", "Test\n");
        addAndCommit(git);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            GitHelper.exportDiff(git, os);
            assertTrue(os.toByteArray().length > 0);
        }
    }

    @Test
    public void exportDiffOnEmptyFiles() throws Exception {
        addAndCommit(git);
        //
        File newFile = new File(directory, "testfile.txt");
        FileUtils.createNewFile(newFile);
        assertTrue(newFile.length() == 0);
        addAndCommit(git);
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            GitHelper.exportDiff(git, os);
            //System.out.println(os.toString());
        }
    }

    @Test
    public void applyPatch() throws Exception {
        File directory1 = folder.newFolder();
        File directory2 = folder.newFolder();

        Git git1 = Git.init().setDirectory(directory1).call();
        addAndCommit(git1);

        Git git2 = Git.init().setDirectory(directory2).call();
        addAndCommit(git2);

        FileTestHelper.appendStringToFile(directory1.toPath(), "file1.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory1.toPath(), "file2.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory1.toPath(), "file3.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory2.toPath(), "file1.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory2.toPath(), "file2.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory2.toPath(), "file3.txt", "Test\n");

        addAndCommit(git1);
        addAndCommit(git2);

        FileTestHelper.appendStringToFile(directory1.toPath(), "file1.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory1.toPath(), "file2.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory1.toPath(), "file3.txt", "Test\n");

        addAndCommit(git1);

        byte[] diff;
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            GitHelper.exportDiff(git1, os);
            diff = os.toByteArray();
        }

        try (ByteArrayInputStream is = new ByteArrayInputStream(diff)) {
            GitHelper.applyDiff(git2, is);
        }

        assertTrue(FileTestHelper.isDirectoryEqualsWithoutGit(directory1.toPath(), directory2.toPath()));
    }
}

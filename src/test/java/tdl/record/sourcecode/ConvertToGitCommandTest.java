package tdl.record.sourcecode;

import org.eclipse.jgit.api.Git;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import support.TestGeneratedSrcsFile;
import tdl.record.sourcecode.snapshot.SnapshotTypeHint;
import tdl.record.sourcecode.snapshot.helpers.GitHelper;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;
import static support.TestUtils.writeFile;
import static tdl.record.sourcecode.test.FileTestHelper.appendStringToFile;
import static tdl.record.sourcecode.test.FileTestHelper.doesFileExist;

public class ConvertToGitCommandTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public TestGeneratedSrcsFile srcsFile = new TestGeneratedSrcsFile(Arrays.asList(
            (Path dst) -> {
                writeFile(dst, "test1.txt", "TEST1");
                return SnapshotTypeHint.KEY;
            },
            (Path dst) -> {
                writeFile(dst, "test1.txt", "TEST1TEST2");
                return SnapshotTypeHint.PATCH;
            },
            (Path dst) -> {
                writeFile(dst, "test2.txt", "TEST1TEST2");
                return SnapshotTypeHint.PATCH;
            },
            (Path dst) -> {
                writeFile(dst, "test2.txt", "TEST1TEST2");
                writeFile(dst, "subdir/test3.txt", "TEST3");
                return SnapshotTypeHint.KEY;
            },
            (Path dst) -> {
                // Empty folder
                return SnapshotTypeHint.PATCH;
            },
            (Path dst) -> {
                writeFile(dst, "test1.txt", "TEST1TEST2");
                return SnapshotTypeHint.PATCH;
            },
            (Path dst) -> {
                writeFile(dst, "test1.txt", "TEST1TEST2");
                return SnapshotTypeHint.KEY;
            },
            (Path dst) -> {
                writeFile(dst, "test1.txt", "TEST1TEST2");
                return SnapshotTypeHint.PATCH;
            },
            (Path dst) -> {
                writeFile(dst, "test1.txt", "TEST1TEST2");
                return SnapshotTypeHint.PATCH;
            },
            (Path dst) -> {
                writeFile(dst, "test1.txt", "TEST1TEST2");
                return SnapshotTypeHint.KEY;
            }
    ), Arrays.asList("tag", "tag"));


    @Test
    public void runShouldCreateDirectoryIfFile() throws Exception {
        ConvertToGitCommand command = new ConvertToGitCommand();
        File file = folder.newFile();
        command.inputFilePath = srcsFile.getFilePath().toString();
        command.outputDirectoryPath = file.toString();
        command.run();
        assertTrue(file.isDirectory());
    }

    @Test
    public void runShouldCleanDirectoryIfNotGit() throws Exception {
        ConvertToGitCommand command = new ConvertToGitCommand();
        File outputDir = folder.newFolder();
        appendStringToFile(outputDir.toPath(), "randomfile.txt", "Lorem Ipsum");
        assertTrue(doesFileExist(outputDir.toPath(), "randomfile.txt"));
        command.inputFilePath = srcsFile.getFilePath().toString();
        command.outputDirectoryPath = outputDir.toString();
        command.run();
        assertFalse(doesFileExist(outputDir.toPath(), "randomfile.txt"));
    }

    @Test
    public void runShouldAppendGit() throws Exception {
        ConvertToGitCommand command = new ConvertToGitCommand();
        File outputDir = folder.newFolder();

        Git git = Git.init().setDirectory(outputDir).call();
        appendStringToFile(outputDir.toPath(), "randomfile.txt", "Lorem Ipsum");
        GitHelper.addAndCommit(git);
        GitHelper.tag(git, "tag");
        GitHelper.tag(git, "tag_1");
        assertEquals(GitHelper.getCommitCount(git), 1);

        command.inputFilePath = srcsFile.getFilePath().toString();
        command.outputDirectoryPath = outputDir.toString();
        command.run();

        assertThat(GitHelper.getCommitCount(git), equalTo(11));
        assertThat(GitHelper.getTags(git), equalTo(Arrays.asList("tag", "tag_1", "tag_2", "tag_3")));
    }
    
    @Test
    public void whenWipeTrue_runShouldRemoveGit() throws Exception {
        ConvertToGitCommand command = new ConvertToGitCommand();
        File outputDir = folder.newFolder();

        Git git = Git.init().setDirectory(outputDir).call();
        appendStringToFile(outputDir.toPath(), "randomfile.txt", "Lorem Ipsum");
        GitHelper.addAndCommit(git);
        assertEquals(GitHelper.getCommitCount(git), 1);

        command.inputFilePath = srcsFile.getFilePath().toString();
        command.outputDirectoryPath = outputDir.toString();
        command.wipeDestinationRepo = true;
        command.run();

        assertThat(GitHelper.getCommitCount(git), equalTo(10));
    }
}

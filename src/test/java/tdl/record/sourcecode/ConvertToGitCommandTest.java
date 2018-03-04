package tdl.record.sourcecode;

import org.eclipse.jgit.api.Git;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import support.TestGeneratedSrcsFile;

import java.io.File;
import java.util.Arrays;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;
import static support.TestUtils.writeFile;
import static tdl.record.sourcecode.test.FileTestHelper.appendStringToFile;
import static tdl.record.sourcecode.test.FileTestHelper.doesFileExist;
import static tdl.record.sourcecode.test.GitTestHelper.*;

public class ConvertToGitCommandTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public TestGeneratedSrcsFile srcsFile = new TestGeneratedSrcsFile(Arrays.asList(
            dst -> writeFile(dst, "test1.txt", "TEST1"), //key
            dst -> writeFile(dst, "test1.txt", "TEST1TEST2"), //patch
            dst -> writeFile(dst, "test2.txt", "TEST1TEST2"), //patch
            dst -> { //key
                writeFile(dst, "test2.txt", "TEST1TEST2");
                writeFile(dst, "subdir/test3.txt", "TEST3");
            },
            dst -> {/* Empty folder */ }, //patch
            dst -> writeFile(dst, "test1.txt", "TEST1TEST2"), //patch
            dst -> writeFile(dst, "test1.txt", "TEST1TEST2"), //key
            dst -> writeFile(dst, "test1.txt", "TEST1TEST2"), //patch
            dst -> writeFile(dst, "test1.txt", "TEST1TEST2"), //patch
            dst -> writeFile(dst, "test1.txt", "TEST1TEST2") //key
    ), Arrays.asList("tag","tag1"));

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
        addAndCommit(git);
        git.tag().setName("tag").call();
        assertEquals(getCommitCount(git), 1);

        command.inputFilePath = srcsFile.getFilePath().toString();
        command.outputDirectoryPath = outputDir.toString();
        command.run();

        assertThat(getCommitCount(git), equalTo(11));
        assertThat(getTags(git), equalTo(Arrays.asList("tag", "tag1")));
    }
    
    @Test
    public void whenAppendFalse_runShouldRemoveGit() throws Exception {
        ConvertToGitCommand command = new ConvertToGitCommand();
        File outputDir = folder.newFolder();

        Git git = Git.init().setDirectory(outputDir).call();
        appendStringToFile(outputDir.toPath(), "randomfile.txt", "Lorem Ipsum");
        addAndCommit(git);
        assertEquals(getCommitCount(git), 1);

        command.inputFilePath = srcsFile.getFilePath().toString();
        command.outputDirectoryPath = outputDir.toString();
        command.appendGit = false;
        command.run();

        assertThat(getCommitCount(git), equalTo(10));
    }
}

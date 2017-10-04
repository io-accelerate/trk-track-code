package tdl.record.sourcecode;

import java.io.File;
import java.util.Arrays;
import org.eclipse.jgit.api.Git;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import support.TemporarySourceCodeRecorder;
import static tdl.record.sourcecode.test.FileTestHelper.*;
import static tdl.record.sourcecode.test.GitTestHelper.*;

public class ConvertToGitCommandTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public TemporarySourceCodeRecorder recorder = new TemporarySourceCodeRecorder(Arrays.asList(
            dst -> TemporarySourceCodeRecorder.writeFile(dst, "test1.txt", "TEST1"), //key
            dst -> TemporarySourceCodeRecorder.writeFile(dst, "test1.txt", "TEST1TEST2"), //patch
            dst -> TemporarySourceCodeRecorder.writeFile(dst, "test2.txt", "TEST1TEST2"), //patch
            dst -> { //key
                TemporarySourceCodeRecorder.writeFile(dst, "test2.txt", "TEST1TEST2");
                TemporarySourceCodeRecorder.writeFile(dst, "subdir/test3.txt", "TEST3");
            },
            dst -> {/* Empty folder */ }, //patch
            dst -> TemporarySourceCodeRecorder.writeFile(dst, "test1.txt", "TEST1TEST2"), //patch
            dst -> TemporarySourceCodeRecorder.writeFile(dst, "test1.txt", "TEST1TEST2"), //key
            dst -> TemporarySourceCodeRecorder.writeFile(dst, "test1.txt", "TEST1TEST2"), //patch
            dst -> TemporarySourceCodeRecorder.writeFile(dst, "test1.txt", "TEST1TEST2"), //patch
            dst -> TemporarySourceCodeRecorder.writeFile(dst, "test1.txt", "TEST1TEST2") //key
    ));

    @Test
    public void runShouldCreateDirectoryIfFile() throws Exception {
        ConvertToGitCommand command = new ConvertToGitCommand();
        File file = folder.newFile();
        command.inputFilePath = recorder.getOutputFilePath().toString();
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
        command.inputFilePath = recorder.getOutputFilePath().toString();
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
        assertEquals(getCommitCount(git), 1);

        command.inputFilePath = recorder.getOutputFilePath().toString();
        command.outputDirectoryPath = outputDir.toString();
        command.run();
        
        assertEquals(getCommitCount(git), 11);
    }
    
    @Test
    public void runShouldRemoveGit() throws Exception {
        ConvertToGitCommand command = new ConvertToGitCommand();
        File outputDir = folder.newFolder();

        Git git = Git.init().setDirectory(outputDir).call();
        appendStringToFile(outputDir.toPath(), "randomfile.txt", "Lorem Ipsum");
        addAndCommit(git);
        assertEquals(getCommitCount(git), 1);

        command.inputFilePath = recorder.getOutputFilePath().toString();
        command.outputDirectoryPath = outputDir.toString();
        command.appendGit = false;
        command.run();
        
        assertEquals(getCommitCount(git), 10);
    }
}

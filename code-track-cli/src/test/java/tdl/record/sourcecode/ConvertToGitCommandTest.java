package tdl.record.sourcecode;

import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tdl.record.sourcecode.record.SourceCodeRecorderException;
import tdl.record.sourcecode.snapshot.SnapshotTypeHint;
import tdl.record.sourcecode.snapshot.helpers.GitHelper;
import tdl.record.sourcecode.test.support.TestGeneratedSrcsFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static tdl.record.sourcecode.test.support.TestUtils.writeFile;
import static tdl.record.sourcecode.test.support.recording.TestRecordingFrame.asFrame;
import static tdl.record.sourcecode.test.FileTestHelper.appendStringToFile;
import static tdl.record.sourcecode.test.FileTestHelper.doesFileExist;

public class ConvertToGitCommandTest {

    @TempDir
    Path folder;

    private TestGeneratedSrcsFile recorder;

    @BeforeEach
    public void setUp() throws SourceCodeRecorderException, IOException {
        recorder = new TestGeneratedSrcsFile(Arrays.asList(
                asFrame(Collections.singletonList("tag"), (Path dst) -> {
                    writeFile(dst, "test1.txt", "TEST1");
                    return SnapshotTypeHint.KEY;
                }),
                asFrame(Collections.singletonList("tag"), (Path dst) -> {
                    writeFile(dst, "test1.txt", "TEST1TEST2");
                    return SnapshotTypeHint.PATCH;
                }),
                asFrame((Path dst) -> {
                    writeFile(dst, "test2.txt", "TEST1TEST2");
                    return SnapshotTypeHint.PATCH;
                }),
                asFrame((Path dst) -> {
                    writeFile(dst, "test2.txt", "TEST1TEST2");
                    writeFile(dst, "subdir/test3.txt", "TEST3");
                    return SnapshotTypeHint.KEY;
                }),
                asFrame((Path dst) -> {
                    // Empty folder
                    return SnapshotTypeHint.PATCH;
                }),
                asFrame((Path dst) -> {
                    writeFile(dst, "test1.txt", "TEST1TEST2");
                    return SnapshotTypeHint.PATCH;
                }),
                asFrame((Path dst) -> {
                    writeFile(dst, "test1.txt", "TEST1TEST2");
                    return SnapshotTypeHint.KEY;
                }),
                asFrame((Path dst) -> {
                    writeFile(dst, "test1.txt", "TEST1TEST2");
                    return SnapshotTypeHint.PATCH;
                }),
                asFrame((Path dst) -> {
                    writeFile(dst, "test1.txt", "TEST1TEST2");
                    return SnapshotTypeHint.PATCH;
                }),
                asFrame((Path dst) -> {
                    writeFile(dst, "test1.txt", "TEST1TEST2");
                    return SnapshotTypeHint.KEY;
                })
        ));
        recorder.beforeEach();
    }

    @AfterEach
    void tearDown() throws IOException {
        recorder.afterEach();
    }

    @Test
    public void runShouldCreateDirectoryIfFile() throws Exception {
        ConvertToGitCommand command = new ConvertToGitCommand();
        File file = Files.createTempFile(folder, "someFile", "test").toFile();
        command.inputFilePath = recorder.getFilePath().toString();
        command.outputDirectoryPath = file.toString();
        command.run();
        assertTrue(file.isDirectory());
    }

    @Test
    public void runShouldCleanDirectoryIfNotGit() throws Exception {
        ConvertToGitCommand command = new ConvertToGitCommand();
        File outputDir = Files.createTempDirectory(folder, "dir").toFile();
        appendStringToFile(outputDir.toPath(), "randomfile.txt", "Lorem Ipsum");
        assertTrue(doesFileExist(outputDir.toPath(), "randomfile.txt"));
        command.inputFilePath = recorder.getFilePath().toString();
        command.outputDirectoryPath = outputDir.toString();
        command.run();
        assertFalse(doesFileExist(outputDir.toPath(), "randomfile.txt"));
    }

    @Test
    public void runShouldAppendGit() throws Exception {
        ConvertToGitCommand command = new ConvertToGitCommand();
        File outputDir = Files.createTempDirectory(folder, "dir").toFile();

        Git git = Git.init().setDirectory(outputDir).call();
        appendStringToFile(outputDir.toPath(), "randomfile.txt", "Lorem Ipsum");
        GitHelper.addAndCommit(git);
        GitHelper.tag(git, "tag");
        GitHelper.tag(git, "tag_1");
        assertEquals(GitHelper.getCommitCount(git), 1);

        command.inputFilePath = recorder.getFilePath().toString();
        command.outputDirectoryPath = outputDir.toString();
        command.run();

        assertThat(GitHelper.getCommitCount(git), equalTo(11));
        assertThat(GitHelper.getTags(git), equalTo(Arrays.asList("tag", "tag_1", "tag_2", "tag_3")));
    }

    @Test
    public void whenWipeTrue_runShouldRemoveGit() throws Exception {
        ConvertToGitCommand command = new ConvertToGitCommand();
        File outputDir = Files.createTempDirectory(folder, "dir").toFile();

        Git git = Git.init().setDirectory(outputDir).call();
        appendStringToFile(outputDir.toPath(), "randomfile.txt", "Lorem Ipsum");
        GitHelper.addAndCommit(git);
        assertEquals(GitHelper.getCommitCount(git), 1);

        command.inputFilePath = recorder.getFilePath().toString();
        command.outputDirectoryPath = outputDir.toString();
        command.wipeDestinationRepo = true;
        command.run();

        assertThat(GitHelper.getCommitCount(git), equalTo(10));
    }
}

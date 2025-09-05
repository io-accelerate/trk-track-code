package io.accelerate.track.code;

import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import io.accelerate.track.code.record.SourceCodeRecorderException;
import io.accelerate.track.code.snapshot.SnapshotTypeHint;
import io.accelerate.track.code.snapshot.helpers.GitHelper;
import io.accelerate.track.code.test.support.TestGeneratedSrcsFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static io.accelerate.track.code.test.FileTestHelper.exportToGit;
import static io.accelerate.track.code.test.support.TestUtils.writeFile;
import static io.accelerate.track.code.test.support.recording.TestRecordingFrame.asFrame;

public class ConvertToGit_Rename_EdgeTest {

    @TempDir
    Path folder;

    private TestGeneratedSrcsFile recorder;

    @BeforeEach
    public void setUp() throws SourceCodeRecorderException, IOException {
        recorder =  new TestGeneratedSrcsFile(Arrays.asList(
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
        recorder.beforeEach();
    }

    @AfterEach
    void tearDown() throws IOException {
        recorder.afterEach();
    }


    @Test
    public void patchWithRenameShouldIgnoreTheRename() throws Exception {
        File outputDir = Files.createTempDirectory(folder, "dir").toFile();
        exportToGit(recorder, outputDir);

        Git git = Git.init().setDirectory(outputDir).call();
        assertThat("Does not have the commits",
                GitHelper.getCommitCount(git), equalTo(2));

        String commit = "HEAD";
        git.checkout().setName("master").setStartPoint(commit).call();
        Path testFile = outputDir.toPath().resolve("Test.txt");
        assertTrue(testFile.toFile().exists(), "File  " + testFile + " does not exist in " + commit);

        assertThat(Files.readAllLines(testFile), hasItems("MSG2"));
    }

}

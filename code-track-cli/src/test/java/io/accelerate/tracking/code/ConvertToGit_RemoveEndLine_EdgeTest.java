package io.accelerate.tracking.code;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import io.accelerate.tracking.code.record.SourceCodeRecorderException;
import io.accelerate.tracking.code.snapshot.SnapshotTypeHint;
import io.accelerate.tracking.code.test.support.TestGeneratedSrcsFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static io.accelerate.tracking.code.test.FileTestHelper.exportToGit;
import static io.accelerate.tracking.code.test.support.TestUtils.writeFile;
import static io.accelerate.tracking.code.test.support.recording.TestRecordingFrame.asFrame;

public class ConvertToGit_RemoveEndLine_EdgeTest {

    @TempDir
    Path folder;

    private TestGeneratedSrcsFile recorder;

    @BeforeEach
    public void setUp() throws SourceCodeRecorderException, IOException {
        recorder = new TestGeneratedSrcsFile(Arrays.asList(
                // Step 1 - content with line plus ending statement
                asFrame((Path dst) -> {
                    writeFile(dst, "test.txt", "BODY\n\ncontent-no-newline");
                    return SnapshotTypeHint.KEY;
                }),
                // Step 1 - remove the ending statement
                asFrame((Path dst) -> {
                    writeFile(dst, "test.txt", "BODY\n\n");
                    return SnapshotTypeHint.PATCH;
                }),
                // Step 2 - re-add the ending statement
                asFrame((Path dst) -> {
                    writeFile(dst, "test.txt", "BODY\n\ncontent-with-newline");
                    return SnapshotTypeHint.PATCH;
                })));
        recorder.beforeEach();
    }

    @AfterEach
    void tearDown() throws IOException {
        recorder.afterEach();
    }

    @Test
    public void exportTest() throws IOException {
        File newFolder = Files.createTempDirectory(folder, "dir").toFile();
        exportToGit(recorder, newFolder);
    }
}

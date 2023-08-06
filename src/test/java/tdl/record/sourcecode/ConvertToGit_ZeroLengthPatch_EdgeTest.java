package tdl.record.sourcecode;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import support.TestGeneratedSrcsFile;
import tdl.record.sourcecode.record.SourceCodeRecorderException;
import tdl.record.sourcecode.snapshot.SnapshotTypeHint;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static support.TestUtils.exportToGit;
import static support.TestUtils.writeFile;
import static support.recording.TestRecordingFrame.asFrame;

public class ConvertToGit_ZeroLengthPatch_EdgeTest {

    @TempDir
    Path folder;

    private TestGeneratedSrcsFile recorder;

    @BeforeEach
    public void setUp() throws SourceCodeRecorderException, IOException {
        recorder = new TestGeneratedSrcsFile(Arrays.asList(
                asFrame((Path dst) -> {
                    writeFile(dst, "test.txt", "BODY\n");
                    return SnapshotTypeHint.KEY;
                }),
                // No changes
                asFrame((Path dst) -> {
                    writeFile(dst, "test.txt", "BODY\n");
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
    public void exportTest() throws IOException {
        File newFolder = Files.createTempDirectory(folder, "dir").toFile();
        exportToGit(recorder, newFolder);
    }
}

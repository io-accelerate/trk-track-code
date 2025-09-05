package tdl.record.sourcecode;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tdl.record.sourcecode.record.SourceCodeRecorderException;
import tdl.record.sourcecode.snapshot.SnapshotTypeHint;
import tdl.record.sourcecode.test.support.TestGeneratedSrcsFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import static tdl.record.sourcecode.test.support.TestUtils.writeFile;
import static tdl.record.sourcecode.test.support.recording.TestRecordingFrame.asFrame;

public class ListCommandTest {

    @TempDir
    Path folder;

    private TestGeneratedSrcsFile recorder;

    @BeforeEach
    public void setUp() throws SourceCodeRecorderException, IOException {
        recorder = new TestGeneratedSrcsFile(Arrays.asList(
                asFrame(Collections.singletonList("tag1"), (Path dst) -> {
                    writeFile(dst, "test1.txt", "TEST1");
                    return SnapshotTypeHint.KEY;
                }),
                asFrame(Collections.singletonList("x"), (Path dst) -> {
                    writeFile(dst, "test1.txt", "TEST1TEST2");
                    return SnapshotTypeHint.PATCH;
                }),
                asFrame(Collections.singletonList("tag12"), (Path dst) -> {
                    writeFile(dst, "test2.txt", "TEST1TEST2");
                    return SnapshotTypeHint.PATCH;
                }),
                asFrame(Collections.singletonList("tag3"), (Path dst) -> {
                    writeFile(dst, "test2.txt", "TEST1TEST2");
                    writeFile(dst, "subdir/test3.txt", "TEST3");
                    return SnapshotTypeHint.KEY;
                }),
                asFrame(Collections.singletonList("tag3"), (Path dst) -> {
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
    public void run() {
        ListCommand command = new ListCommand();
        command.inputFilePath = recorder.getFilePath().toString();
        command.run();
    }
}

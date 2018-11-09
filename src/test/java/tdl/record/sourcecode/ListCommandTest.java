package tdl.record.sourcecode;

import java.nio.file.Path;
import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import support.TestGeneratedSrcsFile;
import tdl.record.sourcecode.snapshot.SnapshotTypeHint;

import static support.TestUtils.*;

public class ListCommandTest {

    @Rule
    public TestGeneratedSrcsFile recorder = new TestGeneratedSrcsFile(Arrays.asList(
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
    ), Arrays.asList("tag1", "x", "tag12", "tag3", "tag3"));


    @Test
    public void run() {
        ListCommand command = new ListCommand();
        command.inputFilePath = recorder.getFilePath().toString();
        command.run();
    }
}

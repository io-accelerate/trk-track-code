package tdl.record.sourcecode;

import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import support.TestGeneratedSrcsFile;

import static support.TestUtils.*;

public class ListCommandTest {

    @Rule
    public TestGeneratedSrcsFile recorder = new TestGeneratedSrcsFile(Arrays.asList(
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
    ));

    @Test
    public void run() {
        ListCommand command = new ListCommand();
        command.inputFilePath = recorder.getFilePath().toString();
        command.run();
    }
}

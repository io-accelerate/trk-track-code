package tdl.record.sourcecode;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import org.apache.commons.io.FileUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import tdl.record.sourcecode.test.TemporarySourceCodeRecorder;

public class ExportCommandTest {

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
    public void run() throws IOException {
        Path inputFilePath = recorder.getOutputFilePath();
        
        
        Path dir1 = folder.newFolder().toPath();
        int time1 = 0;
        (new ExportCommand(inputFilePath.toString(), dir1.toString(), time1)).run();
        assertTrue(exists(dir1, "test1.txt"));
        assertEquals("TEST1", readFile(dir1, "test1.txt"));
        
        //TODO:
//        Path dir2 = folder.newFolder().toPath();
//        int time2 = 1;
//        (new ExportCommand(inputFilePath.toString(), dir2.toString(), time2)).run();
//        assertTrue(exists(dir2, "test1.txt"));
//        assertEquals("TEST1TEST2", readFile(dir2, "test1.txt"));

        Path dir3 = folder.newFolder().toPath();
        int time3 = 2;
        (new ExportCommand(inputFilePath.toString(), dir3.toString(), time3)).run();
        assertTrue(exists(dir3, "test1.txt"));
        //assertTrue(exists(dir3, "test2.txt"));
        assertEquals("TEST1TEST2", readFile(dir3, "test1.txt"));
        //assertEquals("TEST1TEST2", readFile(dir3, "test2.txt"));
    }

    public boolean exists(Path parent, String path) {
        return Files.exists(parent.resolve(path));
    }

    public String readFile(Path parent, String path) {
        File file = parent.resolve(path).toFile();
        try {
            return FileUtils.readFileToString(file, Charset.defaultCharset());
        } catch (IOException ex) {
            return "";
        }
    }
}

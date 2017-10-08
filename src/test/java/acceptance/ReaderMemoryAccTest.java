package acceptance;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import support.TemporarySourceCodeRecorder;
import tdl.record.sourcecode.ExportCommand;
import tdl.record.sourcecode.snapshot.file.BaselineReader;
import tdl.record.sourcecode.snapshot.file.Reader;
import tdl.record.sourcecode.snapshot.file.Segment;

public class ReaderMemoryAccTest {

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
    public void shouldUseMinimumMemory() throws IOException {
        File inputFile = recorder.getOutputFilePath().toFile();
        long base = getUsedMemory();
        //TODO: Measure baseline
        Reader reader1 = new Reader(inputFile);
        List<Segment> segments1 = reader1.getSnapshots();
        long used1 = getUsedMemory() - base;

        //TODO: Measure optimal
        BaselineReader reader2 = new BaselineReader(inputFile);
        List<Segment> segments2 = reader2.getSnapshots();
        long used2 = getUsedMemory() - used1;

        System.out.println(base + " " + used1 + " " + used2);
    }

    public long getUsedMemory() {
        return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
    }
}

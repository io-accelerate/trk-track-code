package tdl.record.sourcecode.snapshot.file;

import tdl.record.sourcecode.snapshot.file.Writer;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import support.time.FakeTimeSource;
import tdl.record.sourcecode.content.CopyFromDirectorySourceCodeProvider;
import tdl.record.sourcecode.time.TimeSource;

public class WriterTest {

    @Rule
    public TemporaryFolder sourceFolder = new TemporaryFolder();

    @Rule
    public TemporaryFolder destinationFolder = new TemporaryFolder();

    @Test
    public void run() throws Exception {
        Path output = destinationFolder.newFile("snapshot.bin").toPath();
        Path dirPath = Paths.get("src/test/resources/directory_snapshot/dir1");
        Path sourceDir = sourceFolder.getRoot().toPath();
        FileUtils.copyDirectory(dirPath.toFile(), sourceDir.toFile());

        CopyFromDirectorySourceCodeProvider sourceCodeProvider = new CopyFromDirectorySourceCodeProvider(sourceDir);
        TimeSource timeSource = new FakeTimeSource();
        long timestamp = System.currentTimeMillis() / 1000L;
        try (Writer writer = new Writer(output, sourceCodeProvider, timeSource, timestamp, 5, false)) {
            writer.takeSnapshot();

            appendString(sourceDir, "file1.txt", "\nLOREM");
            writer.takeSnapshot();

            appendString(sourceDir, "file1.txt", "\nIPSUM");
            writer.takeSnapshot();

            appendString(sourceDir, "file1.txt", "\nDOLOR");
            writer.takeSnapshot();

            appendString(sourceDir, "file1.txt", "\nSIT");
            writer.takeSnapshot();
            
            appendString(sourceDir, "file2.txt", "\nLOREM");
            writer.takeSnapshot();
            
            appendString(sourceDir, "file4.txt", "\nIPSUM");
            writer.takeSnapshot();
            
            appendString(sourceDir, "file5.txt", "\nDOLOR");
            writer.takeSnapshot();
        }
    }

    private static void appendString(Path dir, String path, String data) throws IOException {
        FileUtils.writeStringToFile(dir.resolve(path).toFile(), data, Charset.defaultCharset(), true);
    }
}

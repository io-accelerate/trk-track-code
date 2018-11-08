package tdl.record.sourcecode;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import support.TestGeneratedSrcsFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;
import static support.TestUtils.writeFile;

public class ExportSegmentsCommandTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

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
    ), Arrays.asList("tag1", "x", "tag12", "tag3", "tag3"));

    @Test
    public void run() throws IOException {
        Path inputFilePath = recorder.getFilePath();

        {
            Path newFilePath = exportTimestamp(inputFilePath, 0);
            assertTrue(newFilePath.toFile().exists());
        }

        {
            Path newFilePath = exportTimestamp(inputFilePath, 1);
            assertTrue(newFilePath.toFile().exists());
        }

        {
            Path newFilePath = exportTimestamp(inputFilePath, 2);
            assertTrue(newFilePath.toFile().exists());
        }

        {
            Path newFilePath = exportTimestamp(inputFilePath, 3);
            assertTrue(newFilePath.toFile().exists());
        }

        {
            Path newFilePath = exportTag(inputFilePath, "tag12");
            assertTrue(newFilePath.toFile().exists());
        }

        {
            Path newFilePath = exportTag(inputFilePath, "tag3");
            assertTrue(newFilePath.toFile().exists());
        }
    }

    private Path exportTimestamp(Path inputFilePath, int timestamp) throws IOException {
        Path filePath = folder.newFile().toPath();
        ExportSegmentsCommand exportSegmentsCommand = new ExportSegmentsCommand(inputFilePath.toString(), filePath.toString(), timestamp);
        exportSegmentsCommand.run();
        return filePath;
    }

    private Path exportTag(Path inputFilePath, String tag) throws IOException {
        Path filePath = folder.newFile().toPath();
        ExportSegmentsCommand exportSegmentsCommand = new ExportSegmentsCommand(inputFilePath.toString(), filePath.toString(), tag);
        exportSegmentsCommand.run();
        return filePath;
    }

    private String readFile(Path parent, String path) throws IOException {
        Path resolvedPath = parent.resolve(path);
        if (!Files.exists(resolvedPath)) {
            throw new AssertionError("File does not exist: "+resolvedPath);
        }

        File file = resolvedPath.toFile();
        return FileUtils.readFileToString(file, Charset.defaultCharset());
    }
}

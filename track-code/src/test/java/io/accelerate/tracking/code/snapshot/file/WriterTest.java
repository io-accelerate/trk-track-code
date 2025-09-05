package io.accelerate.tracking.code.snapshot.file;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import io.accelerate.tracking.code.test.support.time.FakeTimeSource;
import io.accelerate.tracking.code.content.CopyFromDirectorySourceCodeProvider;
import io.accelerate.tracking.code.time.TimeSource;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class WriterTest {
    private int maximumFileSizeLimitInMB = 2;

    @TempDir
    public Path sourceFolder;

    @TempDir
    public Path destinationFolder;

    @BeforeEach
    public void setUp() throws Exception {
        Path output = destinationFolder.resolve("snapshot.bin");
        Path dirPath = Paths.get("src/test/resources/directory_snapshot/dir1");
        Path sourceDir = sourceFolder;
        FileUtils.copyDirectory(dirPath.toFile(), sourceDir.toFile());

        CopyFromDirectorySourceCodeProvider sourceCodeProvider = new CopyFromDirectorySourceCodeProvider(sourceDir, maximumFileSizeLimitInMB);
        TimeSource timeSource = new FakeTimeSource();
        long timestamp = System.currentTimeMillis() / 1000L;
        try (Writer writer = new Writer(output, sourceCodeProvider, timeSource, timestamp, 5, false)) {
            writer.takeSnapshot();
        }
    }

    @Test
    public void run() throws Exception {
        Path output = destinationFolder.resolve("snapshot.bin");
        Path sourceDir = sourceFolder;

        CopyFromDirectorySourceCodeProvider sourceCodeProvider = new CopyFromDirectorySourceCodeProvider(sourceDir, maximumFileSizeLimitInMB);
        TimeSource timeSource = new FakeTimeSource();
        long timestamp = System.currentTimeMillis() / 1000L;
        try (Writer writer = new Writer(output, sourceCodeProvider, timeSource, timestamp, 5, false)) {
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

        // Additional assertions
        assertTrue(output.toFile().exists(), "Snapshot file should exist");
    }

    private static void appendString(Path dir, String path, String data) throws IOException {
        FileUtils.writeStringToFile(dir.resolve(path).toFile(), data, Charset.defaultCharset(), true);
    }
}

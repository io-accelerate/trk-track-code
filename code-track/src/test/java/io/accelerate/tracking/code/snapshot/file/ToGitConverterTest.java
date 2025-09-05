package io.accelerate.tracking.code.snapshot.file;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import io.accelerate.tracking.code.test.support.time.FakeTimeSource;
import io.accelerate.tracking.code.content.CopyFromDirectorySourceCodeProvider;
import io.accelerate.tracking.code.time.TimeSource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ToGitConverterTest {

    private int maximumFileSizeLimitInMB = 2; // in MB

    @TempDir
    public Path folder;

    @Test
    public void runOnEmptyDirectory() throws Exception {
        Path original = Paths.get("src/test/resources/directory_snapshot/dir1");
        File snapshotFile = folder.resolve("snapshot").toFile();
        Path workDir = Files.createTempDirectory(folder, "workDir");
        Path gitDir = Files.createTempDirectory(folder, "gitDir");

        FileUtils.copyDirectory(original.toFile(), workDir.toFile());
        createRandomSnapshot(snapshotFile.toPath(), workDir);

        //System.out.println(Hex.encodeHex(FileUtils.readFileToByteArray(snapshotFile)));
        ToGitConverter converter = new ToGitConverter(snapshotFile.toPath(), gitDir);
        converter.convert();
        //FileUtils.copyFile(snapshotFile, new File("/tmp/test.srcs"));
        //FileUtils.copyDirectory(gitDir.toFile(), new File("/tmp/test"));
        assertTrue(gitDir.resolve(".git").toFile().exists());
    }

    @Test
    public void runOnExistingGitRepo() throws Exception {
        //TODO
    }

    private void createRandomSnapshot(Path snapshotFile, Path workDir) throws Exception {
        CopyFromDirectorySourceCodeProvider sourceCodeProvider = new CopyFromDirectorySourceCodeProvider(workDir, maximumFileSizeLimitInMB);
        TimeSource timeSource = new FakeTimeSource();
        long timestamp = System.currentTimeMillis() / 1000L;
        try (Writer writer = new Writer(snapshotFile, sourceCodeProvider, timeSource, timestamp, 5, false)) {
            writer.takeSnapshot();
            Thread.sleep(1000);

            appendString(workDir, "file1.txt", "Test 1");
            writer.takeSnapshot();
            Thread.sleep(1000);

            appendString(workDir, "file2.txt", "Test 2");
            writer.takeSnapshot();
            //Thread.sleep(1000);

            appendString(workDir, "file1.txt", "Test 3");
            writer.takeSnapshot();
            //Thread.sleep(1000);

            appendString(workDir, "file2.txt", "Test 4");
            writer.takeSnapshot();
            Thread.sleep(1000);

            appendString(workDir, "file1.txt", "Test 1");
            writer.takeSnapshot();
            //Thread.sleep(1000);

            appendString(workDir, "file3.txt", "Test 4");
            writer.takeSnapshot();
            //Thread.sleep(1000);

            appendString(workDir, "file1.txt", "Test 4");
            writer.takeSnapshot();
        }
    }

    private static void appendString(Path dir, String path, String data) throws IOException {
        FileUtils.writeStringToFile(dir.resolve(path).toFile(), data, Charset.defaultCharset(), true);
    }
}

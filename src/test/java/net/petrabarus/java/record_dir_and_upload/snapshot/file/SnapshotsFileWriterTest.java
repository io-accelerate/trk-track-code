package net.petrabarus.java.record_dir_and_upload.snapshot.file;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class SnapshotsFileWriterTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void run() throws IOException {
        Path output = Paths.get("tmp/snapshot.bin");
        Path dirPath = Paths.get("src/test/resources/directory_snapshot/dir1");
        Path tmpDir = folder.getRoot().toPath();
        FileUtils.copyDirectory(dirPath.toFile(), tmpDir.toFile());

        try (SnapshotsFileWriter writer = new SnapshotsFileWriter(output, tmpDir, false)) {
            writer.takeSnapshot();

            appendString(tmpDir, "file1.txt", "\nLOREM");
            writer.takeSnapshot();

            appendString(tmpDir, "file1.txt", "\nIPSUM");
            writer.takeSnapshot();

            appendString(tmpDir, "file1.txt", "\nDOLOR");
            writer.takeSnapshot();

            appendString(tmpDir, "file1.txt", "\nSIT");
            writer.takeSnapshot();
            
            appendString(tmpDir, "file2.txt", "\nLOREM");
            writer.takeSnapshot();
            
            appendString(tmpDir, "file4.txt", "\nIPSUM");
            writer.takeSnapshot();
            
            appendString(tmpDir, "file5.txt", "\nDOLOR");
            writer.takeSnapshot();
        }
    }

    private static void appendString(Path dir, String path, String data) throws IOException {
        FileUtils.writeStringToFile(dir.resolve(path).toFile(), data, Charset.defaultCharset(), true);
    }
}

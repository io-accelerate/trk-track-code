package net.petrabarus.java.record_dir_and_upload.snapshot.file;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ToGitConverterTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void run() throws IOException, GitAPIException, InterruptedException {
        Path original = Paths.get("src/test/resources/directory_snapshot/dir1");
        File snapshotFile = folder.newFile();
        Path workDir = folder.newFolder().toPath();
        Path gitDir = folder.newFolder().toPath();

        FileUtils.copyDirectory(original.toFile(), workDir.toFile());
        createRandomSnapshot(snapshotFile.toPath(), workDir);

        //System.out.println(Hex.encodeHex(FileUtils.readFileToByteArray(snapshotFile)));
        ToGitConverter converter = new ToGitConverter(snapshotFile.toPath(), gitDir);
        converter.convert();
        FileUtils.copyDirectory(gitDir.toFile(), new File("/tmp/test"));
        assertTrue(gitDir.resolve(".git").toFile().exists());
    }

    private void createRandomSnapshot(Path snapshotFile, Path workDir) throws IOException, InterruptedException {
        try (SnapshotsFileWriter writer = new SnapshotsFileWriter(snapshotFile, workDir, false)) {
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

package tdl.record.sourcecode.snapshot;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import tdl.record.sourcecode.content.CopyFromDirectorySourceCodeProvider;

public class SnapshotRecorderTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void takeSnapshot() throws IOException {
        Path directory = Paths.get("./src/test/resources/diff/test1/dir1/");
        Path tmpDir = folder.getRoot().toPath();
        FileUtils.copyDirectory(directory.toFile(), tmpDir.toFile());
        SnapshotRecorder recorder = new SnapshotRecorder(new CopyFromDirectorySourceCodeProvider(tmpDir), 5);

        Snapshot snapshot1 = recorder.takeSnapshot();
        assertTrue(snapshot1 instanceof KeySnapshot);
        printSnapshot(snapshot1);

        appendString(tmpDir, "file1.txt", "\ndata1");
        Snapshot snapshot2 = recorder.takeSnapshot();
        assertTrue(snapshot2 instanceof PatchSnapshot);
        printSnapshot(snapshot2);

        appendString(tmpDir, "file2.txt", "\nLOREM");
        Snapshot snapshot3 = recorder.takeSnapshot();
        assertTrue(snapshot3 instanceof PatchSnapshot);
        printSnapshot(snapshot3);

        appendString(tmpDir, "subdir1/file1.txt", "\nIPSUM");
        Snapshot snapshot4 = recorder.takeSnapshot();
        assertTrue(snapshot4 instanceof PatchSnapshot);
        printSnapshot(snapshot4);
        
        appendString(tmpDir, "subdir1/file1.txt", "SIT");
        Snapshot snapshot5 = recorder.takeSnapshot();
        assertTrue(snapshot5 instanceof PatchSnapshot);
        printSnapshot(snapshot5);
        
        appendString(tmpDir, "subdir1/file1.txt", "AMENT");
        Snapshot snapshot6 = recorder.takeSnapshot();
        assertTrue(snapshot6 instanceof KeySnapshot);
        printSnapshot(snapshot6);
    }

    private static void printSnapshot(Snapshot snapshot) {
        //System.out.println(new String(snapshot.getData()));
        //do nothing
    }

    private static void appendString(Path dir, String path, String data) throws IOException {
        FileUtils.writeStringToFile(dir.resolve(path).toFile(), data, Charset.defaultCharset(), true);
    }
}

package tdl.record.sourcecode.snapshot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import tdl.record.sourcecode.test.FileTestHelper;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class KeySnapshotTest {

    @Rule
    public TemporaryFolder temporary = new TemporaryFolder();

    @Test
    public void takeAndRestoreSnapshot() throws IOException {
        Path directory = Paths.get("./src/test/resources/diff/test1/dir1/");
        
        KeySnapshot snapshot = KeySnapshot.takeSnapshotFromDirectory(directory);
        File tempDir = temporary.newFolder();
        tempDir.mkdir();
        snapshot.restoreSnapshot(tempDir.toPath());
        
        assertTrue(FileTestHelper.isDirectoryEquals(directory, tempDir.toPath()));
    }
}

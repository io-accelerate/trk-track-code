package net.petrabarus.java.record_dir_and_upload.snapshot.git;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class KeySnapshotTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void test() throws IOException {
        SnapshotRecorder recorder = new SnapshotRecorder(folder.getRoot().toPath());
        Git git = recorder.getGit();

        File tmpFile1 = folder.newFile();
        FileUtils.write(tmpFile1, "Lorem Ipsum", StandardCharsets.US_ASCII);
        KeySnapshot snapshot1 = new KeySnapshot(git, recorder.getDirectory());

        String content = new String(snapshot1.asBytes());
        assertTrue(content.contains(tmpFile1.getName()));
        
        File tmpFile2 = folder.newFile();
        FileUtils.write(tmpFile2, "Lorem Ipsum", StandardCharsets.US_ASCII);
        KeySnapshot snapshot2 = new KeySnapshot(git, recorder.getDirectory());

        String content2 = new String(snapshot2.asBytes());
        assertTrue(content2.contains(tmpFile2.getName()));
    }
}

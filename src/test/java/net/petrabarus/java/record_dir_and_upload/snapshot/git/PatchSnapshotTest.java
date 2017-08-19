package net.petrabarus.java.record_dir_and_upload.snapshot.git;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.Git;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class PatchSnapshotTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void test() throws IOException {
        SnapshotRecorder recorder = new SnapshotRecorder(folder.getRoot().toPath());
        Git git = recorder.getGit();
        
        KeySnapshot snapshot1 = new KeySnapshot(git, recorder.getDirectory());
        
        File tmpFile1 = folder.newFile();
        FileUtils.write(tmpFile1, "Lorem Ipsum", StandardCharsets.US_ASCII);
        PatchSnapshot snapshot2 = new PatchSnapshot(git);
        String diff = new String(snapshot2.asBytes(), StandardCharsets.UTF_16);
        assertTrue(diff.contains("Lorem Ipsum"));
        
        
        File tmpFile2 = folder.newFile();
        FileUtils.write(tmpFile2, "Dolor Sit Amet", StandardCharsets.US_ASCII);
        PatchSnapshot snapshot3 = new PatchSnapshot(git);
        String diff2 = new String(snapshot3.asBytes(), StandardCharsets.UTF_16);
        assertTrue(diff2.contains("Dolor Sit Amet"));
    }
}

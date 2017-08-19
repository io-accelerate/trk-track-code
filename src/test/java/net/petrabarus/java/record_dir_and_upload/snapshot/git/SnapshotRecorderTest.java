package net.petrabarus.java.record_dir_and_upload.snapshot.git;

import java.io.File;
import net.petrabarus.java.record_dir_and_upload.git.SnapshotRecorder;
import java.nio.file.Path;
import org.eclipse.jgit.util.FileUtils;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class SnapshotRecorderTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void initWontCreateGitDirectory() throws Exception {
        Path gitDir;
        try (SnapshotRecorder recorder = new SnapshotRecorder(folder.getRoot().toPath())) {
            gitDir = recorder.getGitDirectory();
            assertFalse(gitDir.resolve(".git").toFile().exists());
            assertTrue(gitDir.toFile().exists());
        }
        assertFalse(gitDir.resolve(".git").toFile().exists());
        assertFalse(gitDir.toFile().exists());
    }

    @Test
    public void initWontTouchGitDirectory() throws Exception {
        Path gitDir;
        Path directory = folder.getRoot().toPath();
        File existingGitDir = directory.resolve(".git").toFile();
        FileUtils.mkdir(existingGitDir);
        try (SnapshotRecorder recorder = new SnapshotRecorder(directory)) {
            gitDir = recorder.getGitDirectory();
            assertTrue(existingGitDir.exists());
            assertTrue(gitDir.toFile().exists());
        }
        assertTrue(existingGitDir.exists());
        assertFalse(gitDir.toFile().exists());
    }
}

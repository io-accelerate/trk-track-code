package net.petrabarus.java.record_dir_and_upload.snapshot.git;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import org.apache.commons.io.FileUtils;
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
        Path directory = folder.getRoot().toPath();
        File existingGitDir = directory.resolve(".git").toFile();
        assertFalse(existingGitDir.exists());
        try (SnapshotRecorder recorder = new SnapshotRecorder(directory)) {
            gitDir = recorder.getGitDirectory();
            assertTrue(existingGitDir.exists());
            assertTrue(existingGitDir.isFile());
            //assertTrue(isDirEmpty(directory));
            assertTrue(gitDir.toFile().exists());
        }
        assertFalse(existingGitDir.exists());
        assertFalse(gitDir.toFile().exists());
    }

    @Test
    public void initWontTouchGitDirectory() throws Exception {
        Path gitDir;
        Path directory = folder.getRoot().toPath();
        File existingGitDir = directory.resolve(".git").toFile();
        FileUtils.forceMkdir(existingGitDir);
        assertTrue(existingGitDir.exists());

        assertFalse(isDirEmpty(directory));
        try (SnapshotRecorder recorder = new SnapshotRecorder(directory)) {
            gitDir = recorder.getGitDirectory();
            assertTrue(existingGitDir.exists());
            assertTrue(gitDir.toFile().exists());
        }
        assertTrue(existingGitDir.exists());
        assertFalse(gitDir.toFile().exists());
    }

    private static boolean isDirEmpty(final Path directory) throws IOException {
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
            return !dirStream.iterator().hasNext();
        }
    }
}

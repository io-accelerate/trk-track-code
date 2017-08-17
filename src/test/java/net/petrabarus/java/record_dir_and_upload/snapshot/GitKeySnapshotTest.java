package net.petrabarus.java.record_dir_and_upload.snapshot;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.jgit.api.errors.GitAPIException;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class GitKeySnapshotTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void shouldBeAbleToCreate() throws GitAPIException {
        Path path = folder.getRoot().toPath();

        Path gitPath = folder.getRoot().toPath().resolve(".git");
        assertFalse(Files.exists(gitPath));

        GitKeySnapshot snapshot = new GitKeySnapshot(path);

        assertTrue(Files.exists(gitPath));

        snapshot.commitFiles();
        assertNotNull(snapshot.getCommitHash());
    }
}

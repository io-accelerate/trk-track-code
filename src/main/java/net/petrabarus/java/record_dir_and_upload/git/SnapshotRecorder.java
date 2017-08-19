package net.petrabarus.java.record_dir_and_upload.git;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

public class SnapshotRecorder implements AutoCloseable {

    private final Path directory;

    private Git git;

    private Path gitDirectory;

    private boolean shouldTakeKeySnapshot;

    public SnapshotRecorder(Path directory) {
        this.directory = directory;
        initGit();
    }

    private void initGit() {
        try {
            gitDirectory = Files.createTempDirectory("tempfiles");
            git = Git.init()
                    .setDirectory(directory.toFile())
                    .setGitDir(gitDirectory.toFile())
                    .call();
        } catch (IOException | GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    public Snapshot takeSnapshot() {
        if (shouldTakeKeySnapshot) {
            return takeKeySnapshot();
        } else {
            return takePatchSnapshot();
        }
    }

    public KeySnapshot takeKeySnapshot() {
        KeySnapshot snapshot = new KeySnapshot(git);
        return snapshot;
    }

    public PatchSnapshot takePatchSnapshot() {
        PatchSnapshot snapshot = new PatchSnapshot(git);
        return snapshot;
    }

    public Path getGitDirectory() {
        return gitDirectory;
    }

    @Override
    public void close() throws Exception {
        git.close();
        FileUtils.deleteDirectory(gitDirectory.toFile());
    }
}

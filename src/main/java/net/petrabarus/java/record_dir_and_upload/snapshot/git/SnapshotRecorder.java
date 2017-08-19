package net.petrabarus.java.record_dir_and_upload.snapshot.git;

import java.io.File;
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

    private boolean doesDotGitExistInTheBeginning;

    public SnapshotRecorder(Path directory) {
        this.directory = directory;
        initGit();
    }

    private void initGit() {
        try {
            gitDirectory = Files.createTempDirectory("tempfiles");
            File dotGitFile = directory.resolve(".git").toFile();
            doesDotGitExistInTheBeginning = dotGitFile.exists();
            git = Git.init()
                    //.setBare(true)
                    .setGitDir(gitDirectory.toFile())
                    .setDirectory(directory.toFile())
                    .call();
        } catch (IOException | GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    public Snapshot takeSnapshot() throws IOException {
        if (shouldTakeKeySnapshot) {
            return takeKeySnapshot();
        } else {
            return takePatchSnapshot();
        }
    }

    public KeySnapshot takeKeySnapshot() throws IOException {
        KeySnapshot snapshot = new KeySnapshot(git, directory);
        return snapshot;
    }

    public PatchSnapshot takePatchSnapshot() {
        PatchSnapshot snapshot = new PatchSnapshot(git);
        return snapshot;
    }

    public Git getGit() {
        return git;
    }

    public Path getDirectory() {
        return directory;
    }

    public Path getGitDirectory() {
        return gitDirectory;
    }

    @Override
    public void close() throws Exception {
        git.close();
        if (!doesDotGitExistInTheBeginning) {
            FileUtils.deleteQuietly(directory.resolve(".git").toFile());
        }
        FileUtils.deleteDirectory(gitDirectory.toFile());
    }
}

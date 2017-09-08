package tdl.record.sourcecode.snapshot;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

public class SnapshotRecorder implements AutoCloseable {

    public static int DEFAULT_SNAPSHOT_STEP = 5;

    protected final Path directory;

    private Path gitDirectory;

    private Git git;

    private Path currentDirectorySnapshot;

    private Path previousDirectorySnapshot;

    private int counter = 0;

    public SnapshotRecorder(Path directory) {
        this.directory = directory;
        initGitDirectory();
    }

    private void initGitDirectory() {
        try {
            File sysTmpDir = FileUtils.getTempDirectory();
            gitDirectory = Files.createTempDirectory(
                    sysTmpDir.toPath(),
                    getClass().getSimpleName()
            );
            git = Git.init().setDirectory(gitDirectory.toFile()).call();
        } catch (IOException | GitAPIException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Path getGitDirectory() {
        return gitDirectory;
    }

    public void syncToGitDirectory() throws IOException {
        FileFilter filter = (file) -> {
            Path relative = directory.relativize(file.toPath());
            return !((file.isDirectory() && relative.equals(".git"))
                    || relative.startsWith(".git/"));
        };
        FileUtils.copyDirectory(directory.toFile(), gitDirectory.toFile(), filter);
    }

    public Snapshot takeSnapshot() throws IOException {
        Snapshot snapshot;
        createCurrentDirectorySnapshot();
        if (shouldTakeSnapshot()) {
            counter = 0;
            snapshot = takeKeySnapshot();
        } else {
            snapshot = takePatchSnapshot();
        }
        counter++;
        moveCurrentDirectoryAsPrevious();
        return snapshot;
    }

    private void createCurrentDirectorySnapshot() throws IOException {
        currentDirectorySnapshot = createTmpDirectory();
        FileUtils.copyDirectory(
                directory.toFile(),
                currentDirectorySnapshot.toFile()
        );
    }

    private void moveCurrentDirectoryAsPrevious() {
        if (previousDirectorySnapshot != null) {
            previousDirectorySnapshot.toFile().deleteOnExit();
        }
        previousDirectorySnapshot = currentDirectorySnapshot;
    }

    private static Path createTmpDirectory() throws IOException {
        return Files.createTempDirectory("tmp", new FileAttribute<?>[]{});
    }

    private boolean shouldTakeSnapshot() {
        return counter % DEFAULT_SNAPSHOT_STEP == 0;
    }

    public KeySnapshot takeKeySnapshot() throws IOException {
        return KeySnapshot.takeSnapshotFromDirectory(currentDirectorySnapshot);
    }

    public PatchSnapshot takePatchSnapshot() throws IOException {
        return PatchSnapshot.takeSnapshotFromDirectories(
                previousDirectorySnapshot,
                currentDirectorySnapshot
        );
    }

    @Override
    public void close() {
//        currentDirectorySnapshot.toFile().deleteOnExit();
//        previousDirectorySnapshot.toFile().deleteOnExit();
        git.close();
        gitDirectory.toFile().deleteOnExit();
    }
}

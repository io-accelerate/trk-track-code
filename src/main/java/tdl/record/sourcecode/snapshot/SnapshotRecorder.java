package tdl.record.sourcecode.snapshot;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import tdl.record.sourcecode.snapshot.helpers.DirectoryDiffUtils;

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
            commitAllChanges();
        } catch (IOException | GitAPIException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Git getGit() {
        return git;
    }

    public Path getGitDirectory() {
        return gitDirectory;
    }

    public void syncToGitDirectory() throws IOException {
        copyToGitDirectory();
        removeDeletedFileInOriginal();
    }

    private void copyToGitDirectory() throws IOException {
        FileFilter filter = (file) -> {
            Path relative = directory.relativize(file.toPath());
            return !((file.isDirectory() && relative.equals(".git"))
                    || relative.startsWith(".git/"));
        };
        FileUtils.copyDirectory(directory.toFile(), gitDirectory.toFile(), filter);
    }

    private void removeDeletedFileInOriginal() {
        List<String> files = DirectoryDiffUtils.getRelativeFilePathList(gitDirectory);
        files.stream().forEach((path) -> {
            if (path.startsWith(".git")) {
                return;
            }
            File file = gitDirectory.resolve(path).toFile();
            boolean isExists = file.exists()
                    && !directory.resolve(path).toFile().exists();
            if (isExists) {
                file.delete();
            }
        });
    }

    public void commitAllChanges() {
        try {
            String message = new Date().toString();
            git.commit()
                    .setMessage(message)
                    .setAll(true)
                    .call();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
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

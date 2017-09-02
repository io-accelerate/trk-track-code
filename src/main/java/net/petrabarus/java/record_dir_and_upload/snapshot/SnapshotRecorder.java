package net.petrabarus.java.record_dir_and_upload.snapshot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import org.apache.commons.io.FileUtils;

public class SnapshotRecorder implements AutoCloseable {

    protected final Path directory;

    private Path currentDirectorySnapshot;

    private Path previousDirectorySnapshot;

    private int counter = 0;

    public SnapshotRecorder(Path directory) {
        this.directory = directory;
    }

    public BaseSnapshot takeSnapshot() throws IOException {
        BaseSnapshot snapshot;
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
        FileUtils.copyDirectory(directory.toFile(), currentDirectorySnapshot.toFile());
    }

    private void moveCurrentDirectoryAsPrevious() {
        previousDirectorySnapshot.toFile().deleteOnExit();
        previousDirectorySnapshot = currentDirectorySnapshot;
    }

    private static Path createTmpDirectory() throws IOException {
        return Files.createTempDirectory("tmp", new FileAttribute<?>[]{});
    }

    private boolean shouldTakeSnapshot() {
        return counter % 5 == 0;
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
        currentDirectorySnapshot.toFile().deleteOnExit();
        previousDirectorySnapshot.toFile().deleteOnExit();
    }
}

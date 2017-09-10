package tdl.record.sourcecode.snapshot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

import tdl.record.sourcecode.content.SourceCodeProvider;

public class SnapshotRecorder implements AutoCloseable {

    protected final SourceCodeProvider sourceCodeProvider;

    private Path currentDirectorySnapshot;

    private Path previousDirectorySnapshot;

    private int counter = 0;
    private int keySnapshotPacing;

    public SnapshotRecorder(SourceCodeProvider sourceCodeProvider, int keySnapshotPacing) {
        this.sourceCodeProvider = sourceCodeProvider;
        this.keySnapshotPacing = keySnapshotPacing;
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
        sourceCodeProvider.retrieveAndSaveTo(currentDirectorySnapshot);
    }

    private void moveCurrentDirectoryAsPrevious() {
        if (previousDirectorySnapshot != null) {
            previousDirectorySnapshot.toFile().deleteOnExit();
        }
        previousDirectorySnapshot = currentDirectorySnapshot;
    }

    private static Path createTmpDirectory() throws IOException {
        return Files.createTempDirectory("tmp");
    }

    private boolean shouldTakeSnapshot() {
        return counter % keySnapshotPacing == 0;
    }

    private KeySnapshot takeKeySnapshot() throws IOException {
        return KeySnapshot.takeSnapshotFromDirectory(currentDirectorySnapshot);
    }

    private PatchSnapshot takePatchSnapshot() throws IOException {
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

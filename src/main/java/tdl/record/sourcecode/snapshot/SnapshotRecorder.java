package tdl.record.sourcecode.snapshot;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import tdl.record.sourcecode.content.SourceCodeProvider;
import tdl.record.sourcecode.snapshot.helpers.DirectoryDiffUtils;
import tdl.record.sourcecode.snapshot.helpers.ExcludeGitDirectoryFileFilter;
import tdl.record.sourcecode.snapshot.helpers.FileHelper;

public class SnapshotRecorder implements AutoCloseable {

    protected final SourceCodeProvider sourceCodeProvider;

    private Git git;

    private Path gitDirectory;

    private int counter = 0;

    private final int keySnapshotPacing;

    public SnapshotRecorder(SourceCodeProvider sourceCodeProvider, int keySnapshotPacing) {
        this.sourceCodeProvider = sourceCodeProvider;
        this.keySnapshotPacing = keySnapshotPacing;
    }

    public void init() throws SnapshotRecorderException {
        initGitDirectory();
    }

    private void initGitDirectory() throws SnapshotRecorderException {
        try {
            File sysTmpDir = FileUtils.getTempDirectory();
            gitDirectory = Files.createTempDirectory(
                    sysTmpDir.toPath(),
                    getClass().getSimpleName()
            );
            git = Git.init().setDirectory(gitDirectory.toFile()).call();
            commitAllChanges();
        } catch (IOException | GitAPIException ex) {
            throw new SnapshotRecorderException(ex);
        }
    }

    public Git getGit() {
        return git;
    }

    public Path getGitDirectory() {
        return gitDirectory;
    }

    public void syncToGitDirectory() throws IOException {
        cleanGitDirectory();
        sourceCodeProvider.retrieveAndSaveTo(gitDirectory);
        FileHelper.deleteEmptyFiles(gitDirectory);
    }

    private void cleanGitDirectory() {
        IOFileFilter filter = new ExcludeGitDirectoryFileFilter(gitDirectory);
        FileUtils.listFiles(gitDirectory.toFile(), filter, TrueFileFilter.INSTANCE)
                .stream()
                .forEach(File::delete);
    }

    public void commitAllChanges() {
        try {
            String message = new Date().toString();
            git.add()
                    .addFilepattern(".")
                    .call();
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

        syncToGitDirectory();
        commitAllChanges();
        if (shouldTakeSnapshot()) {
            counter = 0;
            snapshot = takeKeySnapshot();
        } else {
            snapshot = takePatchSnapshot();
        }
        counter++;
        return snapshot;
    }

    private boolean shouldTakeSnapshot() {
        return counter % keySnapshotPacing == 0;
    }

    private KeySnapshot takeKeySnapshot() throws IOException {
        return KeySnapshot.takeSnapshotFromGit(git);
    }

    private PatchSnapshot takePatchSnapshot() throws IOException {
        return PatchSnapshot.takeSnapshotFromGit(git);
    }

    @Override
    public void close() {
        git.close();
        gitDirectory.toFile().deleteOnExit();
    }
}

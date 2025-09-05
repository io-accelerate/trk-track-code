package io.accelerate.tracking.code.snapshot;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import io.accelerate.tracking.code.content.ExcludeGitDirectoryFileFilter;
import io.accelerate.tracking.code.content.SourceCodeProvider;
import io.accelerate.tracking.code.snapshot.helpers.FileHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

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

    Path getGitDirectory() {
        return gitDirectory;
    }

    SnapshotTypeHint syncToGitDirectory() throws IOException {
        cleanGitDirectory();
        SnapshotTypeHint snapshotTypeHint = sourceCodeProvider.retrieveAndSaveTo(gitDirectory);
        FileHelper.deleteEmptyFiles(gitDirectory);
        return snapshotTypeHint;
    }

    private void cleanGitDirectory() {
        IOFileFilter filter = new ExcludeGitDirectoryFileFilter(gitDirectory);
        //noinspection ResultOfMethodCallIgnored
        FileUtils.listFiles(gitDirectory.toFile(), filter, TrueFileFilter.INSTANCE)
                .forEach(File::delete);
    }

    void commitAllChanges() {
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

        SnapshotTypeHint snapshotTypeHint = syncToGitDirectory();
        commitAllChanges();

        if (decideOnSnapshotType(snapshotTypeHint) == SnapshotTypeHint.PATCH) {
            snapshot = takePatchSnapshot();
        } else {
            snapshot = takeKeySnapshot();
        }
        return snapshot;
    }

    private SnapshotTypeHint decideOnSnapshotType(SnapshotTypeHint hintFromProvider) {
        SnapshotTypeHint snapshotTypeHint = hintFromProvider;
        if (hintFromProvider == SnapshotTypeHint.ANY) {
            if  (counter % keySnapshotPacing == 0) {
                snapshotTypeHint = SnapshotTypeHint.KEY;
            } else {
                snapshotTypeHint = SnapshotTypeHint.PATCH;
            }
        }

        counter++;
        return snapshotTypeHint;
    }

    private KeySnapshot takeKeySnapshot() {
        return KeySnapshot.takeSnapshotFromGit(git);
    }

    private PatchSnapshot takePatchSnapshot() {
        return PatchSnapshot.takeSnapshotFromGit(git);
    }

    public EmptySnapshot takeEmptySnapshot() {
        return EmptySnapshot.takeSnapshotFromGit(git);
    }

    @Override
    public void close() {
        git.close();
        gitDirectory.toFile().deleteOnExit();
    }
}

package net.petrabarus.java.record_dir_and_upload.snapshot.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import net.petrabarus.java.record_dir_and_upload.snapshot.Snapshot;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

public class ToGitConverter {

    private final Path inputFile;

    private final Path outputDir;

    private Git git;

    public ToGitConverter(Path inputFile, Path outputDir) throws IOException {
        this.inputFile = inputFile;
        this.outputDir = outputDir;
        throwExceptionIfOutputDirInvalid();
    }

    public void convert() throws IOException, GitAPIException {
        FileUtils.cleanDirectory(outputDir.toFile());
        initGit();
        SnapshotsFileReader reader = new SnapshotsFileReader(inputFile.toFile());

        while (reader.hasNext()) {
            SnapshotFileSegment segment = reader.next();
            writeDirFromSnapshot(segment);
            commitDirectory(segment.getTimestampAsDate());
        }
    }

    private void writeDirFromSnapshot(SnapshotFileSegment segment) throws IOException {
        //TODO: Check if not corrupt.
        Snapshot snapshot = segment.getSnapshot();
        snapshot.restoreSnapshot(outputDir);
    }

    private void commitDirectory(Date timestamp) throws GitAPIException {
        String message = timestamp.toString();
        git.add().addFilepattern(".").call();
        git.commit().setMessage(message).call();
    }

    private void initGit() throws GitAPIException {
        git = Git.init().setDirectory(outputDir.toFile()).call();
    }

    private void throwExceptionIfOutputDirInvalid() throws IOException {
        File dir = outputDir.toFile();
        if (!dir.exists()) {
            throw new IOException("Directory not found");
        }
        if (!dir.isDirectory()) {
            throw new IOException("No directory found.");
        }
    }
}

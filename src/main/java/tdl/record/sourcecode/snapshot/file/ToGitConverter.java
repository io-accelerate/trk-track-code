package tdl.record.sourcecode.snapshot.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import tdl.record.sourcecode.snapshot.Snapshot;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;

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
            commitDirectory(segment);
        }
    }

    private void writeDirFromSnapshot(SnapshotFileSegment segment) throws IOException {
        //TODO: Check if not corrupt.
        Snapshot snapshot = segment.getSnapshot();
        //snapshot.restoreSnapshot(outputDir);
    }

    private void commitDirectory(SnapshotFileSegment segment) throws GitAPIException {
        Date timestamp = segment.getTimestampAsDate();
        PersonIdent origIdent = new PersonIdent(git.getRepository());
        PersonIdent ident = new PersonIdent(origIdent, timestamp);
        String message = timestamp.toString();
        git.add().addFilepattern(".").call();
        git.commit()
                .setAuthor(ident)
                .setMessage(message)
                .call();
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

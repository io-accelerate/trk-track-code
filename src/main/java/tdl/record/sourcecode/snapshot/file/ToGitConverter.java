package tdl.record.sourcecode.snapshot.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import tdl.record.sourcecode.snapshot.Snapshot;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import tdl.record.sourcecode.snapshot.KeySnapshot;

public class ToGitConverter {

    private final Path inputFile;

    private final Path outputDir;

    private Git git;

    public ToGitConverter(Path inputFile, Path outputDir) throws IOException {
        this.inputFile = inputFile;
        this.outputDir = outputDir;
        throwExceptionIfOutputDirInvalid();
    }

    public void convert() throws Exception {
        FileUtils.cleanDirectory(outputDir.toFile());
        initGit();
        Reader reader = new Reader(inputFile.toFile());

        while (reader.hasNext()) {
            Segment segment = reader.nextSegment();
            writeDirFromSnapshot(segment);
            commitDirectory(segment);
        }
    }

    private void writeDirFromSnapshot(Segment segment) throws Exception {
        //TODO: Check if not corrupt.
        Snapshot snapshot = segment.getSnapshot();
        snapshot.restoreSnapshot(git);
    }

    private void commitDirectory(Segment segment) throws GitAPIException {
        Date timestamp = new Date(segment.getTimestamp() * 1000L);
        PersonIdent origIdent = new PersonIdent(git.getRepository());
        PersonIdent ident = new PersonIdent(origIdent, timestamp);
        String message = timestamp.toString();
        git.add().addFilepattern(".").call();
        deleteMissing(git);
        git.commit()
                .setAuthor(ident)
                .setMessage(message)
                .call();
    }

    private static void deleteMissing(Git git) throws GitAPIException {
        Status status = git.status().call();
        Set<String> deletedFiles = new HashSet<>();
        deletedFiles.addAll(status.getMissing());
        deletedFiles.addAll(status.getRemoved());
        if (!deletedFiles.isEmpty()) {
            RmCommand rm = git.rm();
            deletedFiles.stream().forEach(rm::addFilepattern);
            rm.call();
        }
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
            throw new IOException("No sourceCodeProvider found.");
        }
    }
}

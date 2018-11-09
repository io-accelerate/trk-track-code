package tdl.record.sourcecode.snapshot.file;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import tdl.record.sourcecode.snapshot.Snapshot;
import tdl.record.sourcecode.snapshot.helpers.GitHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class ToGitConverter {

    private final Path inputFile;

    private final Path outputDir;

    private Git git;

    private ProgressListener listener;
    private boolean stopOnErrors;

    private final TagManager tagManager;

    @FunctionalInterface
    public interface ProgressListener {
        void commitSegment(Segment segment);

    }

    public ToGitConverter(Path inputFile, Path outputDir, ProgressListener listener, boolean stopOnErrors) throws IOException {
        this.inputFile = inputFile;
        this.outputDir = outputDir;
        this.listener = listener;
        this.stopOnErrors = stopOnErrors;
        this.tagManager = new TagManager();
        throwExceptionIfOutputDirInvalid();
    }

    public ToGitConverter(Path inputFile, Path outputDir) throws IOException {
        this(inputFile, outputDir, createDefaultListener(), true);
    }

    public void convert() throws Exception {
        initGit();
        Reader reader = new Reader(inputFile.toFile());

        while (reader.hasNext()) {
            processSegment(reader);
        }
    }

    private void processSegment(Reader reader) throws Exception {
        try {
            Header header = reader.getFileHeader();
            Segment segment = reader.nextSegment();
            listener.commitSegment(segment);
            writeDirFromSnapshot(segment);
            commitDirectory(header, segment);
        } catch (Exception e) {
            if (stopOnErrors) {
                throw e;
            } else {
                System.err.println(e.getMessage());
            }
        }
    }

    private void writeDirFromSnapshot(Segment segment) throws Exception {
        //TODO: Check if not corrupt.
        Snapshot snapshot = segment.getSnapshot();
        snapshot.restoreSnapshot(git);
    }

    private void commitDirectory(Header header, Segment segment) throws GitAPIException {
        Date timestamp = new Date((header.getTimestamp() + segment.getTimestampSec()) * 1000L);
        PersonIdent origIdent = new PersonIdent(git.getRepository());
        PersonIdent ident = new PersonIdent(origIdent, timestamp);
        String message = timestamp.toString();
        git.add().addFilepattern(".").call();
        deleteMissing(git);
        git.commit()
                .setAuthor(ident)
                .setMessage(message)
                .call();

        if (segment.hasTag()) {
            git.tag()
                    .setTagger(ident)
                    .setName(tagManager.asUniqueTag(segment.getTag()))
                    .call();
        }
    }

    private static void deleteMissing(Git git) throws GitAPIException {
        Status status = git.status().call();
        Set<String> deletedFiles = new HashSet<>();
        deletedFiles.addAll(status.getMissing());
        deletedFiles.addAll(status.getRemoved());
        if (!deletedFiles.isEmpty()) {
            RmCommand rm = git.rm();
            deletedFiles.forEach(rm::addFilepattern);
            rm.call();
        }
    }

    private void initGit() throws GitAPIException, IOException {
        if (!GitHelper.isGitDirectory(outputDir)) {
            git = Git.init().setDirectory(outputDir.toFile()).call();
        } else {
            git = Git.open(outputDir.toFile());
            tagManager.addExisting(GitHelper.getTags(git));
        }
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

    private static ProgressListener createDefaultListener() {
        return (segment) -> {
            //do nothing.
        };
    }
}

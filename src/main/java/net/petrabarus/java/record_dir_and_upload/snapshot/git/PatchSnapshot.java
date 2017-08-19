package net.petrabarus.java.record_dir_and_upload.snapshot.git;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

public class PatchSnapshot extends Snapshot {

    private String patch;

    public PatchSnapshot(Git git) {
        super(git);
        generateDiff();
    }

    private void generateDiff() {
        try {
            Iterable<RevCommit> commits = git.log().setMaxCount(2).all().call();
            Iterator<RevCommit> iterator = commits.iterator();
            RevCommit second = iterator.next();
            RevCommit first = iterator.next();
            patch = getDiffText(git, first, second);
        } catch (IOException | GitAPIException ex) {
            Logger.getLogger(PatchSnapshot.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public byte[] asBytes() {
        return patch.getBytes(StandardCharsets.UTF_16);
    }

    public static String getDiffText(Git git, RevCommit prev, RevCommit next) throws IOException, GitAPIException {
        CanonicalTreeParser oldTreeIterator = getTreeParser(git, prev);
        CanonicalTreeParser newTreeIterator = getTreeParser(git, next);
        try (OutputStream outputStream = new ByteArrayOutputStream();
                DiffFormatter formatter = new DiffFormatter(outputStream)) {
            formatter.setRepository(git.getRepository());
            formatter.format(oldTreeIterator, newTreeIterator);
            return outputStream.toString();
        }
    }

    public static CanonicalTreeParser getTreeParser(Git git, RevCommit commit) throws IOException {
        ObjectReader reader = git.getRepository().newObjectReader();
        return new CanonicalTreeParser(null, reader, commit.getTree());
    }

}

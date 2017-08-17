package net.petrabarus.java.record_dir_and_upload.snapshot;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

public abstract class GitSnapshot {

    protected final Path directory;

    protected final Git git;

    public GitSnapshot(Path dir) throws GitAPIException {
        this.directory = dir;
        this.git = loadOrInit();
    }

    protected Git loadOrInit() throws GitAPIException {
        return Git.init()
                .setDirectory(directory.toFile())
                .call();
    }

    public String getCommitHash() {
        try {
            Iterable<RevCommit> log = git.log().setMaxCount(1).call();
            Iterator<RevCommit> iterator = log.iterator();
            if (!iterator.hasNext()) {
                throw new RuntimeException("Cannot find commit");
            }
            RevCommit commit = iterator.next();
            return commit.name();
        } catch (GitAPIException ex) {
            Logger.getLogger(GitKeySnapshot.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
}

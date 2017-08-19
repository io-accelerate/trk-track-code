package net.petrabarus.java.record_dir_and_upload.snapshot.git;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

abstract public class Snapshot {

    protected final Git git;

    public Snapshot(Git git) {
        this.git = git;
        addFilesAndCommit();
    }

    private void addFilesAndCommit() {
        try {
            git.add().addFilepattern(".").call();
            String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            git.commit().setMessage("Commit " + timestamp).call();
        } catch (GitAPIException ex) {
            Logger.getLogger(Snapshot.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    abstract public byte[] asBytes();
}

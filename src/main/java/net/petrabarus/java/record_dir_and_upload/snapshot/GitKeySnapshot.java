package net.petrabarus.java.record_dir_and_upload.snapshot;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

public class GitKeySnapshot extends GitSnapshot {

    public GitKeySnapshot(Path directory) throws GitAPIException {
        super(directory);
    }

    public void commitFiles() {
        try {
            git.add().addFilepattern(".").call();
            String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            git.commit().setMessage("Commit " + timestamp).call();
        } catch (GitAPIException ex) {
            //Do nothing
        }
    }

    public byte[] asBytes() {
        return null;
    }

}

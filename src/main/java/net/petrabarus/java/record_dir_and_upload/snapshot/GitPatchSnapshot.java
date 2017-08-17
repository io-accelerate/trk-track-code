package net.petrabarus.java.record_dir_and_upload.snapshot;

import java.nio.file.Path;
import org.eclipse.jgit.api.errors.GitAPIException;

public class GitPatchSnapshot extends GitSnapshot {

    public GitPatchSnapshot(Path dir) throws GitAPIException {
        super(dir);
    }

}

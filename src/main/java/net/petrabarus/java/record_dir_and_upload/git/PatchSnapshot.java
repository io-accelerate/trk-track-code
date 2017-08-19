package net.petrabarus.java.record_dir_and_upload.git;

import org.eclipse.jgit.api.Git;

public class PatchSnapshot extends Snapshot {

    public PatchSnapshot(Git git) {
        super(git);
    }

    @Override
    public byte[] asBytes() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}

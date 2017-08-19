package net.petrabarus.java.record_dir_and_upload.git;

import org.eclipse.jgit.api.Git;

abstract public class Snapshot {

    protected final Git git;

    public Snapshot(Git git) {
        this.git = git;
    }

    abstract public byte[] asBytes();
}

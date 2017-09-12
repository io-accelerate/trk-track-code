package tdl.record.sourcecode.snapshot;

import org.eclipse.jgit.api.Git;

abstract public class Snapshot {

    protected byte[] data;

    public byte[] getData() {
        return data;
    }

    abstract public void restoreSnapshot(Git git) throws Exception;
}

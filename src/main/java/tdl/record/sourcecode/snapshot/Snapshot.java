package tdl.record.sourcecode.snapshot;

import org.eclipse.jgit.api.Git;

abstract public class Snapshot {

    private SnapshotType type;

    Snapshot(SnapshotType type) {
        this.type = type;
    }

    public SnapshotType getType() {
        return type;
    }

    protected byte[] data;

    public byte[] getData() {
        return data;
    }

    abstract public void restoreSnapshot(Git git) throws Exception;
}

package tdl.record.sourcecode.snapshot;

import org.eclipse.jgit.api.Git;

public class EmptySnapshot extends Snapshot {
    private static final EmptySnapshot INSTANCE = new EmptySnapshot();

    private EmptySnapshot() {
        super(SnapshotType.EMPTY);
    }

    static EmptySnapshot takeSnapshotFromGit(Git git) {
        return INSTANCE;
    }

    public static Snapshot createSnapshotFromBytes(byte[] data) {
        return INSTANCE;
    }

    @Override
    public void restoreSnapshot(Git git) {
        //Nothing to do, this is an empty snapshot
    }
}

package tdl.record.sourcecode.snapshot;

import java.io.IOException;
import java.nio.file.Path;
import org.eclipse.jgit.api.Git;

abstract public class Snapshot {

    protected byte[] data;

    public byte[] getData() {
        return data;
    }

    abstract public void restoreSnapshot(Git git) throws Exception;
}

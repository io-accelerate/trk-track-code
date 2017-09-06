package net.petrabarus.java.record_dir_and_upload.snapshot;

import java.io.IOException;
import java.nio.file.Path;

abstract public class Snapshot {

    protected byte[] data;

    public byte[] getData() {
        return data;
    }

    abstract public void restoreSnapshot(Path destinationDirectory) throws IOException;
}

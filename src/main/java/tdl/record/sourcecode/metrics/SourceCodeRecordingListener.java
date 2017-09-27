package tdl.record.sourcecode.metrics;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public interface SourceCodeRecordingListener {

    void notifyRecordingStart(Path destinationPath);

    void notifySnapshotStart(long timestamp, TimeUnit unit);

    void notifySnapshotEnd(long timestamp, TimeUnit unit);

    void notifyRecordingEnd();
}

package tdl.record.sourcecode.snapshot.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import tdl.record.sourcecode.content.SourceCodeProvider;
import tdl.record.sourcecode.snapshot.KeySnapshot;
import tdl.record.sourcecode.snapshot.Snapshot;
import tdl.record.sourcecode.snapshot.SnapshotRecorder;
import org.apache.commons.io.IOUtils;

public final class SnapshotsFileWriter implements AutoCloseable {

    private final Charset CHARSET = StandardCharsets.US_ASCII;

    private final File outputFile;

    private final SourceCodeProvider sourceCodeProvider;

    private final FileOutputStream outputStream;

    private final SnapshotRecorder recorder;

    public SnapshotsFileWriter(Path outputPath, SourceCodeProvider sourceCodeProvider, int keySnapshotPacing, boolean append) throws IOException {
        this.outputFile = outputPath.toFile();
        this.sourceCodeProvider = sourceCodeProvider;
        outputStream = new FileOutputStream(outputFile, append);
        recorder = new SnapshotRecorder(sourceCodeProvider, keySnapshotPacing);
    }

    public void takeSnapshot() {
        try {
            Snapshot snapshot = recorder.takeSnapshot();
            //try (ByteArrayOutputStream buff = createSnapshotAndStoreToByteArray()) {
            SnapshotFileSegment segment = new SnapshotFileSegment();
            segment.type = (snapshot instanceof KeySnapshot) ? SnapshotFileSegment.TYPE_KEY : SnapshotFileSegment.TYPE_PATCH;
            segment.timestamp = getTimestamp();
            segment.setData(snapshot.getData());
            IOUtils.write(segment.asBytes(), outputStream);
        } catch (IOException ex) {
            Logger.getLogger(SnapshotsFileWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private int getTimestamp() {
        Long unixTimestamp = System.currentTimeMillis() / 1000L;
        return unixTimestamp.intValue();
    }

    @Override
    public void close() {
        IOUtils.closeQuietly(outputStream);
    }

}

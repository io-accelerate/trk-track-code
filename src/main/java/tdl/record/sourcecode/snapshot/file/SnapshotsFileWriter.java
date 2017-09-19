package tdl.record.sourcecode.snapshot.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Hex;

import tdl.record.sourcecode.content.SourceCodeProvider;
import tdl.record.sourcecode.snapshot.KeySnapshot;
import tdl.record.sourcecode.snapshot.Snapshot;
import tdl.record.sourcecode.snapshot.SnapshotRecorder;
import org.apache.commons.io.IOUtils;
import tdl.record.sourcecode.time.TimeSource;

public final class SnapshotsFileWriter implements AutoCloseable {

    private final Charset CHARSET = StandardCharsets.US_ASCII;

    private final File outputFile;

    private final SourceCodeProvider sourceCodeProvider;

    private TimeSource timeSource;

    private final long recordedTimestamp;

    private final FileOutputStream outputStream;

    private final SnapshotRecorder recorder;

    public SnapshotsFileWriter(
            Path outputPath,
            SourceCodeProvider sourceCodeProvider,
            TimeSource timeSource,
            long recordedTimestamp,
            int keySnapshotPacing,
            boolean append
    ) throws IOException {
        this.outputFile = outputPath.toFile();
        this.sourceCodeProvider = sourceCodeProvider;
        this.timeSource = timeSource;
        this.recordedTimestamp = recordedTimestamp;
        outputStream = new FileOutputStream(outputFile, append);
        recorder = new SnapshotRecorder(sourceCodeProvider, keySnapshotPacing);

        if (outputFile.length() == 0) { //new file
            writeHeader();
        }
    }

    private void writeHeader() {
        try {
            SnapshotFileHeader header = new SnapshotFileHeader(recordedTimestamp);
            byte[] data = header.asBytes();
            IOUtils.write(data, outputStream);
        } catch (IOException ex) {
            Logger.getLogger(SnapshotsFileWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void takeSnapshot() {
        try {
            Snapshot snapshot = recorder.takeSnapshot();
            //try (ByteArrayOutputStream buff = createSnapshotAndStoreToByteArray()) {
            SnapshotFileSegment segment = new SnapshotFileSegment();
            segment.type = (snapshot instanceof KeySnapshot) ? SnapshotFileSegment.TYPE_KEY : SnapshotFileSegment.TYPE_PATCH;
            segment.timestamp = TimeUnit.NANOSECONDS.toSeconds(timeSource.currentTimeNano());
            segment.setData(snapshot.getData());
            IOUtils.write(segment.asBytes(), outputStream);
        } catch (IOException ex) {
            Logger.getLogger(SnapshotsFileWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void close() {
        IOUtils.closeQuietly(outputStream);
    }

}

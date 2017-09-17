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

import tdl.record.sourcecode.content.SourceCodeProvider;
import tdl.record.sourcecode.snapshot.KeySnapshot;
import tdl.record.sourcecode.snapshot.Snapshot;
import tdl.record.sourcecode.snapshot.SnapshotRecorder;
import org.apache.commons.io.IOUtils;
import tdl.record.sourcecode.time.TimeSource;

public final class Writer implements AutoCloseable {

    private final File outputFile;

    private final SourceCodeProvider sourceCodeProvider;

    private final TimeSource timeSource;

    private final long recordedTimestamp;

    private final FileOutputStream outputStream;

    private final SnapshotRecorder recorder;

    public Writer(
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
            Header header = new Header();
            header.setTimestamp(recordedTimestamp);
            byte[] data = header.asBytes();
            IOUtils.write(data, outputStream);
        } catch (IOException ex) {
            Logger.getLogger(Writer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void takeSnapshot() {
        try {
            Snapshot snapshot = recorder.takeSnapshot();
            int type = (snapshot instanceof KeySnapshot) ? Segment.TYPE_KEY : Segment.TYPE_PATCH;
            long timestamp = TimeUnit.NANOSECONDS.toSeconds(timeSource.currentTimeNano());
            byte[] data = snapshot.getData();
            Segment segment = new Segment();
            segment.setType(type);
            segment.setTimestamp(timestamp);
            segment.setData(data);
            segment.generateFromData();
            IOUtils.write(segment.asBytes(), outputStream);
        } catch (IOException ex) {
            Logger.getLogger(Writer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void close() {
        IOUtils.closeQuietly(outputStream);
    }

}

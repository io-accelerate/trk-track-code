package tdl.record.sourcecode.snapshot.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import tdl.record.sourcecode.content.SourceCodeProvider;
import tdl.record.sourcecode.snapshot.*;
import org.apache.commons.io.IOUtils;
import tdl.record.sourcecode.time.TimeSource;

public final class Writer implements AutoCloseable {

    private final File outputFile;

    private final TimeSource timeSource;

    private final long recordedTimestamp;

    private final FileOutputStream outputStream;

    private final SnapshotRecorder recorder;

    private final TagManager tagManager;

    public Writer(
            Path outputPath,
            SourceCodeProvider sourceCodeProvider,
            TimeSource timeSource,
            long recordedTimestamp,
            int keySnapshotPacing,
            boolean append
    ) throws IOException, SnapshotRecorderException {
        this.outputFile = outputPath.toFile();
        this.timeSource = timeSource;
        this.recordedTimestamp = recordedTimestamp;
        outputStream = new FileOutputStream(outputFile, append);
        recorder = new SnapshotRecorder(sourceCodeProvider, keySnapshotPacing);
        recorder.init();
        if (outputFile.length() == 0) { //new file
            writeHeader();
        }
        tagManager = new TagManager();
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

    void takeSnapshot() {
        takeSnapshotWithTag("");
    }

    public void takeSnapshotWithTag(String tag) {
        try {
            Segment segment = createSnapshotFromRecorder();
            if (TagManager.isTag(tag)) {
                segment.setTag(tagManager.asValidTag(tag));
            }
            IOUtils.write(segment.asBytes(), outputStream);
        } catch (IOException ex) {
            Logger.getLogger(Writer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Segment createSnapshotFromRecorder() throws IOException {
        Snapshot snapshot = recorder.takeSnapshot();
        int type = (snapshot instanceof KeySnapshot) ? Segment.TYPE_KEY : Segment.TYPE_PATCH;
        long timestampSec = TimeUnit.NANOSECONDS.toSeconds(timeSource.currentTimeNano());
        byte[] data = snapshot.getData();
        Segment segment = new Segment();
        segment.setType(type);
        segment.setTimestampSec(timestampSec);
        segment.setData(data);
        segment.generateFromData();
        return segment;
    }

    @Override
    public void close() {
        IOUtils.closeQuietly(outputStream);
    }

}

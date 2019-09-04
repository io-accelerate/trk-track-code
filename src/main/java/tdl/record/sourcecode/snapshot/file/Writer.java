package tdl.record.sourcecode.snapshot.file;

import org.apache.commons.io.IOUtils;
import tdl.record.sourcecode.content.SourceCodeProvider;
import tdl.record.sourcecode.snapshot.*;
import tdl.record.sourcecode.time.TimeSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

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

    private void writeHeader() throws SnapshotRecorderException {
        try {
            Header header = new Header();
            header.setTimestamp(recordedTimestamp);
            byte[] data = header.asBytes();
            IOUtils.write(data, outputStream);
        } catch (IOException ex) {
            throw new SnapshotRecorderException(ex);
        }
    }

    void takeSnapshot() throws SnapshotRecorderException {
        takeSnapshotWithTag("");
    }

    public void takeSnapshotWithTag(String tag) throws SnapshotRecorderException{
        try {
            Segment segment = createSnapshotFromRecorder();
            if (TagManager.isTag(tag)) {
                segment.setTag(tagManager.asValidTag(tag));
            }
            IOUtils.write(segment.asBytes(), outputStream);
        } catch (IOException ex) {
            throw new SnapshotRecorderException(ex);
        }
    }

    private Segment createSnapshotFromRecorder() throws IOException {
        Snapshot snapshot = recorder.takeSnapshot();
        long timestampSec = TimeUnit.NANOSECONDS.toSeconds(timeSource.currentTimeNano());
        byte[] data = snapshot.getData();
        Segment segment = new Segment();
        segment.setType(snapshot.getType());
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

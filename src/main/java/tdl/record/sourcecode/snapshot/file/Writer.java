package tdl.record.sourcecode.snapshot.file;

import org.apache.commons.io.IOUtils;
import tdl.record.sourcecode.content.SourceCodeProvider;
import tdl.record.sourcecode.snapshot.Snapshot;
import tdl.record.sourcecode.snapshot.SnapshotRecorder;
import tdl.record.sourcecode.snapshot.SnapshotRecorderException;
import tdl.record.sourcecode.time.TimeSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
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

    public Snapshot takeSnapshot() throws SnapshotRecorderException {
        try {
            return recorder.takeSnapshot();
        } catch (IOException e) {
            throw new SnapshotRecorderException(e);
        }
    }

    public void writeSnapshotWithTags(Snapshot snapshot, List<String> tags) throws SnapshotRecorderException {
        // Ensure that the tags have at least one element
        List<String> tagsToWrite;
        if (tags.size() > 0) {
            tagsToWrite = tags;
        } else {
            tagsToWrite = Collections.singletonList("");
        }

        // Write snapshots
        Snapshot currentSnapshot = snapshot;
        for (String tag : tagsToWrite) {
            try {
                Segment segment = createSegmentFromSnapshot(currentSnapshot);
                if (TagManager.isTag(tag)) {
                    segment.setTag(tagManager.asValidTag(tag));
                }
                IOUtils.write(segment.asBytes(), outputStream);
            } catch (IOException ex) {
                throw new SnapshotRecorderException(ex);
            }
            currentSnapshot = recorder.takeEmptySnapshot();
        }
    }

    private Segment createSegmentFromSnapshot(Snapshot snapshot) {
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

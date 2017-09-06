package net.petrabarus.java.record_dir_and_upload.snapshot.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.petrabarus.java.record_dir_and_upload.snapshot.KeySnapshot;
import net.petrabarus.java.record_dir_and_upload.snapshot.Snapshot;
import net.petrabarus.java.record_dir_and_upload.snapshot.SnapshotRecorder;
import org.apache.commons.io.IOUtils;

public final class SnapshotsFileWriter implements AutoCloseable {

    private final Charset CHARSET = StandardCharsets.US_ASCII;

    private final File outputFile;

    private final Path dirPath;

    private final FileOutputStream outputStream;

    private final SnapshotRecorder recorder;

    public SnapshotsFileWriter(Path outputPath, Path dirPath, boolean append) throws IOException {
        this.outputFile = outputPath.toFile();
        this.dirPath = dirPath;
        outputStream = new FileOutputStream(outputFile, append);
        recorder = new SnapshotRecorder(dirPath);
        if (!append || !isValidFile(outputFile)) {
            initFile();
        }
    }

    private boolean isValidFile(File file) {
        return (file.exists() && file.length() > 0);
    }

    public void initFile() throws IOException {
        //TODO: Magic number
        IOUtils.write("TEST", outputStream, CHARSET);
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

    public int getTimestamp() {
        Long unixTimestamp = System.currentTimeMillis() / 1000L;
        return unixTimestamp.intValue();
    }

    @Override
    public void close() {
        IOUtils.closeQuietly(outputStream);
    }

}

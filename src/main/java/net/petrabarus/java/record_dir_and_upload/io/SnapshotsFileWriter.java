package net.petrabarus.java.record_dir_and_upload.io;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.petrabarus.java.record_dir_and_upload.snapshot.DirectorySnapshot;
import net.petrabarus.java.record_dir_and_upload.snapshot.Snapshot;
import org.apache.commons.io.IOUtils;

public final class SnapshotsFileWriter implements AutoCloseable {

    private final Charset CHARSET = StandardCharsets.US_ASCII;

    private final File outputFile;

    private final Path dirPath;

    private final FileOutputStream outputStream;

    public SnapshotsFileWriter(Path outputPath, Path dirPath, boolean append) throws IOException {
        this.outputFile = outputPath.toFile();
        this.dirPath = dirPath;
        outputStream = new FileOutputStream(outputFile, append);

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
        try (ByteArrayOutputStream buff = createSnapshotAndStoreToByteArray()) {
            Snapshot snapshot = new Snapshot();
            snapshot.type = Snapshot.TYPE_KEY;
            snapshot.timestamp = getTimestamp();
            snapshot.setData(buff.toByteArray());
            IOUtils.write(snapshot.asBytes(), outputStream);
        } catch (IOException ex) {
            Logger.getLogger(SnapshotsFileWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ByteArrayOutputStream createSnapshotAndStoreToByteArray() throws IOException {
        ByteArrayOutputStream buff = new ByteArrayOutputStream();
        DirectorySnapshot snapshot = new DirectorySnapshot(dirPath, buff);
        snapshot.compress();
        return buff;
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

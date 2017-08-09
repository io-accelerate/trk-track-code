package net.petrabarus.java.record_dir_and_upload.snapshot;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public final class SnapshotsFileWriter implements AutoCloseable {

    private final Charset CHARSET = StandardCharsets.US_ASCII;

    private final File outputFile;

    private final Path dirPath;

    private final FileOutputStream outputStream;

    public SnapshotsFileWriter(Path path, Path dirPath, boolean append) throws IOException {
        this.outputFile = path.toFile();
        this.dirPath = dirPath;
        outputStream = new FileOutputStream(outputFile, append);

        if (!append) {
            initFile();
        }
    }

    public void initFile() throws IOException {
        //TODO: Magic number
        IOUtils.write("TEST", outputStream, CHARSET);
    }

    public void snapshot() {
        try (ByteArrayOutputStream buff = createSnapshotAndStoreToByteArray()) {
            writeIntAsBytes(buff.size());
            writeIntAsBytes(getTimestamp());
            IOUtils.write(buff.toByteArray(), outputStream);
        } catch (IOException ex) {
            Logger.getLogger(SnapshotsFileWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void writeIntAsBytes(int i) throws IOException {
        byte[] bytes = ByteHelper.littleEndianIntToByteArray(i, 4);
        IOUtils.write(bytes, outputStream);
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

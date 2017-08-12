package net.petrabarus.java.record_dir_and_upload.snapshot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import org.apache.commons.io.IOUtils;

public class SnapshotsFileReader implements Iterator<SnapshotsFileReader.Snapshot>, AutoCloseable {

    public static class Snapshot {

        public int timestamp;

        public int size;

        public byte[] data;

        public Snapshot(int timestamp, int size, byte[] data) {
            this.timestamp = timestamp;
            this.size = size;
            this.data = data;
        }

    }

    private final File file;

    private final FileInputStream inputStream;

    public SnapshotsFileReader(File file) throws FileNotFoundException, IOException {
        this.file = file;
        this.inputStream = new FileInputStream(file);
        skipMagicNumber();
    }

    private void skipMagicNumber() throws IOException {
        inputStream.skip(4);
    }

    @Override
    public boolean hasNext() {
        try {
            return inputStream.available() > 0;
        } catch (IOException ex) {
            return false;
        }
    }

    @Override
    public Snapshot next() {
        try {
            int size = readIntegerBytes();
            int timestamp = readIntegerBytes();
            byte[] data = new byte[size];
            inputStream.read(data);
            return new Snapshot(timestamp, size, data);
        } catch (IOException ex) {
            return null;
        }
    }

    @Override
    public void remove() {
        Iterator.super.remove();
    }

    @Override
    public void forEachRemaining(Consumer<? super Snapshot> action) {
        Iterator.super.forEachRemaining(action); //To change body of generated methods, choose Tools | Templates.
    }

    public void reset() throws IOException {
        inputStream.getChannel().position(0);
        skipMagicNumber();
    }

    public void skip() throws IOException {
        int size = readIntegerBytes();
        inputStream.skip(4);
        inputStream.skip(size);
    }

    private int readIntegerBytes() throws IOException {
        byte[] bytes = new byte[4];
        inputStream.read(bytes);
        return ByteHelper.byteArrayToLittleEndianInt(bytes);
    }

    public List<Date> getDates() {
        //TODO: need to do manual skip
        List<Date> list = new ArrayList<>();
        this.forEachRemaining((Snapshot snapshot) -> {
            list.add(new Date(snapshot.timestamp * 1000L));
        });
        return list;
    }

    @Override
    public void close() {
        IOUtils.closeQuietly(inputStream);
    }

    public File getFile() {
        return file;
    }

}

package net.petrabarus.java.record_dir_and_upload.snapshot.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import net.petrabarus.java.record_dir_and_upload.snapshot.Snapshot;
import org.apache.commons.io.IOUtils;

public class SnapshotsFileReader implements Iterator<Snapshot>, AutoCloseable {

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
            byte[] header = readHeader();
            Snapshot snapshot = Snapshot.createFromHeaderBytes(header);
            byte[] data = readData((int) snapshot.size);
            snapshot.data = data;

            return snapshot;
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
        byte[] header = readHeader();
        Snapshot snapshot = Snapshot.createFromHeaderBytes(header);
        inputStream.skip(snapshot.size);
    }

    public byte[] readHeader() throws IOException {
        return readData(Snapshot.HEADER_SIZE);
    }

    public byte[] readData(int size) throws IOException {
        byte[] data = new byte[size];
        inputStream.read(data);
        return data;
    }

    public List<Date> getDates() {
        //TODO: need to do manual skip
        List<Date> list = new ArrayList<>();
        this.forEachRemaining((Snapshot snapshot) -> {
            list.add(new Date(snapshot.timestamp * 1000L));
        });
        return list;
    }

    public List<Snapshot> getSnapshots() {
        List<Snapshot> list = new ArrayList<>();
        this.forEachRemaining(list::add);
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

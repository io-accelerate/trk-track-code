package tdl.record.sourcecode.snapshot.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import tdl.record.sourcecode.snapshot.KeySnapshot;

public class Reader implements Iterator<Segment>, AutoCloseable {

    private final File file;

    private final RandomAccessFile randomAccessFile;

    private Header fileHeader;

    public Reader(File file) throws FileNotFoundException, IOException {
        this.file = file;
        this.randomAccessFile = new RandomAccessFile(file, "r");
        reset();
    }

    @Override
    public boolean hasNext() {
        try {
            return randomAccessFile.getFilePointer() < randomAccessFile.length() - 1;
        } catch (IOException ex) {
            return false;
        }
    }

    @Override
    public Segment next() {
        try {
            Segment segment = readHeaderAndCreateFileSegment();

            segment.setData(readData((int) segment.getSize()));

            return segment;
        } catch (IOException ex) {
            //TODO raise as a proper exception
            return null;
        }
    }

    private Header readFileHeader() throws IOException {
        byte[] header = readData(Header.SIZE);
        return Header.fromBytes(header);
    }

    private Segment readHeaderAndCreateFileSegment() throws IOException {
        long address = randomAccessFile.getFilePointer();
        byte[] header = readHeader();
        Segment segment = Segment.createFromHeaderBytes(header);
        segment.setAddress(address);
        return segment;
    }

    @Override
    public void remove() {
        Iterator.super.remove();
    }

    @Override
    public void forEachRemaining(Consumer<? super Segment> action) {
        Iterator.super.forEachRemaining(action); //To change body of generated methods, choose Tools | Templates.
    }

    public Date getStartTimestamp() throws IOException {
        reset();
        Segment firstSegment = next();
        Date firstTimestamp = firstSegment.getTimestampAsDate();
        reset();
        return firstTimestamp;
    }

    public void reset() throws IOException {
        randomAccessFile.getChannel().position(0);
        fileHeader = readFileHeader();
    }


    public Header getFileHeader() {
        return fileHeader;
    }


    public void skip() throws IOException {
        skipAndReturnHeader();
    }

    public byte[] readHeader() throws IOException {
        return readData(Segment.HEADER_SIZE);
    }

    public byte[] readData(int size) throws IOException {
        byte[] data = new byte[size];
        randomAccessFile.read(data);
        return data;
    }

    /**
     * @param index Inclusive.
     * @return
     * @throws java.lang.Exception
     */
    public List<Segment> getReplayableSnapshotSegmentsUntil(int index) throws Exception {
        Segment snapshot = getSnapshotAt(index);
        if (snapshot.getSnapshot() instanceof KeySnapshot) {
            return Arrays.asList(new Segment[]{snapshot});
        }
        int first = getFirstKeySnapshotBefore(index);
        return getSnapshotSegmentsByRange(first, index + 1);
    }

    /**
     * @param start inclusive.
     * @param end exclusive.
     * @return
     * @throws java.io.IOException
     */
    public List<Segment> getSnapshotSegmentsByRange(int start, int end) throws IOException {
        List<Segment> list = new ArrayList<>();
        reset();
        int index = 0;
        while (index < end) {
            if (index >= start && index < end) {
                Segment snapshot = next();
                list.add(snapshot);
            } else {
                skip();
            }
            index++;
        }
        reset();
        return list;
    }

    public int getFirstKeySnapshotBefore(int index) throws IOException {
        int keyIndex = 0;
        int start = 0;
        reset();
        while (start < index) {
            Segment snapshot = skipAndReturnHeader();
            if (snapshot.getSnapshot() instanceof KeySnapshot) {
                keyIndex = start;
            }
            start++;
        }
        reset();
        return keyIndex;
    }

    private Segment skipAndReturnHeader() throws IOException {
        Segment segment = readHeaderAndCreateFileSegment();
        randomAccessFile.skipBytes((int) segment.getSize());
        return segment;
    }

    public List<Date> getDates() throws IOException {
        //TODO: need to do manual skip
        List<Date> list = new ArrayList<>();
        reset();
        this.forEachRemaining((Segment snapshot) -> {
            list.add(new Date(snapshot.getTimestamp() * 1000L));
        });
        return list;
    }

    public Segment getSnapshotAt(int index) throws IOException {
        int start = 0;
        reset();
        while (start < index) {
            skip();
            start++;
        }
        Segment snapshot = next();
        reset();
        return snapshot;
    }

    public List<Segment> getSnapshots() throws IOException {
        List<Segment> list = new ArrayList<>();
        reset();
        forEachRemaining(list::add);
        return list;
    }

    public int getIndexBeforeOrEqualsTimestamp(long timestamp) throws IOException {
        int index = 0;
        reset();
        do {
            Segment segment = skipAndReturnHeader();
            if (segment.getTimestamp() > timestamp) {
                index--;
                break;
            }
            index++;
        } while (hasNext());
        reset();
        return index;
    }

    @Override
    public void close() {
        try {
            randomAccessFile.close();
        } catch (IOException ex) {
            //
        }
    }

    public File getFile() {
        return file;
    }

}

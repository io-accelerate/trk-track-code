package tdl.record.sourcecode.snapshot.file;

import java.io.File;
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
import tdl.record.sourcecode.snapshot.helpers.ByteHelper;

public class Reader implements Iterator<Segment>, AutoCloseable {

    private static class ReadHeader extends Header {

        private final RandomAccessFile file;

        public ReadHeader(RandomAccessFile file) {
            this.file = file;
        }

        public boolean isValid() {
            try {
                byte[] magicBytes = new byte[MAGIC_BYTES.length];
                file.read(magicBytes, 0, MAGIC_BYTES.length);
                return Arrays.equals(magicBytes, MAGIC_BYTES);
            } catch (IOException ex) {
                return false;
            }
        }

        @Override
        public long getTimestamp() {
            try {
                byte[] bytes = new byte[8];
                long lastPosition = file.getFilePointer();
                file.seek(6);
                file.read(bytes, 0, 8);
                file.seek(lastPosition);
                return ByteHelper.byteArrayToLittleEndianLong(bytes);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        public void next() throws IOException {
            file.seek(SIZE);
        }
    }

    private static class ReadSegment extends Segment {

        private final RandomAccessFile file;

        private final long address;

        public ReadSegment(RandomAccessFile file, long address) {
            this.file = file;
            this.address = address;
        }

        private byte[] readByte(int offset, int length) {
            try {
                byte[] bytes = new byte[length];
                long lastPosition = file.getFilePointer();
                file.seek(address + offset);
                file.read(bytes, 0, length);
                file.seek(lastPosition);
                return bytes;
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public int getType() {
            return getTypeByteBytes(readByte(0, MAGIC_BYTES_KEY.length));
        }

        @Override
        public long getTimestamp() {
            return ByteHelper.byteArrayToLittleEndianLong(readByte(6, 8));
        }

        @Override
        public long getSize() {
            return ByteHelper.byteArrayToLittleEndianLong(readByte(14, 8));
        }

        @Override
        public byte[] getChecksum() {
            return readByte(22, 20);
        }

        @Override
        public byte[] getData() {
            long size = getSize();
            return readByte(42, (int) size);
        }

        @Override
        public long getAddress() {
            return address;
        }

        public void next() {
            try {
                int size = (int) getSize();
                file.seek(address + HEADER_SIZE);
                file.skipBytes(size);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

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
            Segment segment = readSegment();
            return segment;
        } catch (IOException ex) {
            //TODO raise as a proper exception
            return null;
        }
    }

    private Header readFileHeader() throws IOException {
        ReadHeader header = new ReadHeader(randomAccessFile);
        if (!header.isValid()) {
            throw new IOException("Cannot parse header");
        }
        header.next();
        return header;
    }

    private Segment readSegment() throws IOException {
        long address = randomAccessFile.getFilePointer();
        ReadSegment segment = new ReadSegment(randomAccessFile, address);
        segment.next();
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

    public final void reset() throws IOException {
        randomAccessFile.seek(0);
        fileHeader = readFileHeader();
    }

    public long getFilePointer() {
        try {
            return randomAccessFile.getFilePointer();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Header getFileHeader() {
        return fileHeader;
    }

    public void skip() throws IOException {
        readSegment();
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
            Segment snapshot = readSegment();
            if (snapshot.getSnapshot() instanceof KeySnapshot) {
                keyIndex = start;
            }
            start++;
        }
        reset();
        return keyIndex;
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
            Segment segment = readSegment();
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

package tdl.record.sourcecode.snapshot.file;

import tdl.record.sourcecode.snapshot.KeySnapshot;
import tdl.record.sourcecode.snapshot.SnapshotType;
import tdl.record.sourcecode.snapshot.helpers.ByteHelper;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.function.Consumer;

public class Reader implements Iterator<Integer>, AutoCloseable {

    private final File file;

    private final RandomAccessFile randomAccessFile;

    private Header fileHeader;

    public Reader(File file) throws IOException {
        this.file = file;
        this.randomAccessFile = new RandomAccessFile(file, "r");
        reset();
    }

    private byte[] readBytesFromOffset(int offset, int length) throws IOException {
        byte[] bytes = new byte[length];
        long lastPosition = randomAccessFile.getFilePointer();
        randomAccessFile.seek(offset);
        randomAccessFile.read(bytes, 0, length);
        randomAccessFile.seek(lastPosition);
        return bytes;
    }

    private long readLong(int offset) throws IOException {
        return ByteHelper.byteArrayToLittleEndianLong(readBytesFromOffset(offset, 8));
    }

    private byte[] readBytes(int length) throws IOException {
        byte[] bytes = new byte[length];
        randomAccessFile.read(bytes, 0, length);
        return bytes;
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
    public Integer next() {
        try {
            long address = randomAccessFile.getFilePointer();
            long size = readSegmentSizeFromAddress((int) address);
            int skip = Segment.HEADER_SIZE + (int) size;
            randomAccessFile.skipBytes(skip);
            return (int) address;
        } catch (IOException ex) {
            //TODO raise as a proper exception
            return null;
        }
    }

    public Segment nextSegment() throws IOException {
        int address = next();
        return readSegmentByAddress(address);
    }

    private Header readFileHeader() throws IOException {
        Header header = new Header();
        header.setMagicBytes(readBytesFromOffset(0, Header.MAGIC_BYTES.length));
        header.setTimestamp(readLong(6));
        if (!header.isValid()) {
            throw new IOException("Cannot parse header");
        }
        randomAccessFile.seek(Header.SIZE);
        return header;
    }

    private long readSegmentSizeFromAddress(int address) throws IOException {
        return readLong(address + Segment.SIZE_ADDRESS);
    }

    @Override
    public void remove() {
        Iterator.super.remove();
    }

    @Override
    public void forEachRemaining(Consumer<? super Integer> action) {
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

    public void skip() {
        next();
    }

    public List<Integer> getSegmentAddresses() throws IOException {
        reset();
        List<Integer> list = new ArrayList<>();
        forEachRemaining(list::add);
        return list;
    }

    /**
     * @param index Inclusive.
     */
    public List<Segment> getReplayableSnapshotSegmentsUntil(int index) throws Exception {
        Segment snapshot = getSnapshotAt(index);
        if (snapshot.getSnapshot() instanceof KeySnapshot) {
            return Collections.singletonList(snapshot);
        }
        int first = getFirstKeySnapshotBefore(index);
        return getSnapshotSegmentsByRange(first, index + 1);
    }

    /**
     * @param start inclusive.
     * @param end exclusive.
     */
    public List<Segment> getSnapshotSegmentsByRange(int start, int end) throws IOException {
        List<Segment> list = new ArrayList<>();
        reset();
        int index = 0;
        while (index < end) {
            if (index >= start) {
                Segment snapshot = nextSegment();
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
            Segment snapshot = nextSegment();
            if (snapshot.getSnapshot() instanceof KeySnapshot) {
                keyIndex = start;
            }
            start++;
        }
        reset();
        return keyIndex;
    }

    public Segment readSegmentByAddress(int address) throws IOException {
        long lastPosition = randomAccessFile.getFilePointer();
        randomAccessFile.seek(address);
        Segment segment = new Segment();
        segment.setAddress(address);
        segment.setType(SnapshotType.fromMagicBytes(readBytes(SnapshotType.MAGIC_BYTES_LENGTH)));
        segment.setTimestampSec(ByteHelper.byteArrayToLittleEndianInt(readBytes(Segment.LONG_SIZE)));
        segment.setSize(ByteHelper.byteArrayToLittleEndianInt(readBytes(Segment.LONG_SIZE)));
        segment.setTag(asString(readBytes(Segment.TAG_SIZE)));
        segment.setChecksum(readBytes(Segment.CHECKSUM_SIZE));
        segment.setData(readBytes((int) segment.getSize()));
        if (segment.isChecksumMismatch()) {
            throw new IOException("Checksum mismatch");
        }
        randomAccessFile.seek(lastPosition);
        return segment;
    }

    private String asString(byte[] bytes) {
        int indexOfZero = Arrays.binarySearch(bytes, (byte) 0);

        int newLength = bytes.length;
        if (indexOfZero >= 0) {
            newLength = indexOfZero;
        }

        return new String(bytes, 0, newLength).trim();
    }

    private Segment generateEmptySegment() {
        Segment segment = new Segment();
        segment.setType(SnapshotType.EMPTY);
        byte[] bytes = new byte[0];
        segment.setData(bytes);
        segment.setTimestampSec(0);
        segment.generateFromData();
        return segment;
    }

    public Segment getSnapshotAt(int index) throws IOException {
        int start = 0;
        reset();
        while (start < index) {
            skip();
            start++;
        }
        int address = next();
        Segment snapshot = readSegmentByAddress(address);
        reset();
        return snapshot;
    }

    public List<Segment> getSegments() throws IOException {
        List<Segment> list = new ArrayList<>();
        reset();
        forEachRemaining((address) -> {
            Segment segment;
            try {
                segment = readSegmentByAddress(address);
            } catch (IOException ex) {
                //Do nothing
                segment = generateEmptySegment();
            }
            list.add(segment);
        });
        return list;
    }

    public int getIndexBeforeForTag(String tag) throws IOException {
        int index = 0;
        reset();
        do {
            Segment segment = nextSegment();
            if (Objects.equals(segment.getTag(), tag)) {
                break;
            }
            index++;
        } while (hasNext());
        reset();
        return index;
    }

    public int getIndexBeforeOrEqualsTimestamp(long timestamp) throws IOException {
        int index = 0;
        reset();
        do {
            Segment segment = nextSegment();
            if (segment.getTimestampSec() > timestamp) {
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

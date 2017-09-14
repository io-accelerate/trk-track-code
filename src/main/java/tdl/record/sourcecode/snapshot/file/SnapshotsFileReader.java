package tdl.record.sourcecode.snapshot.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import org.apache.commons.io.IOUtils;
import tdl.record.sourcecode.snapshot.KeySnapshot;

public class SnapshotsFileReader implements Iterator<SnapshotFileSegment>, AutoCloseable {

    private final File file;

    private final FileInputStream inputStream;

    public SnapshotsFileReader(File file) throws FileNotFoundException, IOException {
        this.file = file;
        this.inputStream = new FileInputStream(file);
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
    public SnapshotFileSegment next() {
        try {
            byte[] header = readHeader();
            SnapshotFileSegment snapshot = SnapshotFileSegment.createFromHeaderBytes(header);
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
    public void forEachRemaining(Consumer<? super SnapshotFileSegment> action) {
        Iterator.super.forEachRemaining(action); //To change body of generated methods, choose Tools | Templates.
    }

    public Date getStartTimestamp() throws IOException {
        reset();
        SnapshotFileSegment firstSegment = next();
        Date firstTimestamp = firstSegment.getTimestampAsDate();
        reset();
        return firstTimestamp;
    }

    public void reset() throws IOException {
        inputStream.getChannel().position(0);
    }

    public void skip() throws IOException {
        skipAndReturnHeader();
    }

    public byte[] readHeader() throws IOException {
        return readData(SnapshotFileSegment.HEADER_SIZE);
    }

    public byte[] readData(int size) throws IOException {
        byte[] data = new byte[size];
        inputStream.read(data);
        return data;
    }

    /**
     * @param index Inclusive.
     * @return
     * @throws java.lang.Exception
     */
    public List<SnapshotFileSegment> getReplayableSnapshotSegmentsUntil(int index) throws Exception {
        SnapshotFileSegment snapshot = getSnapshotsAt(index);
        if (snapshot.getSnapshot() instanceof KeySnapshot) {
            return Arrays.asList(new SnapshotFileSegment[]{snapshot});
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
    public List<SnapshotFileSegment> getSnapshotSegmentsByRange(int start, int end) throws IOException {
        List<SnapshotFileSegment> list = new ArrayList<>();
        reset();
        int index = 0;
        while (index < end) {
            SnapshotFileSegment snapshot = skipAndReturnHeader();
            if (index >= start && index < end) {
                list.add(snapshot);
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
            SnapshotFileSegment snapshot = skipAndReturnHeader();
            if (snapshot.getSnapshot() instanceof KeySnapshot) {
                keyIndex = start;
            }
            start++;
        }
        reset();
        return keyIndex;
    }

    private SnapshotFileSegment skipAndReturnHeader() throws IOException {
        byte[] header = readHeader();
        SnapshotFileSegment snapshot = SnapshotFileSegment.createFromHeaderBytes(header);

        inputStream.skip(snapshot.size);
        return snapshot;
    }

    public List<Date> getDates() throws IOException {
        //TODO: need to do manual skip
        List<Date> list = new ArrayList<>();
        reset();
        this.forEachRemaining((SnapshotFileSegment snapshot) -> {
            list.add(new Date(snapshot.timestamp * 1000L));
        });
        return list;
    }

    public SnapshotFileSegment getSnapshotsAt(int index) throws IOException {
        int start = 0;
        reset();
        while (start < index) {
            skip();
            start++;
        }
        SnapshotFileSegment snapshot = next();
        reset();
        return snapshot;
    }

    public List<SnapshotFileSegment> getSnapshots() throws IOException {
        List<SnapshotFileSegment> list = new ArrayList<>();
        reset();
        forEachRemaining(list::add);
        return list;
    }

    public int getIndexBeforeTime(Date date) throws IOException {
        int index = 0;
        reset();
        do {
            SnapshotFileSegment segment = skipAndReturnHeader();
            Date timestamp = segment.getTimestampAsDate();
            if (timestamp.after(date) || timestamp.equals(date)) {
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
        IOUtils.closeQuietly(inputStream);
    }

    public File getFile() {
        return file;
    }

}

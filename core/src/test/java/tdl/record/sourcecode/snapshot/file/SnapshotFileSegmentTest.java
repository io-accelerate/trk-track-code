package tdl.record.sourcecode.snapshot.file;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class SnapshotFileSegmentTest {

    @Test
    public void setData() {
        String string = "Lorem Ipsum Dolor Sit Amet";
        byte[] stringBytes = string.getBytes(StandardCharsets.US_ASCII);
        SnapshotFileSegment snapshot = new SnapshotFileSegment();
        snapshot.type = SnapshotFileSegment.TYPE_KEY;
        snapshot.setData(stringBytes);
        assertEquals(snapshot.size, stringBytes.length);
    }

    @Test
    public void asBytes() {
        String string = "Lorem Ipsum Dolor Sit Amet";
        byte[] stringBytes = string.getBytes(StandardCharsets.US_ASCII);

        SnapshotFileSegment snapshot = new SnapshotFileSegment();
        snapshot.type = SnapshotFileSegment.TYPE_KEY;
        snapshot.setData(stringBytes);
        snapshot.timestamp = new Date().getTime();

        byte[] bytes = snapshot.asBytes();
        assertEquals(SnapshotFileSegment.HEADER_SIZE + stringBytes.length, bytes.length);
    }

    @Test
    public void getHeaderAsBytesAndCreateFromHeaderBytes() {
        String string = "Lorem Ipsum Dolor Sit Amet";
        byte[] stringBytes = string.getBytes(StandardCharsets.US_ASCII);

        SnapshotFileSegment snapshot = new SnapshotFileSegment();
        snapshot.type = SnapshotFileSegment.TYPE_KEY;
        snapshot.setData(stringBytes);
        snapshot.timestamp = new Date().getTime();

        byte[] header = snapshot.getHeaderAsBytes();
        assertEquals(SnapshotFileSegment.HEADER_SIZE, header.length);

        SnapshotFileSegment snapshot2 = SnapshotFileSegment.createFromHeaderBytes(header);
        assertEquals(snapshot2.type, snapshot.type);
        Assert.assertArrayEquals(snapshot2.checksum, snapshot.checksum);
        assertEquals(snapshot2.size, snapshot.size);
        assertEquals(snapshot2.timestamp, snapshot.timestamp);
    }

    @Test
    public void generateChecksum() {
        String string = "Lorem Ipsum Dolor Sit Amet";
        String expected = "887a5b6d458b496633a01451ae7370025f4e7ceb";
        byte[] stringBytes = string.getBytes(StandardCharsets.US_ASCII);
        SnapshotFileSegment snapshot = new SnapshotFileSegment();
        snapshot.data = stringBytes;
        byte[] checksum = snapshot.generateChecksum();
        assertEquals(checksum.length, 20);
        String checksumString = new String(Hex.encodeHex(checksum));
        assertEquals(checksumString.length(), 40);
        assertEquals(expected, checksumString);
    }

    @Test
    public void isDataValidShouldReturnTrue() throws DecoderException {
        String dataString = "Lorem Ipsum Dolor Sit Amet";
        String checksumString = "887a5b6d458b496633a01451ae7370025f4e7ceb";
        byte[] stringBytes = dataString.getBytes(StandardCharsets.US_ASCII);
        byte[] checksumBytes = Hex.decodeHex(checksumString.toCharArray());
        SnapshotFileSegment snapshot = new SnapshotFileSegment();
        snapshot.data = stringBytes;
        snapshot.checksum = checksumBytes;
        assertTrue(snapshot.isDataValid());
    }

    @Test
    public void isDataValidShouldReturnFalse() throws DecoderException {
        String dataString = "Lorem Ipsum Dolor Sit Amet";
        String checksumString = "887a5b6d458b496633a01451ae7370025f4f7ceb";
        byte[] stringBytes = dataString.getBytes(StandardCharsets.US_ASCII);
        byte[] checksumBytes = Hex.decodeHex(checksumString.toCharArray());
        SnapshotFileSegment snapshot = new SnapshotFileSegment();
        snapshot.data = stringBytes;
        snapshot.checksum = checksumBytes;
        assertFalse(snapshot.isDataValid());
    }
}

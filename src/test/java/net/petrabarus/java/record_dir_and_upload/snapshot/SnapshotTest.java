package net.petrabarus.java.record_dir_and_upload.snapshot;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class SnapshotTest {

    @Test
    public void asBytes() {
        String string = "Lorem Ipsum Dolor Sit Amet";
        byte[] stringBytes = string.getBytes(StandardCharsets.US_ASCII);

        Snapshot snapshot = new Snapshot();
        snapshot.data = stringBytes;
        snapshot.checksum = snapshot.generateChecksum();
        snapshot.type = Snapshot.TYPE_KEY;
        snapshot.size = stringBytes.length;
        snapshot.timestamp = new Date().getTime();

        byte[] bytes = snapshot.asBytes();
        assertEquals(Snapshot.HEADER_SIZE + stringBytes.length, bytes.length);
    }
    
    @Test
    public void getHeaderAsBytesAndCreateFromHeaderBytes() {
        String string = "Lorem Ipsum Dolor Sit Amet";
        byte[] stringBytes = string.getBytes(StandardCharsets.US_ASCII);

        Snapshot snapshot = new Snapshot();
        snapshot.data = stringBytes;
        snapshot.checksum = snapshot.generateChecksum();
        snapshot.type = Snapshot.TYPE_KEY;
        snapshot.size = stringBytes.length;
        snapshot.timestamp = new Date().getTime();

        byte[] header = snapshot.getHeaderAsBytes();
        assertEquals(Snapshot.HEADER_SIZE, header.length);
        
        Snapshot snapshot2 = Snapshot.createFromHeaderBytes(header);
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
        Snapshot snapshot = new Snapshot();
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
        Snapshot snapshot = new Snapshot();
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
        Snapshot snapshot = new Snapshot();
        snapshot.data = stringBytes;
        snapshot.checksum = checksumBytes;
        assertFalse(snapshot.isDataValid());
    }
}

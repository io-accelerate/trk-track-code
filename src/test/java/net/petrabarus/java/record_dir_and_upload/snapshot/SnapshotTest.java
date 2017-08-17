package net.petrabarus.java.record_dir_and_upload.snapshot;

import java.nio.charset.StandardCharsets;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class SnapshotTest {

    @Test
    public void asBytes() {

    }

    @Test
    public void generateChecksumFromData() {
        String string = "Lorem Ipsum Dolor Sit Amet";
        String expected = "887a5b6d458b496633a01451ae7370025f4e7ceb";
        byte[] stringBytes = string.getBytes(StandardCharsets.US_ASCII);
        Snapshot snapshot = new Snapshot();
        snapshot.data = stringBytes;
        byte[] checksum = snapshot.generateChecksumFromData();
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

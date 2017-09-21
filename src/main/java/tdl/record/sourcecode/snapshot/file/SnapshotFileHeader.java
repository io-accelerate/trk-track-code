package tdl.record.sourcecode.snapshot.file;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import org.apache.commons.codec.binary.Hex;
import tdl.record.sourcecode.snapshot.helpers.ByteHelper;

public class SnapshotFileHeader {

    /**
     * The header size. 6 for magic bytes. 8 for timestamp.
     */
    public static int SIZE = 14;

    public static final byte[] MAGIC_BYTES = new byte[]{0x53 /*S*/, 0x52 /*R*/, 0x43 /*C*/, 0x53 /*S*/, 0x54 /*T*/, 0x4d /*M*/};

    private long timestamp;

    public byte[] asBytes() {
        try (ByteArrayOutputStream byteArray = new ByteArrayOutputStream()) {
            byteArray.write(MAGIC_BYTES);
            byteArray.write(ByteHelper.littleEndianLongToByteArray(timestamp, 8));
            return byteArray.toByteArray();
        } catch (IOException ex) {
            return new byte[0];
        }
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public static SnapshotFileHeader fromBytes(byte[] bytes) {
        byte[] magicBytes = Arrays.copyOfRange(bytes, 0, MAGIC_BYTES.length);
        if (!Arrays.equals(magicBytes, MAGIC_BYTES)) {
            throw new RuntimeException("Unrecognized format");
        }
        SnapshotFileHeader header = new SnapshotFileHeader();
        header.timestamp = ByteHelper.byteArrayToLittleEndianLong(Arrays.copyOfRange(bytes, 6, 14));
        return header;
    }
}

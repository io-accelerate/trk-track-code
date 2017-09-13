package tdl.record.sourcecode.snapshot.file;

import tdl.record.sourcecode.snapshot.helpers.ByteHelper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import tdl.record.sourcecode.snapshot.KeySnapshot;
import tdl.record.sourcecode.snapshot.PatchSnapshot;
import tdl.record.sourcecode.snapshot.Snapshot;

public class SnapshotFileSegment {

    /**
     * 6 magic bytes 8 timestamp 8 size 20 checksum
     */
    public static final int HEADER_SIZE = 42;

    public static final int TYPE_KEY = 0;
    public static final int TYPE_PATCH = 1;

    public static final byte[] MAGIC_BYTES_KEY = new byte[]{0x53, 0x52, 0x43, 0x4b, 0x45, 0x59};

    public static final byte[] MAGIC_BYTES_PATCH = new byte[]{0x53, 0x52, 0x43, 0x50, 0x54, 0x43};

    public int type;

    /**
     * Timestamp in second.
     */
    public long timestamp;

    /**
     * The data size in bytes.
     */
    public long size;

    public byte[] checksum;

    public byte[] data;

    public byte[] generateChecksum() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            return md.digest(data);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    public boolean isDataValid() {
        byte[] checksumChallenge = generateChecksum();
        return Arrays.equals(checksumChallenge, checksum);
    }

    public byte[] asBytes() {
        try (ByteArrayOutputStream byteArray = new ByteArrayOutputStream()) {
            byte[] header = getHeaderAsBytes();
            byteArray.write(header);
            byteArray.write(data);
            return byteArray.toByteArray();
        } catch (IOException ex) {
            return new byte[0];
        }
    }

    public byte[] getHeaderAsBytes() {
        try (ByteArrayOutputStream byteArray = new ByteArrayOutputStream(HEADER_SIZE)) {
            byteArray.write(getMagicBytesByType(type));
            byteArray.write(ByteHelper.littleEndianLongToByteArray(timestamp, 8));
            byteArray.write(ByteHelper.littleEndianLongToByteArray(size, 8));
            byteArray.write(checksum);
            return byteArray.toByteArray();
        } catch (IOException ex) {
            return new byte[0];
        }
    }

    public static byte[] getMagicBytesByType(int type) {
        if (type == TYPE_KEY) {
            return MAGIC_BYTES_KEY;
        } else if (type == TYPE_PATCH) {
            return MAGIC_BYTES_PATCH;
        }
        throw new RuntimeException("Unknown type: " + type);
    }

    public static int getTypeByteBytes(byte[] bytes) {
        if (Arrays.equals(bytes, MAGIC_BYTES_KEY)) {
            return TYPE_KEY;
        } else if (Arrays.equals(bytes, MAGIC_BYTES_PATCH)) {
            return TYPE_PATCH;
        }
        throw new RuntimeException("Unknown bytes: " + new String(bytes));
    }

    /**
     * This should only be called when checksum and size is not set. I.e. on
     * writing data instead of reading.
     *
     * @param data
     */
    public void setData(byte[] data) {
        this.data = data;
        this.size = data.length;
        this.checksum = generateChecksum();
    }

    public static SnapshotFileSegment createFromHeaderBytes(byte[] bytes) {
        SnapshotFileSegment snapshot = new SnapshotFileSegment();
        snapshot.type = getTypeByteBytes(Arrays.copyOfRange(bytes, 0, MAGIC_BYTES_KEY.length));
        snapshot.timestamp = ByteHelper.byteArrayToLittleEndianLong(Arrays.copyOfRange(bytes, 6, 14));
        snapshot.size = ByteHelper.byteArrayToLittleEndianLong(Arrays.copyOfRange(bytes, 14, 22));
        snapshot.checksum = Arrays.copyOfRange(bytes, 22, 42);
        return snapshot;
    }

    public Snapshot getSnapshot() {
        switch (type) {
            case TYPE_KEY:
                return KeySnapshot.createSnapshotFromBytes(data);
            case TYPE_PATCH:
                return PatchSnapshot.createSnapshotFromBytes(data);
        }
        throw new RuntimeException("Cannot recognize type");
    }

    public Date getTimestampAsDate() {
        return new Date(timestamp * 1000L);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SnapshotFileSegment)) {
            return false;
        }
        SnapshotFileSegment snapshot = (SnapshotFileSegment) obj;

        return type == snapshot.type
                && timestamp == snapshot.timestamp
                && Arrays.equals(checksum, snapshot.checksum)
                && Arrays.equals(data, snapshot.data);
    }
}
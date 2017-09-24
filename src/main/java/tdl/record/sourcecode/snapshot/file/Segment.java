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

public class Segment {

    /**
     * 6 magic bytes 8 timestamp 8 size 20 checksum
     */
    public static final int HEADER_SIZE = 42;

    public static final int TYPE_KEY = 0;
    public static final int TYPE_PATCH = 1;

    public static final byte[] MAGIC_BYTES_KEY = new byte[]{0x53, 0x52, 0x43, 0x4b, 0x45, 0x59};

    public static final byte[] MAGIC_BYTES_PATCH = new byte[]{0x53, 0x52, 0x43, 0x50, 0x54, 0x43};

    private byte[] data;

    private byte[] checksum;

    private long size;

    private int type;

    private long timestamp;

    private long address;

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
        setSize(data.length);
        setChecksum(generateChecksum());
    }

    public byte[] getChecksum() {
        return checksum;
    }

    public void setChecksum(byte[] checksum) {
        this.checksum = checksum;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getAddress() {
        return address;
    }

    public void setAddress(long address) {
        this.address = address;
    }

    public final byte[] generateChecksum() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            return md.digest(getData());
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    public boolean isDataValid() {
        byte[] checksumChallenge = generateChecksum();
        return Arrays.equals(checksumChallenge, getChecksum());
    }

    public byte[] asBytes() {
        try (ByteArrayOutputStream byteArray = new ByteArrayOutputStream()) {
            byteArray.write(getHeaderAsBytes());
            byteArray.write(getData());
            return byteArray.toByteArray();
        } catch (IOException ex) {
            return new byte[0];
        }
    }

    public byte[] getHeaderAsBytes() {
        try (ByteArrayOutputStream byteArray = new ByteArrayOutputStream(HEADER_SIZE)) {
            byteArray.write(getMagicBytesByType(getType()));
            byteArray.write(ByteHelper.littleEndianLongToByteArray(getTimestamp(), 8));
            byteArray.write(ByteHelper.littleEndianLongToByteArray(getSize(), 8));
            byteArray.write(getChecksum());
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

    public Snapshot getSnapshot() {
        switch (getType()) {
            case TYPE_KEY:
                return KeySnapshot.createSnapshotFromBytes(getData());
            case TYPE_PATCH:
                return PatchSnapshot.createSnapshotFromBytes(getData());
        }
        throw new RuntimeException("Cannot recognize type");
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Segment)) {
            return false;
        }
        Segment snapshot = (Segment) obj;

        return getType() == snapshot.getType()
                && getTimestamp() == snapshot.getTimestamp()
                && Arrays.equals(getChecksum(), snapshot.getChecksum())
                && Arrays.equals(getData(), snapshot.getData());
    }
}

package net.petrabarus.java.record_dir_and_upload.snapshot;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Snapshot {

    /**
     * 1 magic number 1 type key/diff 8 timestamp 8 size 32 checksum
     */
    public static final int HEADER_SIZE = 50;

    public static final int MAGIC_NUMBER = 99;

    public static final int TYPE_KEY = 0;
    public static final int TYPE_DIFF = 1;

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

    public byte[] generateChecksumFromData() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            return md.digest(data);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    public boolean isDataValid() {
        byte[] checksumChallenge = generateChecksumFromData();
        return Arrays.equals(checksumChallenge, checksum);
    }

    public byte[] asBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
        buffer.put(ByteHelper.littleEndianIntToByteArray(MAGIC_NUMBER, 1));
        buffer.put(ByteHelper.littleEndianIntToByteArray(type, 1));
        buffer.put(ByteHelper.littleEndianLongToByteArray(timestamp, 8));
        buffer.put(ByteHelper.littleEndianLongToByteArray(size, 8));
        buffer.put(checksum);
        return buffer.array();
    }

    public static Snapshot createFromHeaderBytes(byte[] bytes) {
        Snapshot snapshot = new Snapshot();

        return snapshot;
    }
}

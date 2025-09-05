package io.accelerate.track.code.snapshot.file;

import io.accelerate.track.code.snapshot.*;
import io.accelerate.track.code.snapshot.helpers.ByteHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Segment {


    static final int SIZE_ADDRESS = 14;

    static final int LONG_SIZE = 8;

    static final int TAG_SIZE = 64;

    static final int CHECKSUM_SIZE = 20;

    /**
     * 6 magic bytes 8 timestamp 8 size 20 checksum 64 tag.
     */
    public static final int HEADER_SIZE = SIZE_ADDRESS + LONG_SIZE + TAG_SIZE + CHECKSUM_SIZE;

    private byte[] data;

    private byte[] checksum;

    private String tag;

    private long size;

    private SnapshotType type;

    private long timestampSec;

    private long address;

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    void generateFromData() {
        setSize(data.length);
        setChecksum(generateChecksum());
    }

    public byte[] getChecksum() {
        return checksum;
    }

    void setChecksum(byte[] checksum) {
        this.checksum = checksum;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public SnapshotType getType() {
        return type;
    }

    public void setType(SnapshotType type) {
        this.type = type;
    }

    public long getTimestampSec() {
        return timestampSec;
    }

    void setTimestampSec(long timestampSec) {
        this.timestampSec = timestampSec;
    }

    public long getAddress() {
        return address;
    }

    public void setAddress(long address) {
        this.address = address;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    boolean hasTag() {
        return tag != null && tag.trim().length() > 0;
    }

    private byte[] generateChecksum() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            return md.digest(getData());
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    public boolean isChecksumMismatch() {
        byte[] checksumChallenge = generateChecksum();
        return !Arrays.equals(checksumChallenge, getChecksum());
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

    private byte[] getHeaderAsBytes() {
        try (ByteArrayOutputStream byteArray = new ByteArrayOutputStream(HEADER_SIZE)) {
            byteArray.write(type.getMagicBytes());
            byteArray.write(ByteHelper.littleEndianLongToByteArray(getTimestampSec(), 8));
            byteArray.write(ByteHelper.littleEndianLongToByteArray(getSize(), 8));
            byteArray.write(getTagAsByte());
            byteArray.write(getChecksum());
            return byteArray.toByteArray();
        } catch (IOException ex) {
            return new byte[0];
        }
    }

    private byte[] getTagAsByte() {
        String tagToSave = getTag();
        if (tagToSave == null) {
            tagToSave = "";
        }
        byte[] src = tagToSave.getBytes(Charset.defaultCharset());
        return Arrays.copyOf(src, TAG_SIZE);
    }

    public Snapshot getSnapshot() {
        switch (getType()) {
            case KEY:
                return KeySnapshot.createSnapshotFromBytes(getData());
            case PATCH:
                return PatchSnapshot.createSnapshotFromBytes(getData());
            case EMPTY:
                return EmptySnapshot.createSnapshotFromBytes(getData());
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
                && getTimestampSec() == snapshot.getTimestampSec()
                && Arrays.equals(getChecksum(), snapshot.getChecksum())
                && Arrays.equals(getData(), snapshot.getData());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Arrays.hashCode(this.data);
        hash = 67 * hash + Arrays.hashCode(this.checksum);
        hash = 67 * hash + this.type.hashCode();
        hash = 67 * hash + (int) (this.timestampSec ^ (this.timestampSec >>> 32));
        return hash;
    }
}

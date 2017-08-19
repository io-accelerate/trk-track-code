package net.petrabarus.java.record_dir_and_upload.helpers;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteHelper {

    public static int byteArrayToLittleEndianInt(byte[] b) {
        final ByteBuffer bb = ByteBuffer.wrap(b);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getInt();
    }

    public static long byteArrayToLittleEndianLong(byte[] b) {
        final ByteBuffer bb = ByteBuffer.wrap(b);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getLong();
    }

    public static byte[] littleEndianIntToByteArray(int i, int size) {
        final ByteBuffer bb = ByteBuffer.allocate(size);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(i);
        return bb.array();
    }

    public static byte[] littleEndianLongToByteArray(long i, int size) {
        final ByteBuffer bb = ByteBuffer.allocate(size);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putLong(i);
        return bb.array();
    }
}

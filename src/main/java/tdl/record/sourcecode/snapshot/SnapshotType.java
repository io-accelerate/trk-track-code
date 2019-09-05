package tdl.record.sourcecode.snapshot;

import java.util.Arrays;

public enum SnapshotType {
    KEY(new byte[]{0x53 /*S*/, 0x52 /*R*/, 0x43 /*C*/, 0x4b /*K*/, 0x45 /*E*/, 0x59 /*Y*/}),
    PATCH(new byte[]{0x53 /*S*/, 0x52 /*R*/, 0x43 /*C*/, 0x50 /*P*/, 0x54 /*T*/, 0x43 /*C*/}),
    EMPTY(new byte[]{0x53 /*S*/, 0x52 /*R*/, 0x43 /*C*/, 0x45 /*E*/, 0x4d /*M*/, 0x50 /*P*/});

    public static final int MAGIC_BYTES_LENGTH = 6;

    private final byte[] magicBytes;

    SnapshotType(byte[] magicBytes) {
        this.magicBytes = magicBytes;
    }

    public byte[] getMagicBytes() {
        return magicBytes;
    }


    public static SnapshotType fromMagicBytes(byte[] bytes) {
        for (SnapshotType value : SnapshotType.values()) {
            if (Arrays.equals(bytes, value.getMagicBytes()))
                return value;
        }
        throw new RuntimeException("Unknown bytes: '" + new String(bytes) + "'");
    }
}

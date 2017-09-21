package tdl.record.sourcecode.snapshot.file;

import tdl.record.sourcecode.snapshot.file.Header;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import tdl.record.sourcecode.snapshot.helpers.ByteHelper;

public class HeaderTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void fromBytesAndAsBytes() throws IOException {
        long timestamp = 10000L;
        byte[] timestampBytes = ByteHelper.littleEndianLongToByteArray(timestamp, 8);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(Header.MAGIC_BYTES);
        outputStream.write(timestampBytes);
        byte[] all = outputStream.toByteArray();

        Header header = Header.fromBytes(all);
        assertEquals(header.getTimestamp(), timestamp);

        byte[] asBytes = header.asBytes();
        assertArrayEquals(all, asBytes);
    }

    @Test
    public void fromBytesShouldThrowException() throws IOException {
        expectedException.expect(RuntimeException.class);
        byte[] wrongHeader = new byte[]{0x53 /*S*/, 0x52 /*R*/, 0x43 /*C*/, 0x53 /*S*/, 0x54 /*T*/, 0x4e /*N*/};
        long timestamp = 10000L;
        byte[] timestampBytes = ByteHelper.littleEndianLongToByteArray(timestamp, 8);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(wrongHeader);
        outputStream.write(timestampBytes);
        byte[] all = outputStream.toByteArray();
        Header.fromBytes(all);
    }
}

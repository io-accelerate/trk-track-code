package tdl.record.sourcecode.snapshot.helpers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class ByteHelperTest {

    private static Stream<Arguments> dataProvider_littleEndianIntToByteArray() {
        return Stream.of(
            Arguments.of(1, new byte[]{1, 0, 0, 0}),
            Arguments.of(2, new byte[]{2, 0, 0, 0}),
            Arguments.of(127, new byte[]{127, 0, 0, 0}),
            Arguments.of(128, new byte[]{-128, 0, 0, 0}),
            Arguments.of(129, new byte[]{-127, 0, 0, 0}),
            Arguments.of(256, new byte[]{0, 1, 0, 0}),
            Arguments.of(1024, new byte[]{0, 4, 0, 0}),
            Arguments.of(65536, new byte[]{0, 0, 1, 0}),
            Arguments.of(16777216, new byte[]{0, 0, 0, 1})
            );
    }

    @ParameterizedTest
    @MethodSource("dataProvider_littleEndianIntToByteArray")
    public void littleEndianIntToByteArray(int number, byte[] expected) {
        byte[] actuals = ByteHelper.littleEndianIntToByteArray(number, 4);
        Assertions.assertArrayEquals(expected, actuals);
        int number2 = ByteHelper.byteArrayToLittleEndianInt(actuals);
        Assertions.assertEquals(number, number2);
    }
    
    private static Stream<Arguments>  dataProvider_littleEndianLongToByteArray() {
        return Stream.of(
            Arguments.of(1L, new byte[]{1, 0, 0, 0, 0, 0, 0, 0}),
            Arguments.of(2L, new byte[]{2, 0, 0, 0, 0, 0, 0, 0}),
            Arguments.of(127L, new byte[]{127, 0, 0, 0, 0, 0, 0, 0}),
            Arguments.of(128L, new byte[]{-128, 0, 0, 0, 0, 0, 0, 0}),
            Arguments.of(129L, new byte[]{-127, 0, 0, 0, 0, 0, 0, 0}),
            Arguments.of(256L, new byte[]{0, 1, 0, 0, 0, 0, 0, 0}),
            Arguments.of(1024L, new byte[]{0, 4, 0, 0, 0, 0, 0, 0}),
            Arguments.of(65536L, new byte[]{0, 0, 1, 0, 0, 0, 0, 0}),
            Arguments.of(16777216L, new byte[]{0, 0, 0, 1, 0, 0, 0, 0})
        );
    }
    
    @ParameterizedTest
    @MethodSource("dataProvider_littleEndianLongToByteArray")
    public void littleEndianLongToByteArray(long number, byte[] expected) {
        byte[] actuals = ByteHelper.littleEndianLongToByteArray(number, 8);
        Assertions.assertArrayEquals(expected, actuals);
        long number2 = ByteHelper.byteArrayToLittleEndianLong(actuals);
        Assertions.assertEquals(number, number2);
    }
}

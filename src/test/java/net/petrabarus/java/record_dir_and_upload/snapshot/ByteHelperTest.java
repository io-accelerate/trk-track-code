package net.petrabarus.java.record_dir_and_upload.snapshot;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(DataProviderRunner.class)
public class ByteHelperTest {

    @DataProvider
    public static Object[][] dataProvider_littleEndianIntToByteArray() {
        // @formatter:off
        return new Object[][]{
            {1, new byte[]{1, 0, 0, 0}},
            {2, new byte[]{2, 0, 0, 0}},
            {127, new byte[]{127, 0, 0, 0}},
            {128, new byte[]{-128, 0, 0, 0}},
            {129, new byte[]{-127, 0, 0, 0}},
            {256, new byte[]{0, 1, 0, 0}},
            {1024, new byte[]{0, 4, 0, 0}},
            {65536, new byte[]{0, 0, 1, 0}},
            {16777216, new byte[]{0, 0, 0, 1}},};
    }

    @Test
    @UseDataProvider("dataProvider_littleEndianIntToByteArray")
    public void littleEndianIntToByteArray(int number, byte[] expected) {
        byte[] actuals = ByteHelper.littleEndianIntToByteArray(number, 4);
        Assert.assertArrayEquals(expected, actuals);
        int number2 = ByteHelper.byteArrayToLittleEndianInt(actuals);
        Assert.assertEquals(number, number2);
    }
    
    @DataProvider
    public static Object[][] dataProvider_littleEndianLongToByteArray() {
        // @formatter:off
        return new Object[][]{
            {1L, new byte[]{1, 0, 0, 0, 0, 0, 0, 0}},
            {2L, new byte[]{2, 0, 0, 0, 0, 0, 0, 0}},
            {127L, new byte[]{127, 0, 0, 0, 0, 0, 0, 0}},
            {128L, new byte[]{-128, 0, 0, 0, 0, 0, 0, 0}},
            {129L, new byte[]{-127, 0, 0, 0, 0, 0, 0, 0}},
            {256L, new byte[]{0, 1, 0, 0, 0, 0, 0, 0}},
            {1024L, new byte[]{0, 4, 0, 0, 0, 0, 0, 0}},
            {65536L, new byte[]{0, 0, 1, 0, 0, 0, 0, 0}},
            {16777216L, new byte[]{0, 0, 0, 1, 0, 0, 0, 0}}
        };
    }
    
    @Test
    @UseDataProvider("dataProvider_littleEndianLongToByteArray")
    public void littleEndianLongToByteArray(long number, byte[] expected) {
        byte[] actuals = ByteHelper.littleEndianLongToByteArray(number, 8);
        Assert.assertArrayEquals(expected, actuals);
        long number2 = ByteHelper.byteArrayToLittleEndianLong(actuals);
        Assert.assertEquals(number, number2);
    }
}

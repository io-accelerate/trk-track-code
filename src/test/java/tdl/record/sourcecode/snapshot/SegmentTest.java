package tdl.record.sourcecode.snapshot;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.eclipse.jgit.lib.Repository;
import static org.junit.Assert.assertEquals;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import tdl.record.sourcecode.snapshot.file.Segment;

@RunWith(DataProviderRunner.class)
public class SegmentTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @DataProvider
    public static Object[][] dataProvider_isValidTagName() {
        // @formatter:off
        return new Object[][]{
            {"ref", true},
            {"ref1", true},
            {"ref 1", false},
            {"v1.1", true},
            {"v1.1.", false},
            {"v1..1", false},
            {"v11\\", false},};
    }

    @Test
    @UseDataProvider("dataProvider_isValidTagName")
    public void isValidTagName(String tagName, boolean expected) {
        assertEquals(expected, Segment.isValidTagName(tagName));
    }

    @DataProvider
    public static Object[][] dataProvider_setTag() {
        // @formatter:off
        return new Object[][]{
            {null, false},
            {"", false},
            {" ", false},
            {"ref", false},
            {"ref1", false},
            {"ref 1", true},
            {"v1.1", false},
            {"v1.1.", true},
            {"v1..1", true},
            {"v11\\", true}
        };
    }

    @Test
    @UseDataProvider("dataProvider_setTag")
    public void setTag(String tagName, boolean shouldExpectException) {
        if (shouldExpectException) {
            expectedException.expectMessage("Invalid tag name");
        }
        Segment segment = new Segment();
        segment.setTag(tagName);
    }
}

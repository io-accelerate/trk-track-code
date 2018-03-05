package tdl.record.sourcecode.snapshot.file;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(DataProviderRunner.class)
public class TagManagerTest {

    @DataProvider
    public static Object[][] dataProvider_isTag() {
        // @formatter:off
        return new Object[][]{
                {null, false},
                {"", false},
                {"   ", false},
                {"ref", true},
        };
    }

    @Test
    @UseDataProvider("dataProvider_isTag")
    public void isTag(String tagName, boolean expectedAnswer) {
        assertThat(TagManager.isTag(tagName), is(expectedAnswer));
    }

    @DataProvider
    public static Object[][] dataProvider_asValidTag() {
        // @formatter:off
        return new Object[][]{
                {"keep normal tag", "tag1", "tag1"},
                {"trim large tag", String.format("%0200d", 0), String.format("%064d", 0)},
                {"trim leading spaces", "  tag   ", "tag"},
                {"replace dodgy characters", "@@a_\\//@  #(x)  *<t>", "a_/#(x)_t"},
        };
    }

    @Test
    @UseDataProvider("dataProvider_asValidTag")
    public void asValidTag(String testName, String tagName, String expectedTag) {
        assertThat(testName, new TagManager().asValidTag(tagName), is(expectedTag));
    }

    @Test
    public void ensure_tag_uniqueness() {
        TagManager tagManager = new TagManager();
        tagManager.addExisting(Arrays.asList("tag", "tag_1"));

        assertThat("tag reuse 2", tagManager.asUniqueTag("tag"), is("tag_2"));
        assertThat("tag reuse 3", tagManager.asUniqueTag("tag"), is("tag_3"));
        assertThat("tag clash", tagManager.asUniqueTag("tag_2"), is("tag_2_2"));
    }
}
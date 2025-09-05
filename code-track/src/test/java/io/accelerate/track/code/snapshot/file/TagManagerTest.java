package io.accelerate.track.code.snapshot.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TagManagerTest {

    private static Stream<Arguments> dataProvider_isTag() {
        return Stream.of(
                Arguments.of(null, false),
                Arguments.of("", false),
                Arguments.of("   ", false),
                Arguments.of("ref", true)
        );
    }

    @ParameterizedTest
    @MethodSource("dataProvider_isTag")
    public void isTag(String tagName, boolean expectedAnswer) {
        assertThat(TagManager.isTag(tagName), is(expectedAnswer));
    }

    private static Stream<Arguments> dataProvider_asValidTag() {
        return Stream.of(
                Arguments.of("keep normal tag", "tag1", "tag1"),
                Arguments.of("trim large tag", String.format("%0200d", 0), String.format("%064d", 0)),
                Arguments.of("trim leading spaces", "  tag   ", "tag"),
                Arguments.of("replace dodgy characters", "@@a_\\//@  #(x)  *<t>", "a_/#(x)_t")
        );
    }

    @ParameterizedTest
    @MethodSource("dataProvider_asValidTag")
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

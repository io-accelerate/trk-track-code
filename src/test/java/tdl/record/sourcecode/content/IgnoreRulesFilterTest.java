package tdl.record.sourcecode.content;

import org.apache.commons.io.filefilter.IOFileFilter;
import org.eclipse.jgit.ignore.FastIgnoreRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static tdl.record.sourcecode.test.FileTestHelper.applyIOFileFilter;

public class IgnoreRulesFilterTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void accept() {
        Path dir = folder.getRoot().toPath();
        List<String> filenames = Arrays.asList(
                "TestResultX/1.txt",
                "testresultY/2.txt",
                ".vs/x.txt",
                "challenges/FIZ.txt",
                "subdir/X/Y.ignore/test.xml",

                "challenges/keep",
                "keep/test.xml"
        );

        IOFileFilter filter1 = new IgnoreRulesFilter(dir,
                Arrays.asList(
                        new FastIgnoreRule("[Tt]est[Rr]esult*/"),
                        new FastIgnoreRule(".vs/"),
                        new FastIgnoreRule("**/challenges/*.txt"),
                        new FastIgnoreRule("**/*.ignore/test.xml")
                )
        );

        List<String> actualNames = applyIOFileFilter(filter1, dir, filenames);

        List<String> expectedNames = Arrays.asList(
                "challenges/keep",
                "keep/test.xml"
        );
        assertThat(actualNames, equalTo(expectedNames));
    }
}
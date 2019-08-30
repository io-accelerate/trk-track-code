package tdl.record.sourcecode.snapshot.helpers;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static tdl.record.sourcecode.test.FileTestHelper.applyIOFileFilter;

public class ExcludeGitDirectoryFileFilterTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void accept() {
        Path dir = folder.getRoot().toPath();
        List<String> filenames = Arrays.asList(
                ".git/text2.txt",
                ".git/text3.txt",
                "subdir/text4.txt",
                "subdir/text5.txt",
                "text1.txt",
                "text2.txt"
        );

        ExcludeGitDirectoryFileFilter filter = new ExcludeGitDirectoryFileFilter(dir);
        List<String> actualNames = applyIOFileFilter(filter, dir, filenames);

        List<String> expectedNames = Arrays.asList(
                "subdir/text4.txt",
                "subdir/text5.txt",
                "text1.txt",
                "text2.txt"
        );
        assertThat(actualNames, equalTo(expectedNames));
    }

}

package tdl.record.sourcecode.snapshot.helpers;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import tdl.record.sourcecode.test.FileTestHelper;

public class ExcludeGitDirectoryFileFilterTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void accept() throws IOException {
        Path dir = folder.getRoot().toPath();
        FileTestHelper.appendStringToFile(dir, "text1.txt", "Hello World!");
        FileTestHelper.appendStringToFile(dir, "text2.txt", "Hello World!");
        FileTestHelper.appendStringToFile(dir, ".git/text2.txt", "Hello World!");
        FileTestHelper.appendStringToFile(dir, ".git/text3.txt", "Hello World!");
        FileTestHelper.appendStringToFile(dir, "subdir/text4.txt", "Hello World!");
        FileTestHelper.appendStringToFile(dir, "subdir/text5.txt", "Hello World!");
        IOFileFilter filter = new ExcludeGitDirectoryFileFilter(dir);
        String[] names = (String[]) FileUtils.listFiles(dir.toFile(), filter, TrueFileFilter.INSTANCE)
                .stream()
                .map(File::getName)
                .toArray(String[]::new);
        String[] expected = new String[]{"text1.txt", "text2.txt", "text4.txt", "text5.txt"};
        Arrays.sort(names);
        Arrays.sort(expected);
        Assert.assertArrayEquals(names, expected);
    }
}

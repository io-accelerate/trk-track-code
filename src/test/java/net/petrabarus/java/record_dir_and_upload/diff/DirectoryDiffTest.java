package net.petrabarus.java.record_dir_and_upload.diff;

import difflib.Patch;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class DirectoryDiffTest {

    @Test
    public void getRelativeFilePathList() {
        Path directory = Paths.get("./src/test/resources/diff/test1/dir1/");
        List<String> result = DirectoryDiff.getRelativeFilePathList(directory);
        List<String> expected = Arrays.asList(
                "file1.txt",
                "file2.txt",
                "subdir1/file1.txt",
                "subdir1/subfile1.txt"
        );
        Collections.sort(expected);
        assertEquals(expected, result);
    }

    @Test
    public void getUnionRelativeFilePathList() {
        Path directory1 = Paths.get("./src/test/resources/diff/test1/dir1/");
        Path directory2 = Paths.get("./src/test/resources/diff/test1/dir2/");

        List<String> result = DirectoryDiff.getUnionRelativeFilePathList(directory1, directory2);
        List<String> expected = Arrays.asList(
                "file1.txt",
                "file2.txt",
                "file3.txt",
                "subdir1/file1.txt",
                "subdir1/subfile1.txt"
        );
        Collections.sort(expected);
        assertEquals(expected, result);
    }

    @Test
    public void diffFiles() throws IOException {
        Path path1 = Paths.get("./src/test/resources/diff/test1/dir1/file1.txt");
        Path path2 = Paths.get("./src/test/resources/diff/test1/dir1/file2.txt");
        Patch patch = DirectoryDiff.diffFiles(path1, path2);
        assertEquals(1, patch.getDeltas().size());
        String line = patch.getDelta(0).getRevised().getLines().get(0).toString();
        assertEquals("XXX", line);
    }

    @Test
    public void diffDirectories() throws IOException {
        Path path1 = Paths.get("./src/test/resources/diff/test1/dir1");
        Path path2 = Paths.get("./src/test/resources/diff/test1/dir2");
        Map<String, Patch> diff = DirectoryDiff.diffDirectories(path1, path2);
        //System.out.println(diff.keySet());
    }
}

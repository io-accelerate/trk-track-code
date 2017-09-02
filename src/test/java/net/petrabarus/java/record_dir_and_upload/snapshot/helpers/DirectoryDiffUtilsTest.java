package net.petrabarus.java.record_dir_and_upload.snapshot.helpers;

import net.petrabarus.java.record_dir_and_upload.snapshot.helpers.DirectoryDiffUtils;
import difflib.Patch;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.petrabarus.java.record_dir_and_upload.test.FileTestHelper;
import org.apache.commons.io.FileUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class DirectoryDiffUtilsTest {

    @Test
    public void getRelativeFilePathList() {
        Path directory = Paths.get("./src/test/resources/diff/test1/dir1/");
        List<String> result = DirectoryDiffUtils.getRelativeFilePathList(directory);
        List<String> expected = Arrays.asList(
                "file1.txt",
                "file2.txt",
                "file4.txt",
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

        List<String> result = DirectoryDiffUtils.getUnionRelativeFilePathList(directory1, directory2);
        List<String> expected = Arrays.asList(
                "file1.txt",
                "file2.txt",
                "file3.txt",
                "file4.txt",
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
        Patch patch = DirectoryDiffUtils.diffFiles(path1, path2);
        assertEquals(1, patch.getDeltas().size());
        String line = patch.getDelta(0).getRevised().getLines().get(0).toString();
        assertEquals("XXX", line);
    }

    @Test
    public void diffDirectories() throws IOException {
        Path path1 = Paths.get("./src/test/resources/diff/test1/dir1");
        Path path2 = Paths.get("./src/test/resources/diff/test1/dir2");
        Map<String, Patch> diff = DirectoryDiffUtils.diffDirectories(path1, path2);
        //System.out.println(diff.keySet());
        Patch patchFile3 = diff.get("file3.txt");
        assertEquals(3, patchFile3.getDeltas().get(0).getRevised().getSize());
    }

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void patch() throws IOException {
        Path original = Paths.get("./src/test/resources/diff/test1/dir1");
        Path revised = Paths.get("./src/test/resources/diff/test1/dir2");

        Map<String, Patch> patches = DirectoryDiffUtils.diffDirectories(original, revised);

        File patchDest = temp.newFolder("original");
        Path patchDestPath = patchDest.toPath();
        FileUtils.copyDirectory(original.toFile(), patchDest);

        assertTrue(patchDestPath.resolve("file4.txt").toFile().exists());
        DirectoryDiffUtils.patch(patchDest.toPath(), patches);
        assertTrue(FileUtils.contentEquals(
                revised.resolve("file3.txt").toFile(),
                patchDest.toPath().resolve("file3.txt").toFile()
        ));
        assertFalse(patchDestPath.resolve("file4.txt").toFile().exists());

        assertTrue(FileTestHelper.isDirectoryEquals(revised, patchDestPath));
    }
}

package io.accelerate.tracking.code.snapshot.helpers;

import difflib.Delta;
import difflib.InsertDelta;
import difflib.Patch;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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

        Delta delta = (Delta) patch.getDeltas().get(0);
        assertTrue(delta instanceof InsertDelta);
        String line = (String) delta.getRevised().getLines().get(0);
        assertEquals("XXX", line);
    }

    @Test
    public void diffDirectories() throws IOException {
        Path path1 = Paths.get("./src/test/resources/diff/test1/dir1");
        Path path2 = Paths.get("./src/test/resources/diff/test1/dir2");
        DirectoryPatch patches = DirectoryDiffUtils.diffDirectories(path1, path2);

        Patch patchFile3 = patches.getPatches().get("file3.txt");
        assertEquals(3, ((Delta) patchFile3.getDeltas().get(0)).getRevised().size());
    }

    @TempDir
    Path temp;

    @Test
    public void patch() throws IOException {
        Path original = Paths.get("./src/test/resources/diff/test1/dir1");
        Path revised = Paths.get("./src/test/resources/diff/test1/dir2");

        DirectoryPatch patches = DirectoryDiffUtils.diffDirectories(original, revised);

        Path patchDest = temp.resolve("original");
        FileUtils.copyDirectory(original.toFile(), patchDest.toFile());

        assertTrue(patchDest.resolve("file4.txt").toFile().exists());
        DirectoryDiffUtils.patch(patchDest, patches);
        assertTrue(FileUtils.contentEquals(
                revised.resolve("file3.txt").toFile(),
                patchDest.resolve("file3.txt").toFile()
        ));

        assertFalse(patchDest.resolve("file4.txt").toFile().exists());
    }
}

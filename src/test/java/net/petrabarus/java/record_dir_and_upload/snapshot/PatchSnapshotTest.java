package net.petrabarus.java.record_dir_and_upload.snapshot;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.petrabarus.java.record_dir_and_upload.test.FileTestHelper;
import org.apache.commons.io.FileUtils;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class PatchSnapshotTest {

    @Rule
    public TemporaryFolder temporary = new TemporaryFolder();

    @Test
    public void takeAndRestoreSnapshot() throws IOException, ClassNotFoundException {
        Path originalContent = Paths.get("./src/test/resources/diff/test1/dir1/");

        File originalCopy = temporary.newFolder();
        FileUtils.copyDirectory(originalContent.toFile(), originalCopy);
        File revisedCopy = temporary.newFolder();
        FileUtils.copyDirectory(originalContent.toFile(), revisedCopy);

        FileUtils.writeStringToFile(
                new File(revisedCopy, "file1.txt"),
                "Lorem Ipsum Dolor Sit Amet!",
                StandardCharsets.US_ASCII,
                true
        );
        PatchSnapshot snapshot = PatchSnapshot.takeSnapshotFromDirectories(originalCopy.toPath(), revisedCopy.toPath());
        snapshot.restoreSnapshot(originalCopy.toPath());
        assertTrue(FileTestHelper.isDirectoryEquals(originalCopy.toPath(), revisedCopy.toPath()));
    }
}

package net.petrabarus.java.record_dir_and_upload.snapshot.file;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class SnapshotsFileReaderTest {

    @Rule
    public TemporaryFolder outputFolder = new TemporaryFolder();

    @Rule
    public TemporaryFolder zipFolder = new TemporaryFolder();

    private Path outputFilePath;

    @Before
    public void createSnapshotFile() throws IOException {
        outputFilePath = outputFolder.newFile("output.bin").toPath();
        Path zipFolderPath = zipFolder.getRoot().toPath();

        try (SnapshotsFileWriter writer = new SnapshotsFileWriter(outputFilePath, zipFolderPath, true)) {

            File newFile1 = zipFolder.newFile("test1.txt");
            FileUtils.writeStringToFile(newFile1, "TEST1", StandardCharsets.US_ASCII);

            writer.takeSnapshot();

            FileUtils.writeStringToFile(newFile1, "TEST2", StandardCharsets.US_ASCII, true);

            writer.takeSnapshot();

            File newFile2 = zipFolder.newFile("test2.txt");
            newFile2.delete();
            FileUtils.moveFile(newFile1, newFile2);

            writer.takeSnapshot();
        }
    }

    @Test
    public void next() throws IOException {
        try (SnapshotsFileReader reader = new SnapshotsFileReader(outputFilePath.toFile())) {
            assertTrue(reader.hasNext());
            SnapshotFileSegment snapshot1 = reader.next();
            assertTrue(getTimestamp() - snapshot1.timestamp < 60);

            assertTrue(reader.hasNext());
            SnapshotFileSegment snapshot2 = reader.next();
            assertTrue(getTimestamp() - snapshot2.timestamp < 60);

            assertTrue(reader.hasNext());
            SnapshotFileSegment snapshot3 = reader.next();
            assertTrue(getTimestamp() - snapshot3.timestamp < 60);

            assertFalse(reader.hasNext());
        }
    }

    public int getTimestamp() {
        Long unixTimestamp = System.currentTimeMillis() / 1000L;
        return unixTimestamp.intValue();
    }

    @Test
    public void skip() throws IOException {

        try (SnapshotsFileReader reader = new SnapshotsFileReader(outputFilePath.toFile())) {
            assertTrue(reader.hasNext());
            SnapshotFileSegment snapshot1 = reader.next();
            assertTrue(getTimestamp() - snapshot1.timestamp < 60);

            assertTrue(reader.hasNext());
            reader.skip();

            assertTrue(reader.hasNext());
            SnapshotFileSegment snapshot3 = reader.next();
            assertTrue(getTimestamp() - snapshot3.timestamp < 60);

            assertFalse(reader.hasNext());
        }
    }

    @Test
    public void reset() throws IOException {
        try (SnapshotsFileReader reader = new SnapshotsFileReader(outputFilePath.toFile())) {
            assertTrue(reader.hasNext());
            SnapshotFileSegment snapshot1 = reader.next();

            assertTrue(reader.hasNext());
            reader.skip();

            assertTrue(reader.hasNext());
            reader.skip();

            assertFalse(reader.hasNext());
            reader.reset();
            assertTrue(reader.hasNext());

            SnapshotFileSegment snapshot2 = reader.next();

            assertEquals(snapshot1, snapshot2);
        }
    }

}

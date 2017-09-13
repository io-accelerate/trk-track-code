package tdl.record.sourcecode.snapshot.file;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import support.time.FakeTimeSource;
import tdl.record.sourcecode.content.CopyFromDirectorySourceCodeProvider;
import tdl.record.sourcecode.time.TimeSource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class SnapshotsFileReaderTest {

    @Rule
    public TemporaryFolder sourceFolder = new TemporaryFolder();

    @Rule
    public TemporaryFolder destinationFolder = new TemporaryFolder();

    private Path outputFilePath;

    @Before
    public void createSnapshotFile() throws IOException {
        outputFilePath = sourceFolder.newFile("output.bin").toPath();
        Path destinationFolderPath = destinationFolder.getRoot().toPath();
        CopyFromDirectorySourceCodeProvider sourceCodeProvider = new CopyFromDirectorySourceCodeProvider(destinationFolderPath);
        TimeSource timeSource = new FakeTimeSource(TimeUnit.SECONDS.toNanos(1));
        try (SnapshotsFileWriter writer = new SnapshotsFileWriter(
                outputFilePath, sourceCodeProvider, timeSource, 5, true)) {

            File newFile1 = destinationFolder.newFile("test1.txt");
            FileUtils.writeStringToFile(newFile1, "TEST1", StandardCharsets.US_ASCII);

            writer.takeSnapshot();

            FileUtils.writeStringToFile(newFile1, "TEST2", StandardCharsets.US_ASCII, true);

            writer.takeSnapshot();

            File newFile2 = destinationFolder.newFile("test2.txt");
            newFile2.delete();
            FileUtils.moveFile(newFile1, newFile2);

            writer.takeSnapshot();
        }
    }

    @Test
    public void next() throws IOException {
        try (SnapshotsFileReader reader = new SnapshotsFileReader(outputFilePath.toFile())) {
            assertTrue(reader.hasNext());
            assertThat(reader.next().timestamp, equalTo(1L));

            assertTrue(reader.hasNext());
            assertThat(reader.next().timestamp, equalTo(2L));

            assertTrue(reader.hasNext());
            assertThat(reader.next().timestamp, equalTo(3L));

            assertFalse(reader.hasNext());
        }
    }

    @Test
    public void skip() throws IOException {

        try (SnapshotsFileReader reader = new SnapshotsFileReader(outputFilePath.toFile())) {
            assertTrue(reader.hasNext());
            assertThat(reader.next().timestamp, equalTo(1L));

            assertTrue(reader.hasNext());
            reader.skip();

            assertTrue(reader.hasNext());
            assertThat(reader.next().timestamp, equalTo(3L));

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

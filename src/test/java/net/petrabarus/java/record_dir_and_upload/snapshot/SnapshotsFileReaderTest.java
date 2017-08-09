package net.petrabarus.java.record_dir_and_upload.snapshot;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.petrabarus.java.record_dir_and_upload.snapshot.SnapshotsFileReader.Snapshot;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class SnapshotsFileReaderTest {

    @Test
    public void next() throws IOException {
        Path path = Paths.get("src/test/resources/snapshot.bin");

        try (SnapshotsFileReader reader = new SnapshotsFileReader(path.toFile())) {
            assertTrue(reader.hasNext());
            Snapshot snapshot1 = reader.next();
            byte[] data1 = snapshot1.data;
            assertEquals(1502318398, snapshot1.timestamp);
            assertEquals(1091, snapshot1.size);
            assertEquals(snapshot1.size, data1.length);

            assertTrue(reader.hasNext());

            Snapshot snapshot2 = reader.next();
            byte[] data2 = snapshot2.data;
            assertEquals(1502318408, snapshot2.timestamp);
            assertEquals(1091, snapshot2.size);
            assertEquals(snapshot2.size, data2.length);

            assertTrue(reader.hasNext());

            Snapshot snapshot3 = reader.next();
            byte[] data3 = snapshot3.data;
            assertEquals(1502318418, snapshot3.timestamp);
            assertEquals(1091, snapshot3.size);
            assertEquals(snapshot3.size, data3.length);

            assertFalse(reader.hasNext());
        }
    }

    @Test
    public void skip() throws IOException {
        Path path = Paths.get("src/test/resources/snapshot.bin");

        try (SnapshotsFileReader reader = new SnapshotsFileReader(path.toFile())) {
            assertTrue(reader.hasNext());

            reader.skip();

            assertTrue(reader.hasNext());

            Snapshot snapshot2 = reader.next();
            byte[] data2 = snapshot2.data;
            assertEquals(1502318408, snapshot2.timestamp);
            assertEquals(1091, snapshot2.size);
            assertEquals(snapshot2.size, data2.length);

            assertTrue(reader.hasNext());

            reader.skip();

            assertFalse(reader.hasNext());
        }
    }

    @Test
    public void reset() throws IOException {
        Path path = Paths.get("src/test/resources/snapshot.bin");

        try (SnapshotsFileReader reader = new SnapshotsFileReader(path.toFile())) {
            assertTrue(reader.hasNext());
            Snapshot snapshot1 = reader.next();
            byte[] data1 = snapshot1.data;
            assertEquals(1502318398, snapshot1.timestamp);
            assertEquals(1091, snapshot1.size);
            assertEquals(snapshot1.size, data1.length);

            assertTrue(reader.hasNext());

            reader.skip();

            assertTrue(reader.hasNext());

            reader.skip();

            assertFalse(reader.hasNext());

            reader.reset();

            assertTrue(reader.hasNext());

            Snapshot snapshot2 = reader.next();
            byte[] data2 = snapshot2.data;
            assertEquals(1502318398, snapshot2.timestamp);
            assertEquals(1091, snapshot2.size);
            assertEquals(snapshot2.size, data2.length);
        }
    }

}

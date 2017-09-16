package tdl.record.sourcecode.snapshot.file;

import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import tdl.record.sourcecode.record.SourceCodeRecorderException;
import tdl.record.sourcecode.test.TemporarySourceCodeRecorder;

public class SnapshotsFileReaderTest {

    @Rule
    public TemporarySourceCodeRecorder recorder = new TemporarySourceCodeRecorder(Arrays.asList(
            dst -> TemporarySourceCodeRecorder.writeFile(dst, "test1.txt", "TEST1"), //key
            dst -> TemporarySourceCodeRecorder.writeFile(dst, "test1.txt", "TEST1TEST2"), //patch
            dst -> TemporarySourceCodeRecorder.writeFile(dst, "test2.txt", "TEST1TEST2"), //patch
            dst -> { //key
                TemporarySourceCodeRecorder.writeFile(dst, "test2.txt", "TEST1TEST2");
                TemporarySourceCodeRecorder.writeFile(dst, "subdir/test3.txt", "TEST3");
            },
            dst -> {/* Empty folder */ }, //patch
            dst -> TemporarySourceCodeRecorder.writeFile(dst, "test1.txt", "TEST1TEST2"), //patch
            dst -> TemporarySourceCodeRecorder.writeFile(dst, "test1.txt", "TEST1TEST2"), //key
            dst -> TemporarySourceCodeRecorder.writeFile(dst, "test1.txt", "TEST1TEST2"), //patch
            dst -> TemporarySourceCodeRecorder.writeFile(dst, "test1.txt", "TEST1TEST2"), //patch
            dst -> TemporarySourceCodeRecorder.writeFile(dst, "test1.txt", "TEST1TEST2") //key
    ));

    @Test
    public void next() throws IOException {
        try (SnapshotsFileReader reader = new SnapshotsFileReader(recorder.getOutputFilePath().toFile())) {
            assertTrue(reader.hasNext());
            assertThat(reader.next().timestamp, equalTo(0L));

            assertTrue(reader.hasNext());
            assertThat(reader.next().timestamp, equalTo(1L));

            assertTrue(reader.hasNext());
            assertThat(reader.next().timestamp, equalTo(2L));

            assertTrue(reader.hasNext());
        }
    }

    @Test
    public void skip() throws IOException {

        try (SnapshotsFileReader reader = new SnapshotsFileReader(recorder.getOutputFilePath().toFile())) {
            assertTrue(reader.hasNext());
            assertThat(reader.next().timestamp, equalTo(0L));

            assertTrue(reader.hasNext());
            reader.skip();

            assertTrue(reader.hasNext());
            assertThat(reader.next().timestamp, equalTo(2L));

            assertTrue(reader.hasNext());
        }
    }

    @Test
    public void reset() throws IOException {
        try (SnapshotsFileReader reader = new SnapshotsFileReader(recorder.getOutputFilePath().toFile())) {
            assertTrue(reader.hasNext());
            SnapshotFileSegment snapshot1 = reader.next();

            assertTrue(reader.hasNext());
            reader.skip();

            assertTrue(reader.hasNext());
            reader.skip();

            assertTrue(reader.hasNext());
            reader.reset();
            assertTrue(reader.hasNext());

            SnapshotFileSegment snapshot2 = reader.next();

            assertEquals(snapshot1, snapshot2);
        }
    }

    @Test
    public void getSnapshotAt() throws SourceCodeRecorderException, IOException, Exception {
        try (SnapshotsFileReader reader = new SnapshotsFileReader(recorder.getOutputFilePath().toFile())) {
            int[][] inputAndExpected = new int[][]{
                {0, 139},
                {1, 136},
                {2, 182}
            };
            for (int[] inputs : inputAndExpected) {
                int input = inputs[0];
                int expected = inputs[1];
                int actual = (int) reader.getSnapshotAt(input).size;
                assertEquals(expected, actual);
            }
        }
    }

    @Test
    public void getFirstKeySnapshotBefore() throws SourceCodeRecorderException, IOException, Exception {
        try (SnapshotsFileReader reader = new SnapshotsFileReader(recorder.getOutputFilePath().toFile())) {
            int[][] inputAndExpected = new int[][]{
                {0, 0},
                {1, 0},
                {2, 0},
                {3, 0},
                {4, 3},
                {5, 3},
                {6, 3}
            };
            for (int[] inputs : inputAndExpected) {
                int input = inputs[0];
                int expected = inputs[1];
                int actual = reader.getFirstKeySnapshotBefore(input);
                assertEquals(expected, actual);
            }
        }
    }

    @Test
    public void getSnapshotSegmentsByRange() throws SourceCodeRecorderException, IOException, Exception {
        try (SnapshotsFileReader reader = new SnapshotsFileReader(recorder.getOutputFilePath().toFile())) {
            int[][] inputAndExpected = new int[][]{
                {0, 3, 3},
                {1, 2, 1},
                {2, 6, 4},};
            for (int[] inputs : inputAndExpected) {
                int start = inputs[0];
                int end = inputs[1];
                int count = inputs[2];
                List<SnapshotFileSegment> segments = reader.getSnapshotSegmentsByRange(start, end);
                assertEquals(count, segments.size());
                boolean hasData = segments.stream().allMatch(segment -> segment.data.length > 0);
                assertTrue(hasData);
            }
        }
    }

    @Test
    public void getReplayableSnapshotSegmentsUntil() throws SourceCodeRecorderException, IOException, Exception {
        try (SnapshotsFileReader reader = new SnapshotsFileReader(recorder.getOutputFilePath().toFile())) {
            int[][] inputAndExpected = new int[][]{
                {0, 1},
                {1, 2},
                {2, 3},
                {3, 1},
                {4, 2},
                {5, 3},
                {6, 1},};
            for (int[] inputs : inputAndExpected) {
                int end = inputs[0];
                int count = inputs[1];
                List<SnapshotFileSegment> segments = reader.getReplayableSnapshotSegmentsUntil(end);
                assertEquals(count, segments.size());
                boolean hasData = segments.stream().allMatch(segment -> segment.data.length > 0);
                assertTrue(hasData);
            }
        }
    }

    @Test
    public void getIndexBeforeTime() throws SourceCodeRecorderException, IOException, Exception {
        try (SnapshotsFileReader reader = new SnapshotsFileReader(recorder.getOutputFilePath().toFile())) {
            int[][] inputAndExpected = new int[][]{
                {0, -1},
                {1, 0},
                {2, 1},
                {3, 2}
            };
            for (int[] inputs : inputAndExpected) {
                int timestamp = inputs[0];
                int expected = inputs[1];
                int actual = reader.getIndexBeforeOrEqualsTimestamp(timestamp);
                assertEquals(expected, actual);
            }
        }
    }

}

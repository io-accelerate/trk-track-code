package tdl.record.sourcecode.snapshot.file;

import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import tdl.record.sourcecode.record.SourceCodeRecorderException;
import support.TemporarySourceCodeRecorder;

public class ReaderTest {

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
    public void getSegmentAddresses() throws IOException {
        try (Reader reader = new Reader(recorder.getOutputFilePath().toFile())) {
            List<Integer> addresses = reader.getSegmentAddresses();
            System.out.println(addresses);
            Integer[] expected = new Integer[]{14, 259, 501, 789, 1276, 1565, 1813, 2061, 2187, 2313};
            assertArrayEquals(addresses.toArray(), expected);
        }
    }

    @Test
    public void next() throws IOException {
        try (Reader reader = new Reader(recorder.getOutputFilePath().toFile())) {
            assertEquals(Header.SIZE, reader.getFilePointer());
            assertTrue(reader.hasNext());

            int address1 = reader.next();
            assertEquals(14, address1);
            Segment segment1 = reader.readSegmentByAddress(address1);
            assertNotNull(segment1);
            assertEquals(Segment.TYPE_KEY, segment1.getType());
            assertEquals(139, segment1.getSize());

            int address2 = reader.next();
            assertEquals(259, address2);
            Segment segment2 = reader.readSegmentByAddress(address2);
            assertNotNull(segment2);
            assertEquals(Segment.TYPE_PATCH, segment2.getType());

            int address3 = reader.next();
            assertEquals(501, address3);
            Segment segment3 = reader.readSegmentByAddress(address3);
            assertNotNull(segment3);
            assertEquals(Segment.TYPE_PATCH, segment3.getType());

            int address4 = reader.next();
            assertEquals(789, address4);
            Segment segment4 = reader.readSegmentByAddress(address4);
            assertNotNull(segment4);
            assertEquals(Segment.TYPE_KEY, segment4.getType());

            int address5 = reader.next();
            assertEquals(1276, address5);
            Segment segment5 = reader.readSegmentByAddress(address5);
            assertNotNull(segment5);
            assertEquals(Segment.TYPE_PATCH, segment5.getType());

            int address6 = reader.next();
            assertEquals(1565, address6);
            Segment segment6 = reader.readSegmentByAddress(address6);
            assertNotNull(segment6);
            assertEquals(Segment.TYPE_PATCH, segment6.getType());
        }
    }

    @Test
    public void skip() throws IOException {

        try (Reader reader = new Reader(recorder.getOutputFilePath().toFile())) {
            assertTrue(reader.hasNext());
            assertThat(reader.nextSegment().getTimestampSec(), equalTo(0L));

            assertTrue(reader.hasNext());
            reader.skip();

            assertTrue(reader.hasNext());
            assertThat(reader.nextSegment().getTimestampSec(), equalTo(2L));

            assertTrue(reader.hasNext());
        }
    }

    @Test
    public void reset() throws IOException {
        try (Reader reader = new Reader(recorder.getOutputFilePath().toFile())) {
            assertTrue(reader.hasNext());
            Segment snapshot1 = reader.nextSegment();

            assertTrue(reader.hasNext());
            reader.skip();

            assertTrue(reader.hasNext());
            reader.skip();

            assertTrue(reader.hasNext());
            reader.reset();
            assertTrue(reader.hasNext());

            Segment snapshot2 = reader.nextSegment();

            assertEquals(snapshot1, snapshot2);
        }
    }

    @Test
    public void getSnapshotAt() throws Exception {
        try (Reader reader = new Reader(recorder.getOutputFilePath().toFile())) {
            int[][] inputAndExpected = new int[][]{
                {0, 139},
                {1, 136},
                {2, 182}
            };
            for (int[] inputs : inputAndExpected) {
                int input = inputs[0];
                int expected = inputs[1];
                int actual = (int) reader.getSnapshotAt(input).getSize();
                assertEquals(expected, actual);
            }
        }
    }

    @Test
    public void getFirstKeySnapshotBefore() throws Exception {
        try (Reader reader = new Reader(recorder.getOutputFilePath().toFile())) {
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
        try (Reader reader = new Reader(recorder.getOutputFilePath().toFile())) {
            int[][] inputAndExpected = new int[][]{
                {0, 3, 3},
                {1, 2, 1},
                {2, 6, 4},};
            for (int[] inputs : inputAndExpected) {
                int start = inputs[0];
                int end = inputs[1];
                int count = inputs[2];
                List<Segment> segments = reader.getSnapshotSegmentsByRange(start, end);
                assertEquals(count, segments.size());
                boolean hasData = segments.stream().allMatch(segment -> segment.getData().length > 0);
                assertTrue(hasData);
            }
        }
    }

    @Test
    public void getReplayableSnapshotSegmentsUntil() throws SourceCodeRecorderException, IOException, Exception {
        try (Reader reader = new Reader(recorder.getOutputFilePath().toFile())) {
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
                List<Segment> segments = reader.getReplayableSnapshotSegmentsUntil(end);
                assertEquals(count, segments.size());
                boolean hasData = segments.stream().allMatch(segment -> segment.getData().length > 0);
                assertTrue(hasData);
            }
        }
    }

    @Test
    public void getIndexBeforeOrEqualsTimestamp() throws Exception {
        try (Reader reader = new Reader(recorder.getOutputFilePath().toFile())) {
            int[][] inputAndExpected = new int[][]{
                {0, 0},
                {1, 1},
                {2, 2},
                {3, 3}
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

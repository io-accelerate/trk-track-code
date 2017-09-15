package tdl.record.sourcecode.snapshot.file;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import support.time.FakeTimeSource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import support.content.MultiStepSourceCodeProvider;
import tdl.record.sourcecode.content.SourceCodeProvider;
import tdl.record.sourcecode.record.SourceCodeRecorder;
import tdl.record.sourcecode.record.SourceCodeRecorderException;

public class SnapshotsFileReaderTest {

    @Rule
    public TemporaryFolder sourceFolder = new TemporaryFolder();

    @Rule
    public TemporaryFolder destinationFolder = new TemporaryFolder();

    private Path outputFilePath;

    @Before
    public void createSnapshotFile() throws IOException, SourceCodeRecorderException {
        outputFilePath = sourceFolder.newFile("output.bin").toPath();
        Path destinationFolderPath = destinationFolder.getRoot().toPath();
        List<SourceCodeProvider> sourceCodeHistory = Arrays.asList(
                dst -> writeTextFile(dst, "test1.txt", "TEST1"), //key
                dst -> writeTextFile(dst, "test1.txt", "TEST1TEST2"), //patch
                dst -> writeTextFile(dst, "test2.txt", "TEST1TEST2"), //patch
                dst -> { //key
                    writeTextFile(dst, "test2.txt", "TEST1TEST2");
                    writeTextFile(dst, "subdir/test3.txt", "TEST3");
                },
                dst -> {/* Empty folder */ }, //patch
                dst -> writeTextFile(dst, "test1.txt", "TEST1TEST2"), //patch
                dst -> writeTextFile(dst, "test1.txt", "TEST1TEST2"), //key
                dst -> writeTextFile(dst, "test1.txt", "TEST1TEST2"), //patch
                dst -> writeTextFile(dst, "test1.txt", "TEST1TEST2"), //patch
                dst -> writeTextFile(dst, "test1.txt", "TEST1TEST2") //key
        );

        // TODO Change the KeySnapshotSpacing to be greater than 1
        SourceCodeRecorder sourceCodeRecorder = new SourceCodeRecorder.Builder(new MultiStepSourceCodeProvider(sourceCodeHistory), outputFilePath)
                .withTimeSource(new FakeTimeSource())
                .withSnapshotEvery(1, TimeUnit.SECONDS)
                .withKeySnapshotSpacing(3)
                .build();
        sourceCodeRecorder.start(Duration.of(sourceCodeHistory.size(), ChronoUnit.SECONDS));
        sourceCodeRecorder.close();
        //FileUtils.copyFile(outputFilePath.toFile(), new File("/tmp/samplesnapshot2.srcs"));
    }

    @Test
    public void next() throws IOException {
        try (SnapshotsFileReader reader = new SnapshotsFileReader(outputFilePath.toFile())) {
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

        try (SnapshotsFileReader reader = new SnapshotsFileReader(outputFilePath.toFile())) {
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
        try (SnapshotsFileReader reader = new SnapshotsFileReader(outputFilePath.toFile())) {
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
        try (SnapshotsFileReader reader = new SnapshotsFileReader(outputFilePath.toFile())) {
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
        try (SnapshotsFileReader reader = new SnapshotsFileReader(outputFilePath.toFile())) {
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
        try (SnapshotsFileReader reader = new SnapshotsFileReader(outputFilePath.toFile())) {
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
        try (SnapshotsFileReader reader = new SnapshotsFileReader(outputFilePath.toFile())) {
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
        try (SnapshotsFileReader reader = new SnapshotsFileReader(outputFilePath.toFile())) {
            int[][] inputAndExpected = new int[][]{
                {0, -1},
                {1, 0},
                {2, 1},
                {3, 2}
            };
            for (int[] inputs : inputAndExpected) {
                int timestamp = inputs[0];
                int expected = inputs[1];
                int actual = reader.getIndexBeforeTimestamp(timestamp);
                assertEquals(expected, actual);
            }
        }
    }

    private void writeTextFile(Path destinationFolder, String childFile, String content) throws IOException {
        File newFile1 = destinationFolder.resolve(childFile).toFile();
        FileUtils.writeStringToFile(newFile1, content, StandardCharsets.US_ASCII);
    }

}

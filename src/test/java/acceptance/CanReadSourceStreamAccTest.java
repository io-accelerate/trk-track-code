package acceptance;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import support.TestSourceStreamRecorder;
import tdl.record.sourcecode.snapshot.file.Segment;
import tdl.record.sourcecode.snapshot.file.Reader;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.Assert.*;

public class CanReadSourceStreamAccTest {

    @Rule
    public TemporaryFolder sourceCodeFolder = new TemporaryFolder();

    @Rule
    public TemporaryFolder outputFolder = new TemporaryFolder();


    @Test
    public void should_be_able_to_read_source_stream() throws Exception {
        Path outputFilePath = outputFolder.newFile("output.srcs").toPath();
        File outputFile = outputFilePath.toFile();
        TestSourceStreamRecorder.recordRandom(outputFilePath, 3, 1);

        Reader reader = new Reader(outputFile);

        assertEquals(3, reader.getSegments().size());
        boolean isAllIntact = reader.getSegments().stream().allMatch(this::isSnapshotIntact);
        assertTrue(isAllIntact);
    }

    @Ignore
    @Test
    public void should_be_able_to_read_large_stream() {
        //TODO Read stream with large file without running out of memory (do not load all snapshots)
    }

    @Test
    public void should_be_resilient_to_truncation() throws Exception {
        Path outputFilePath = outputFolder.newFile("output.srcs").toPath();
        File outputFile = outputFilePath.toFile();

        TestSourceStreamRecorder.recordRandom(outputFilePath, 5, 1);

        //Truncate to the zip header. If the zip header is intact, the file
        //is still considered valid.
        truncateFile(outputFile);

        Reader reader2 = new Reader(outputFile);
        List<Segment> list2 = reader2.getSegments();
        assertEquals(5, list2.size());
        boolean isAllButLastIntact = list2.subList(0, 4).stream().allMatch(this::isSnapshotIntact);
        assertTrue(isAllButLastIntact);
        assertFalse(isSnapshotIntact(list2.get(4)));
    }

    private void truncateFile(File file) throws IOException {
        int sizeOfTruncation = 1000;
        try (FileChannel outChan = new FileOutputStream(file, true).getChannel()) {
            outChan.truncate(file.length() - sizeOfTruncation);
        }
    }

    private boolean isSnapshotIntact(Segment snapshot) {
        if (snapshot.isChecksumMismatch()) {
            return false;
        }
        int count = 0;
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(snapshot.getData()))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.getName().isEmpty()) {
                    return false;
                }
                count++;
                int read = zip.read();
                if (read == -1) {
                    return false;
                }
            }
        } catch (IOException ex) {
            return false;
        }
        return count > 0;
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    @Test
    public void should_be_resilient_to_snapshot_data_corruption() throws Exception {
        Path outputFilePath = outputFolder.newFile("output.srcs").toPath();
        File outputFile = outputFilePath.toFile();

        TestSourceStreamRecorder.recordRandom(outputFilePath, 5, 1);

        long size1 = outputFile.length();
        Reader reader1 = new Reader(outputFile);
        List<Segment> list1 = reader1.getSegments();

        int start = 4 //magit number
                + Segment.HEADER_SIZE
                + (int) list1.get(0).getSize()
                + Segment.HEADER_SIZE
                + 10 //random
                ;

        corruptFileAt(start, outputFile);

        long size2 = outputFile.length();
        assertEquals(size1, size2);

        Reader reader2 = new Reader(outputFile);
        List<Segment> list2 = reader2.getSegments();
        assertEquals(5, list2.size());
        assertTrue(isSnapshotIntact(list2.get(0)));
        boolean isSnapshot2Intact = isSnapshotIntact(list2.get(1));
        assertFalse(isSnapshot2Intact);
        boolean isTheRestIntact = list2.subList(2, 5).stream().allMatch(this::isSnapshotIntact);
        assertTrue(isTheRestIntact);
    }


    @Ignore
    @Test
    public void should_be_resilient_to_key_snapshot_header_corruption() {
        //TODO On key snapshot corruption the reader should ignore all the other snapshots up to the next good key frame
    }

    @Ignore
    @Test
    public void should_be_resilient_to_patch_snapshot_header_corruption() {
        //TODO On patch snapshot corruption the reader should ignore all the other snapshots up to the next good key frame
    }


    private void corruptFileAt(int start, File file) throws IOException {
        try (RandomAccessFile accessFile = new RandomAccessFile(file, "rw")) {
            byte[] bytes = new byte[1000];
            Arrays.fill(bytes, (byte) 99);
            accessFile.seek(start);
            accessFile.write(bytes);
        }
    }
}

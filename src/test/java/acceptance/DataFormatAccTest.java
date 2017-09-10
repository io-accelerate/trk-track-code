package acceptance;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import support.time.FakeTimeSource;
import tdl.record.sourcecode.App;
import tdl.record.sourcecode.content.SourceCodeProvider;
import tdl.record.sourcecode.record.SourceCodeRecorder;
import tdl.record.sourcecode.snapshot.file.SnapshotFileSegment;
import tdl.record.sourcecode.snapshot.file.SnapshotsFileReader;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.Assert.*;

public class DataFormatAccTest {

    @Rule
    public TemporaryFolder sourceCodeFolder = new TemporaryFolder();

    @Rule
    public TemporaryFolder outputFolder = new TemporaryFolder();


    private void record(String zipFolderPath, String outputFilePath) throws IOException, InterruptedException {
        App.main(new String[]{
            "record",
            "--dir", zipFolderPath,
            "--out", outputFilePath,
            "--one-time",
            "--append"
        });
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    @Test
    public void should_be_resilient_to_premature_termination() throws Exception {
        Path outputFilePath = outputFolder.newFile("output.srcs").toPath();
        Path zipFolderPath = sourceCodeFolder.getRoot().toPath();
        File outputFile = outputFilePath.toFile();

        createRandomSnapshot(zipFolderPath.toString(), outputFilePath);

        long size1 = outputFile.length();
        SnapshotsFileReader reader1 = new SnapshotsFileReader(outputFile);
        List<SnapshotFileSegment> list1 = reader1.getSnapshots();
        assertEquals(5, list1.size());
        byte[] data1 = list1.get(4).data;
        boolean isAllIntact = list1.stream().allMatch(this::isSnapshotIntact);
        assertTrue(isAllIntact);

        //Truncate to the zip header. If the zip header is intact, the file
        //is still considered valid.
        truncateFile(outputFile);

        long size2 = outputFile.length();
        assertNotEquals(size1, size2);

        SnapshotsFileReader reader2 = new SnapshotsFileReader(outputFile);
        List<SnapshotFileSegment> list2 = reader2.getSnapshots();
        assertEquals(5, list2.size());
        byte[] data2 = list2.get(4).data;
        boolean isAllButLastIntact = list2.subList(0, 4).stream().allMatch(this::isSnapshotIntact);
        assertTrue(isAllButLastIntact);
        assertFalse(isSnapshotIntact(list2.get(4)));
        assertNotEquals(data2, data1);
    }

    private void createRandomSnapshot(String zipFolderPath, Path outputFilePath) throws Exception {
        SourceCodeProvider sourceCodeProvider =
                this::createRandomFile;
        SourceCodeRecorder sourceCodeRecorder = new SourceCodeRecorder.Builder(sourceCodeProvider, outputFilePath)
                .withTimeSource(new FakeTimeSource())
                .withSnapshotEvery(1, TimeUnit.SECONDS)
                .withKeySnapshotSpacing(1) // Only key snapshots, no patches
                .build();
        sourceCodeRecorder.start(Duration.of(5, ChronoUnit.SECONDS));
        sourceCodeRecorder.close();

    }

    private void createRandomFile(Path directoryPath) throws IOException {
        String name = UUID.randomUUID().toString();
        File newFile = directoryPath.resolve(name).toFile();
        boolean newFileResult = newFile.createNewFile();
        if (!newFileResult) {
            throw new IOException("File already exists");
        }
        FileUtils.writeStringToFile(newFile, name, StandardCharsets.US_ASCII);
        byte[] random = new byte[1000];
        new Random().nextBytes(random);
        FileUtils.writeByteArrayToFile(newFile, random, true);
    }

    private void truncateFile(File file) throws IOException {
        int sizeOfTruncation = 1000;
        try (FileChannel outChan = new FileOutputStream(file, true).getChannel()) {
            outChan.truncate(file.length() - sizeOfTruncation);
        }
    }

    private boolean isSnapshotIntact(SnapshotFileSegment snapshot) {
        if (!snapshot.isDataValid()) {
            return false;
        }
        int count = 0;
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(snapshot.data))) {
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
    public void should_be_resilient_to_data_corruption() throws Exception {
        Path outputFilePath = outputFolder.newFile("output.srcs").toPath();
        Path zipFolderPath = sourceCodeFolder.getRoot().toPath();
        File outputFile = outputFilePath.toFile();

        createRandomSnapshot(zipFolderPath.toString(), outputFilePath);

        long size1 = outputFile.length();
        SnapshotsFileReader reader1 = new SnapshotsFileReader(outputFile);
        List<SnapshotFileSegment> list1 = reader1.getSnapshots();
        assertEquals(5, list1.size());
        byte[] data1 = list1.get(4).data;
        boolean isAllIntact = list1.stream().allMatch(this::isSnapshotIntact);
        assertTrue(isAllIntact);

        int start = 4 //magit number
                + SnapshotFileSegment.HEADER_SIZE
                + (int) list1.get(0).size
                + SnapshotFileSegment.HEADER_SIZE
                + 10 //random
                ;

        corruptFileAt(start, outputFile);

        long size2 = outputFile.length();
        assertEquals(size1, size2);

        SnapshotsFileReader reader2 = new SnapshotsFileReader(outputFile);
        List<SnapshotFileSegment> list2 = reader2.getSnapshots();
        assertEquals(5, list2.size());
        byte[] data2 = list2.get(4).data;
        assertTrue(isSnapshotIntact(list2.get(0)));
        boolean isSnapshot2Intact = isSnapshotIntact(list2.get(1));
        assertFalse(isSnapshot2Intact);
        boolean isTheRestIntact = list2.subList(2, 5).stream().allMatch(this::isSnapshotIntact);
        assertTrue(isTheRestIntact);
        assertNotEquals(data2, data1);
    }

    private void corruptFileAt(int start, File file) throws IOException {
        try (RandomAccessFile accessFile = new RandomAccessFile(file, "rw")) {
            byte[] bytes = new byte[1000];
            Arrays.fill(bytes, (byte) 99);
            accessFile.seek(start);
            accessFile.write(bytes);
        }
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    public void should_minimize_the_size_of_the_stream() throws IOException {
        Path outputFilePath = outputFolder.newFile("output.bin").toPath();
        Path zipFolderPath = sourceCodeFolder.getRoot().toPath();
    }
}

package acceptance;

import support.content.MultiStepSourceCodeProvider;
import support.time.FakeTimeSource;
import tdl.record.sourcecode.App;
import tdl.record.sourcecode.content.SourceCodeProvider;
import tdl.record.sourcecode.record.SourceCodeRecorder;
import tdl.record.sourcecode.snapshot.file.SnapshotFileSegment;
import tdl.record.sourcecode.snapshot.file.SnapshotsFileReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class AppAcceptanceTest {

    @Rule
    public TemporaryFolder sourceCodeFolder = new TemporaryFolder();

    @Rule
    public TemporaryFolder outputFolder = new TemporaryFolder();

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    @Test
    public void should_be_able_to_record_history() throws Exception {
        Path outputFilePath = outputFolder.newFile("output.srcs").toPath();

        SourceCodeProvider source0 = destinationFolder ->
                writeString(destinationFolder, "test1.txt", "TEST1");
        SourceCodeProvider source1 = destinationFolder ->
                writeString(destinationFolder, "test1.txt", "TEST1TEST2");
        SourceCodeProvider source2 = destinationFolder ->
                writeString(destinationFolder, "test2.txt", "TEST1TEST2");
        SourceCodeProvider source3 = destinationFolder ->
                { /* Empty folder */ };

        MultiStepSourceCodeProvider sourceCodeProvider =
                new MultiStepSourceCodeProvider(source0, source1, source2, source3);
        SourceCodeRecorder sourceCodeRecorder = new SourceCodeRecorder.Builder(sourceCodeProvider, outputFilePath)
                .withTimeSource(new FakeTimeSource())
                .withSnapshotEvery(1, TimeUnit.SECONDS)
                .withKeySnapshotSpacing(1) // Only key snapshots, no patches
                .build();

        sourceCodeRecorder.start(Duration.of(4, ChronoUnit.SECONDS));
        sourceCodeRecorder.close();

        // Verify snapshot content
        try (SnapshotsFileReader reader = getReader(outputFilePath)) {
            List<SnapshotFileSegment> snapshots = reader.getSnapshots();
            Assert.assertEquals(4, snapshots.size());

            String content1 = getFileContentFromZipByteArray("/test1.txt", snapshots.get(0).data);
            Assert.assertEquals("TEST1", content1);

            String content2 = getFileContentFromZipByteArray("/test1.txt", snapshots.get(1).data);
            Assert.assertEquals("TEST1TEST2", content2);

            String content3 = getFileContentFromZipByteArray("/test1.txt", snapshots.get(2).data);
            Assert.assertNull(content3);

            String content4 = getFileContentFromZipByteArray("/test2.txt", snapshots.get(2).data);
            Assert.assertEquals("TEST1TEST2", content4);

            String content5 = getFileContentFromZipByteArray("/test2.txt", snapshots.get(3).data);
            Assert.assertNull(content5);
        }
    }

    private void writeString(Path destinationFolder, String childFile, String content) throws IOException {
        File newFile1 = destinationFolder.resolve(childFile).toFile();
        FileUtils.writeStringToFile(newFile1, content, StandardCharsets.US_ASCII);
    }


    private SnapshotsFileReader getReader(Path outputFilePath) throws IOException, InterruptedException {
        return new SnapshotsFileReader(outputFilePath.toFile());
    }

    private String getFileContentFromZipByteArray(String path, byte[] bytes) throws IOException {
        ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(bytes));
        ZipEntry entry;
        while ((entry = zip.getNextEntry()) != null) {
            if (entry.getName().equals(path)) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                IOUtils.copy(zip, out);
                return out.toString();
            }
        }
        return null;
    }

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
    public void should_be_resilient_to_premature_termination() throws IOException, InterruptedException {
        Path outputFilePath = outputFolder.newFile("output.bin").toPath();
        Path zipFolderPath = sourceCodeFolder.getRoot().toPath();
        File outputFile = outputFilePath.toFile();
        createRandomSnapshot(zipFolderPath.toString(), outputFilePath.toString());

        long size1 = outputFile.length();
        SnapshotsFileReader reader1 = new SnapshotsFileReader(outputFile);
        List<SnapshotFileSegment> list1 = reader1.getSnapshots();
        assertEquals(5, list1.size());
        byte[] data1 = list1.get(4).data;
        boolean isAllIntact = list1.stream().allMatch(this::isSnapshotIntact);
        assertTrue(isAllIntact);

        //Truncate to the zip header. If the zip header is intact, the file
        //is still considered valid.
        truncateFile(outputFile, 1000);

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

    private void createRandomSnapshot(String zipFolderPath, String outputFilePath) throws IOException, InterruptedException {
        createRandomFile(zipFolderPath);
        record(zipFolderPath, outputFilePath);
        createRandomFile(zipFolderPath);
        record(zipFolderPath, outputFilePath);
        createRandomFile(zipFolderPath);
        record(zipFolderPath, outputFilePath);
        createRandomFile(zipFolderPath);
        record(zipFolderPath, outputFilePath);
        createRandomFile(zipFolderPath);
        record(zipFolderPath, outputFilePath);
    }

    private void createRandomFile(String directoryPath) throws IOException {
        String name = UUID.randomUUID().toString();
        File directory = new File(directoryPath);
        File newfile = directory.toPath().resolve(name).toFile();
        newfile.createNewFile();
        FileUtils.writeStringToFile(newfile, name, StandardCharsets.US_ASCII);
        byte[] random = new byte[1000];
        new Random().nextBytes(random);
        FileUtils.writeByteArrayToFile(newfile, random, true);
    }

    private void truncateFile(File file, int size) throws IOException {
        try (FileChannel outChan = new FileOutputStream(file, true).getChannel()) {
            outChan.truncate(file.length() - size);
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
                zip.read();
            }
        } catch (IOException ex) {
            return false;
        }
        return count > 0;
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    @Test
    public void should_be_resilient_to_data_corruption() throws IOException, InterruptedException {
        Path outputFilePath = outputFolder.newFile("output.bin").toPath();
        Path zipFolderPath = sourceCodeFolder.getRoot().toPath();
        File outputFile = outputFilePath.toFile();

        createRandomSnapshot(zipFolderPath.toString(), outputFilePath.toString());

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

        corruptFile(outputFile, start, 1000);

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

    private void corruptFile(File file, int start, int size) throws IOException {
        try (RandomAccessFile accessFile = new RandomAccessFile(file, "rw")) {
            byte[] bytes = new byte[size];
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

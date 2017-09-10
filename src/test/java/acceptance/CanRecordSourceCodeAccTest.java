package acceptance;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import support.content.MultiStepSourceCodeProvider;
import support.time.FakeTimeSource;
import tdl.record.sourcecode.content.SourceCodeProvider;
import tdl.record.sourcecode.record.SourceCodeRecorder;
import tdl.record.sourcecode.snapshot.file.SnapshotFileSegment;
import tdl.record.sourcecode.snapshot.file.SnapshotsFileReader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class CanRecordSourceCodeAccTest {

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
        try (SnapshotsFileReader reader = new SnapshotsFileReader(outputFilePath.toFile())) {
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
}

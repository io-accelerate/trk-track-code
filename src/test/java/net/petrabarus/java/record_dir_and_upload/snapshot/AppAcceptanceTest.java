package net.petrabarus.java.record_dir_and_upload.snapshot;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import net.petrabarus.java.record_dir_and_upload.App;
import net.petrabarus.java.record_dir_and_upload.snapshot.SnapshotsFileReader.Snapshot;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;

public class AppAcceptanceTest {

    @Rule
    public TemporaryFolder outputFolder = new TemporaryFolder();

    @Rule
    public TemporaryFolder zipFolder = new TemporaryFolder();

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    @Test
    public void should_be_able_to_reproduce_history() throws IOException, InterruptedException {
        String outputFilePath = outputFolder.newFile("output.bin").getPath();
        String zipFolderPath = zipFolder.getRoot().getPath();

        File newFile1 = zipFolder.newFile("test1.txt");
        FileUtils.writeStringToFile(newFile1, "TEST1", StandardCharsets.US_ASCII);
        
        record(zipFolderPath, outputFilePath);

        FileUtils.writeStringToFile(newFile1, "TEST2", StandardCharsets.US_ASCII, true);

        record(zipFolderPath, outputFilePath);

        File newFile2 = zipFolder.newFile("test2.txt");
        newFile2.delete();
        FileUtils.moveFile(newFile1, newFile2);

        record(zipFolderPath, outputFilePath);

        newFile2.delete();
        try (SnapshotsFileReader reader = recordSnapshotAndGetReader(zipFolderPath, outputFilePath)) {
            List<Snapshot> snapshots = reader.getSnapshots();
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

    private SnapshotsFileReader recordSnapshotAndGetReader(String zipFolderPath, String outputFilePath) throws IOException, InterruptedException {
        record(zipFolderPath, outputFilePath);
        File outputFile = new File(outputFilePath);
        return new SnapshotsFileReader(outputFile);
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
    public void should_be_resilient_to_premature_termination() throws IOException {
        Path outputFilePath = outputFolder.newFile("output.bin").toPath();
        Path zipFolderPath = zipFolder.getRoot().toPath();
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    public void should_be_resilient_to_data_corruption() throws IOException {
        Path outputFilePath = outputFolder.newFile("output.bin").toPath();
        Path zipFolderPath = zipFolder.getRoot().toPath();
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    public void should_minimize_the_size_of_the_stream() throws IOException {
        Path outputFilePath = outputFolder.newFile("output.bin").toPath();
        Path zipFolderPath = zipFolder.getRoot().toPath();
    }
}

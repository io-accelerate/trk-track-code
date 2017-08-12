package net.petrabarus.java.record_dir_and_upload.snapshot;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import net.petrabarus.java.record_dir_and_upload.App;
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
        //TEST1
        try (SnapshotsFileReader reader = recordSnapshotAndGetReader(zipFolderPath, outputFilePath)) {
            List<Date> dates = reader.getDates();
            Assert.assertEquals(dates.size(), 1);
        }

        try (SnapshotsFileReader reader = recordSnapshotAndGetReader(zipFolderPath, outputFilePath)) {
            List<Date> dates = reader.getDates();
            Assert.assertEquals(dates.size(), 2);
        }

        try (SnapshotsFileReader reader = recordSnapshotAndGetReader(zipFolderPath, outputFilePath)) {
            List<Date> dates = reader.getDates();
            Assert.assertEquals(dates.size(), 3);
        }

        try (SnapshotsFileReader reader = recordSnapshotAndGetReader(zipFolderPath, outputFilePath)) {
            List<Date> dates = reader.getDates();
            Assert.assertEquals(dates.size(), 4);
        }

        //create thread for running app
        //wait some second
        //add file
        //join thread
    }

    private SnapshotsFileReader recordSnapshotAndGetReader(String zipFolderPath, String outputFilePath) throws IOException, InterruptedException {
        record(zipFolderPath, outputFilePath);
        File outputFile = new File(outputFilePath);
        return new SnapshotsFileReader(outputFile);
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

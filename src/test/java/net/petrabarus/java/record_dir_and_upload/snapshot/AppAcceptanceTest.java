package net.petrabarus.java.record_dir_and_upload.snapshot;

import java.io.IOException;
import java.nio.file.Path;
import net.petrabarus.java.record_dir_and_upload.App;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

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

        record(zipFolderPath, outputFilePath);
        
        record(zipFolderPath, outputFilePath);
        
        record(zipFolderPath, outputFilePath);
        
        record(zipFolderPath, outputFilePath);
        
        record(zipFolderPath, outputFilePath);

        //create thread for running app
        //wait some second
        //add file
        //join thread
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

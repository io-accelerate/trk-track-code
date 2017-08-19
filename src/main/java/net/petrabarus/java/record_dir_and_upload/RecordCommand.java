package net.petrabarus.java.record_dir_and_upload;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.petrabarus.java.record_dir_and_upload.snapshot.io.SnapshotsFileWriter;

@Parameters
public class RecordCommand {

    @Parameter(names = "--dir")
    public String dirPath;

    @Parameter(names = "--out")
    public String outputPath;

    @Parameter(names = "--delay", description = "In seconds")
    public Integer delay = 300; //in seconds

    @Parameter(names = "--one-time", description = "Whether one time or continuous")
    public Boolean isOneTime = false;

    @Parameter(names = "--append", description = "Append existing output")
    public Boolean append = false;

    private SnapshotsFileWriter writer;

    public void run() throws IOException, InterruptedException {
        writer = new SnapshotsFileWriter(Paths.get(outputPath), Paths.get(dirPath), append);
        if (isOneTime) {
            runOneTime();
        } else {
            runContinous();
        }
    }

    private void runOneTime() {
        writer.takeSnapshot();
    }

    private void runContinous() throws InterruptedException {
        registerSigtermHandler();
        while (true) {
            Logger.getLogger(App.class.getName()).log(Level.INFO, "Snapshot");
            writer.takeSnapshot();
            Thread.sleep(delay);
        }
    }

    private void registerSigtermHandler() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                writer.takeSnapshot();
            }
        });
    }
}

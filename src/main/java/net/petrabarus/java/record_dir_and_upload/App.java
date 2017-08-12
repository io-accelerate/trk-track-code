package net.petrabarus.java.record_dir_and_upload;

import com.beust.jcommander.JCommander;
import java.io.IOException;

public class App {

    public static void main(final String[] argv) throws IOException, InterruptedException {
        RecordCommand record = new RecordCommand();
        App app = new App();
        JCommander jc = JCommander.newBuilder()
                .addObject(app)
                .addCommand("record", record)
                .build();
        jc.parse(argv);
        if (jc.getParsedCommand().equals("record")) {
            record.run();
        }
    }

}

package tdl.record.sourcecode;

import com.beust.jcommander.JCommander;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;

public class App {

    public static void main(final String[] argv) throws IOException, InterruptedException {
        RecordCommand record = new RecordCommand();
        ConvertToGitCommand open = new ConvertToGitCommand();
        App app = new App();
        JCommander jc = JCommander.newBuilder()
                .addObject(app)
                .addCommand("record", record)
                .addCommand("convert-to-git", open)
                .build();
        jc.parse(argv);
        switch (jc.getParsedCommand()) {
            case "record":
                record.run();
                return;
            case "open":
                try {
                    open.run();
                } catch (GitAPIException ex) {
                    Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                }
                return;
            default:
                throw new RuntimeException("Unrecognized command");
        }
    }

}

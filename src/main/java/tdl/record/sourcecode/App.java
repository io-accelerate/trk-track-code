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
        String command = jc.getParsedCommand();
        if(command == null) {
            jc.usage();
            return;
        }

        switch (command) {
            case "record":
                record.run();
                return;
            case "convert-to-git":
                try {
                    open.run();
                } catch (GitAPIException ex) {
                    Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                }
                return;
            default:
                jc.usage();
        }
    }

}

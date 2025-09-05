package io.accelerate.track.code;

import com.beust.jcommander.JCommander;
import io.accelerate.track.code.record.SourceCodeRecorder;

import java.util.Map;

public class App {

    public static void main(final String[] argv) {
        SourceCodeRecorder.runSanityCheck();

        Map<String, Command> commandMap = Command.getCommandMap();

        App app = new App();

        JCommander.Builder builder = JCommander.newBuilder()
                .addObject(app);
        commandMap.entrySet().forEach(entry
                -> builder.addCommand(entry.getKey(), entry.getValue()));
        JCommander jc = builder.build();

        jc.parse(argv);
        String parsedCommand = jc.getParsedCommand();

        Command command = commandMap.getOrDefault(parsedCommand, null);
        if (command == null) {
            jc.usage();
            return;
        }
        command.run();

    }

}

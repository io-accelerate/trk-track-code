package tdl.record.sourcecode;

import java.util.HashMap;
import java.util.Map;

abstract public class Command {

    abstract public void run();

    public static Map<String, Command> getCommandMap() {
        return new HashMap<String, Command>() {
            {
                put("list", new ListCommand());
                put("record", new RecordCommand());
                put("export", new ExportCommand());
                put("convert-to-git", new ConvertToGitCommand());
            }
        };

    }
}

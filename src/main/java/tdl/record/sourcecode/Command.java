package tdl.record.sourcecode;

import java.util.HashMap;
import java.util.Map;

abstract public class Command {

    public static final String COMMAND_LIST = "list";

    public static final String COMMAND_RECORD = "record";

    public static final String COMMAND_EXPORT = "export";

    public static final String COMMAND_CONVERT_TO_GIT = "convert-to-git";

    abstract public void run();

    public static Map<String, Command> getCommandMap() {
        return new HashMap<String, Command>() {
            {
                put(COMMAND_LIST, new ListCommand());
                put(COMMAND_RECORD, new RecordCommand());
                put(COMMAND_EXPORT, new ExportCommand());
                put(COMMAND_CONVERT_TO_GIT, new ConvertToGitCommand());
            }
        };

    }
}

package tdl.record.sourcecode;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.nio.file.Paths;
import tdl.record.sourcecode.snapshot.file.ToGitConverter;

@Parameters(commandDescription = "Convert a SRCS file to git repository.")
class ConvertToGitCommand extends Command {

    @Parameter(names = {"-i", "--input"}, required = true, description = "The SRCS input file")
    private String inputFilePath;

    @Parameter(names = {"-o", "--output"}, required = true, description = "The destination sourceCodeProvider. Warning! It will be cleared if exists.")
    private String outputDirectoryPath;

    public void run() {
        try {
            ToGitConverter converter = new ToGitConverter(
                    Paths.get(inputFilePath),
                    Paths.get(outputDirectoryPath)
            );
            converter.convert();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}

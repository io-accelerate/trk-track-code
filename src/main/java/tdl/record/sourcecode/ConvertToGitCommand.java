package tdl.record.sourcecode;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import tdl.record.sourcecode.snapshot.file.ToGitConverter;
import tdl.record.sourcecode.snapshot.helpers.GitHelper;

@Parameters(commandDescription = "Convert a SRCS file to git repository.")
class ConvertToGitCommand extends Command {

    @Parameter(names = {"-i", "--input"}, required = true, description = "The SRCS input file")
    public String inputFilePath;

    @Parameter(names = {"-o", "--output"}, required = true, description = "The destination sourceCodeProvider. Warning! It will be cleared if exists.")
    public String outputDirectoryPath;

    public void run() {
        Path outputPath = Paths.get(outputDirectoryPath);
        boolean clearDirectory = true;
        if (GitHelper.isGitDirectory(outputPath)) {
            clearDirectory = promptShouldClearDirectory();
        }
        if (clearDirectory) {
            
        }
        try {
            ToGitConverter converter = new ToGitConverter(
                    Paths.get(inputFilePath),
                    outputPath
            );
            converter.convert();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public boolean promptShouldClearDirectory() {
        boolean isInputValid;
        do {
            Scanner reader = new Scanner(System.in);
            System.out.print("Existing git repository detected. Should I 1) append git, 2) empty? [1,2] ");
            String input = reader.nextLine().toLowerCase();
            switch (input) {
                case "1":
                    return false;
                case "2":
                    return true;
                default:
                    isInputValid = false;
                    break;
            }
        } while (!isInputValid);
        return false;
    }

    public void createDirectory() {

    }
}

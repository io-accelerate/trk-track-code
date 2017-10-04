package tdl.record.sourcecode;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import tdl.record.sourcecode.snapshot.file.ToGitConverter;
import tdl.record.sourcecode.snapshot.helpers.GitHelper;

@Parameters(commandDescription = "Convert a SRCS file to git repository.")
class ConvertToGitCommand extends Command {

    private Path outputPath;

    @Parameter(names = {"-i", "--input"}, required = true, description = "The SRCS input file")
    public String inputFilePath;

    @Parameter(names = {"-o", "--output"}, required = true, description = "The destination sourceCodeProvider. Warning! It will be cleared if exists.")
    public String outputDirectoryPath;

    @Parameter(names = {"--append-git"}, description = "Append commits if already git repository")
    public boolean appendGit = true;

    @Override
    public void run() {
        outputPath = Paths.get(outputDirectoryPath);
        createDirectoryIfNotExists();
        cleanDirectoryIfOkay();
        try {
            ToGitConverter converter = new ToGitConverter(
                    Paths.get(inputFilePath),
                    outputPath,
                    (segment) -> {
                        System.out.println("Committing timestamp: " + segment.getTimestampSec());
                    }
            );
            converter.convert();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void createDirectoryIfNotExists() {
        File file = outputPath.toFile();
        try {
            if (file.isFile()) {
                FileUtils.deleteQuietly(file);
            }
            if (!file.exists()) {
                FileUtils.forceMkdir(file);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void cleanDirectoryIfOkay() {
        if (GitHelper.isGitDirectory(outputPath) && appendGit) {
            return;
        }
        try {
            FileUtils.cleanDirectory(outputPath.toFile());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}

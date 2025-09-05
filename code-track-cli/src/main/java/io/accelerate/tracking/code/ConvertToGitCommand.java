package io.accelerate.tracking.code;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.apache.commons.io.FileUtils;
import io.accelerate.tracking.code.snapshot.file.ToGitConverter;
import io.accelerate.tracking.code.snapshot.helpers.GitHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Parameters(commandDescription = "Convert a SRCS file to git repository.")
public class ConvertToGitCommand extends Command {

    private Path outputPath;

    @Parameter(names = {"-i", "--input"}, required = true, description = "The SRCS input file")
    public String inputFilePath;

    @Parameter(names = {"-o", "--output"}, required = true, description = "The destination sourceCodeProvider. Warning! It will be cleared if exists.")
    public String outputDirectoryPath;

    @Parameter(names = {"--clean-dest"}, description = "Remove destination, even if git repo.")
    public boolean wipeDestinationRepo = false;

    @Parameter(names = {"--ignore-errors"}, description = "Continue processing even if errors occur")
    public boolean ignoreErrors = false;

    @Override
    public void run() {
        outputPath = Paths.get(outputDirectoryPath);
        createDirectoryIfNotExists();
        cleanDirectoryIfOkay();
        try {
            ToGitConverter converter = new ToGitConverter(
                    Paths.get(inputFilePath),
                    outputPath,
                    (segment) ->
                            System.out.format("Committing timestamp: %d (type: %s, size: %d, tag: %s)%n",
                                    segment.getTimestampSec(),
                                    segment.getType().name(),
                                    segment.getSize(),
                                    segment.getTag()),
                    !ignoreErrors
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
        if (!GitHelper.isGitDirectory(outputPath) || wipeDestinationRepo) {
            try {
                FileUtils.cleanDirectory(outputPath.toFile());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}

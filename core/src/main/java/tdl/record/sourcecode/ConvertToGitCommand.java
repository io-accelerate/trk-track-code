package tdl.record.sourcecode;

import com.beust.jcommander.Parameter;
import java.io.IOException;
import java.nio.file.Paths;
import tdl.record.sourcecode.snapshot.file.ToGitConverter;
import org.eclipse.jgit.api.errors.GitAPIException;

class ConvertToGitCommand {

    @Parameter(names = "--in", description = "The SRCS input file")
    private String inputFilePath;

    @Parameter(names = "--out", description = "The destination sourceCodeProvider. Warning! It will be cleared if exists.")
    private String outputDirectoryPath;

    void run() throws IOException, GitAPIException {
        ToGitConverter converter = new ToGitConverter(
                Paths.get(inputFilePath),
                Paths.get(outputDirectoryPath)
        );
        converter.convert();
    }
}

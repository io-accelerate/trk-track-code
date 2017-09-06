package net.petrabarus.java.record_dir_and_upload;

import com.beust.jcommander.Parameter;
import java.io.IOException;
import java.nio.file.Paths;
import net.petrabarus.java.record_dir_and_upload.snapshot.file.ToGitConverter;
import org.eclipse.jgit.api.errors.GitAPIException;

public class ConvertToGitCommand {

    @Parameter(names = "--in")
    public String inputFilePath;

    @Parameter(names = "--out")
    public String outputDirectoryPath;

    public void run() throws IOException, GitAPIException {
        ToGitConverter converter = new ToGitConverter(
                Paths.get(inputFilePath),
                Paths.get(outputDirectoryPath)
        );
        converter.convert();
    }
}

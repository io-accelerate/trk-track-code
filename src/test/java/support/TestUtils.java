package support;

import org.apache.commons.io.FileUtils;
import tdl.record.sourcecode.ConvertToGitCommand;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class TestUtils {
    public static void writeFile(Path destinationFolder, String childFile, String content) {
        try {
            File newFile1 = destinationFolder.resolve(childFile).toFile();
            FileUtils.writeStringToFile(newFile1, content, StandardCharsets.US_ASCII);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void exportToGit(TestGeneratedSrcsFile testSrcsFile, File outputDir) {
        ConvertToGitCommand command = new ConvertToGitCommand();

        command.inputFilePath = testSrcsFile.getFilePath().toString();
        command.outputDirectoryPath = outputDir.toString();
        command.run();
    }
}

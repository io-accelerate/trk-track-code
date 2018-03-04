package support;

import org.apache.commons.io.FileUtils;

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
}

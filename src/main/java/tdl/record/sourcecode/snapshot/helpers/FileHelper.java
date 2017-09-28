package tdl.record.sourcecode.snapshot.helpers;

import java.io.IOException;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

public class FileHelper {

    public static void deleteEmptyFiles(Path destPath) throws IOException {
        FileUtils.listFiles(destPath.toFile(), TrueFileFilter.TRUE, TrueFileFilter.TRUE)
                .forEach(file -> {
                    if (file.length() == 0) {
                        FileUtils.deleteQuietly(file);
                    }
                });
    }
}

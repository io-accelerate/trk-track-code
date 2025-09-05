package io.accelerate.tracking.code.snapshot.helpers;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.IOException;
import java.nio.file.Path;

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

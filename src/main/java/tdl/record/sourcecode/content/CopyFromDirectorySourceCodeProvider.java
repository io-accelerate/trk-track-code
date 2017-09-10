package tdl.record.sourcecode.content;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;

public class CopyFromDirectorySourceCodeProvider implements SourceCodeProvider {
    private Path sourceFolderPath;

    public CopyFromDirectorySourceCodeProvider(Path sourceFolderPath) {
        this.sourceFolderPath = sourceFolderPath;
    }

    @Override
    public void retrieveAndSaveTo(Path destinationFolder) throws IOException {
        FileUtils.copyDirectory(
                sourceFolderPath.toFile(),
                destinationFolder.toFile()
        );
    }
}

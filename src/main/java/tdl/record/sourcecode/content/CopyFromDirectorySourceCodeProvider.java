package tdl.record.sourcecode.content;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import org.apache.commons.io.filefilter.IOFileFilter;
import tdl.record.sourcecode.snapshot.helpers.ExcludeGitDirectoryFileFilter;

public class CopyFromDirectorySourceCodeProvider implements SourceCodeProvider {

    private final Path sourceFolderPath;

    private final IOFileFilter filter;

    public CopyFromDirectorySourceCodeProvider(Path sourceFolderPath) {
        this.sourceFolderPath = sourceFolderPath;
        filter = new ExcludeGitDirectoryFileFilter(sourceFolderPath);
    }

    @Override
    public void retrieveAndSaveTo(Path destinationFolder) throws IOException {
        FileUtils.copyDirectory(
                sourceFolderPath.toFile(),
                destinationFolder.toFile(),
                filter
        );
    }
}

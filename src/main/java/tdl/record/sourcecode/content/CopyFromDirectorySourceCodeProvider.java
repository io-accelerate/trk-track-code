package tdl.record.sourcecode.content;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import org.apache.commons.io.filefilter.IOFileFilter;
import tdl.record.sourcecode.snapshot.SnapshotTypeHint;
import tdl.record.sourcecode.snapshot.helpers.ExcludeGitDirectoryFileFilter;
import tdl.record.sourcecode.snapshot.helpers.GitHelper;

public class CopyFromDirectorySourceCodeProvider implements SourceCodeProvider {

    private final Path sourceFolderPath;

    private final IOFileFilter filter;

    private CopyFromGitSourceCodeProvider gitSourceCodeProvider;

    public CopyFromDirectorySourceCodeProvider(Path sourceFolderPath) {
        this.sourceFolderPath = sourceFolderPath;
        filter = new ExcludeGitDirectoryFileFilter(sourceFolderPath);
        try {
            initGitIfAvailable();
        } catch (IOException ex) {
            //Do nothing.
        }
    }

    private void initGitIfAvailable() throws IOException {
        if (!GitHelper.isGitDirectory(sourceFolderPath)) {
            return;
        }
        gitSourceCodeProvider = new CopyFromGitSourceCodeProvider(sourceFolderPath);
    }

    public boolean isGit() {
        return gitSourceCodeProvider != null;
    }

    @Override
    public SnapshotTypeHint retrieveAndSaveTo(Path destinationFolder) throws IOException {
        if (!isGit()) {
            copyDirectory(destinationFolder);
        } else {
            gitSourceCodeProvider.retrieveAndSaveTo(destinationFolder);
        }
        return SnapshotTypeHint.ANY;
    }

    private void copyDirectory(Path destinationFolder) throws IOException {
        FileUtils.copyDirectory(
                sourceFolderPath.toFile(),
                destinationFolder.toFile(),
                filter
        );
    }
}

package tdl.record.sourcecode.content;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import tdl.record.sourcecode.snapshot.SnapshotTypeHint;
import tdl.record.sourcecode.snapshot.helpers.ExcludeGitDirectoryFileFilter;
import tdl.record.sourcecode.snapshot.helpers.GitHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.apache.commons.io.FileUtils.ONE_MB;

public class CopyFromDirectorySourceCodeProvider implements SourceCodeProvider {

    private final Path sourceFolderPath;
    private final int maximumFileSizeLimitInMB;
    private final ExcludeGitDirectoryFileFilter filter;

    public CopyFromDirectorySourceCodeProvider(Path sourceFolderPath, int maximumFileSizeLimitInMB) {
        this.sourceFolderPath = sourceFolderPath;
        this.maximumFileSizeLimitInMB = maximumFileSizeLimitInMB;

        filter = new ExcludeGitDirectoryFileFilter(sourceFolderPath);
        initGitIfAvailable();
    }

    private void initGitIfAvailable() {
        if (!GitHelper.isGitDirectory(sourceFolderPath)) {
            return;
        }
    }

    @Override
    public SnapshotTypeHint retrieveAndSaveTo(Path destinationFolder) throws IOException {
        List<String> ignoredFilesPatternList = GitHelper.getIgnoredFiles(sourceFolderPath);
        copyDirectory(destinationFolder, ignoredFilesPatternList);
        return SnapshotTypeHint.ANY;
    }

    private void copyDirectory(Path destinationFolder,
                               List<String> ignoredFilesPatternList) throws IOException {
        final WildcardFileFilter ignoredFilesFilter = new WildcardFileFilter(ignoredFilesPatternList);
        final MaximumFileSizeLimitFilter maximumFileSizeLimitFilter = new MaximumFileSizeLimitFilter(maximumFileSizeLimitInMB * ONE_MB);

        final CombinedFileFilter combinedFilter =
                new CombinedFileFilter(filter, ignoredFilesFilter, maximumFileSizeLimitFilter);

        FileUtils.copyDirectory(
                sourceFolderPath.toFile(),
                destinationFolder.toFile(),
                combinedFilter
        );
    }

}

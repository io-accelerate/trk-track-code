package tdl.record.sourcecode.content;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import tdl.record.sourcecode.snapshot.SnapshotTypeHint;
import tdl.record.sourcecode.snapshot.helpers.ExcludeGitDirectoryFileFilter;
import tdl.record.sourcecode.snapshot.helpers.GitHelper;

import static org.apache.commons.io.FileUtils.ONE_MB;

public class CopyFromDirectorySourceCodeProvider implements SourceCodeProvider {

    private final Path sourceFolderPath;

    private final ExcludeGitDirectoryFileFilter filter;

    public CopyFromDirectorySourceCodeProvider(Path sourceFolderPath) {
        this.sourceFolderPath = sourceFolderPath;
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
        WildcardFileFilter ignoredFilesFilter = new WildcardFileFilter(ignoredFilesPatternList);
        MinimumFileSizeFilter minimumFileSizeFilter = new MinimumFileSizeFilter(2 * ONE_MB);

        CombinedFileFilter combinedFilter =
                new CombinedFileFilter(filter, ignoredFilesFilter, minimumFileSizeFilter);

        FileUtils.copyDirectory(
                sourceFolderPath.toFile(),
                destinationFolder.toFile(),
                combinedFilter
        );
    }

}

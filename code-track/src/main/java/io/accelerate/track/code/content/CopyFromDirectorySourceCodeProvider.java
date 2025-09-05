package io.accelerate.track.code.content;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.ignore.FastIgnoreRule;
import io.accelerate.track.code.snapshot.SnapshotTypeHint;
import io.accelerate.track.code.snapshot.helpers.GitHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.apache.commons.io.FileUtils.ONE_MB;

public class CopyFromDirectorySourceCodeProvider implements SourceCodeProvider {
    private final Path sourceFolderPath;
    private final int maximumFileSizeLimitInMB;

    public CopyFromDirectorySourceCodeProvider(Path sourceFolderPath, int maximumFileSizeLimitInMB) {
        this.sourceFolderPath = sourceFolderPath;
        this.maximumFileSizeLimitInMB = maximumFileSizeLimitInMB;
    }

    @Override
    public SnapshotTypeHint retrieveAndSaveTo(Path destinationFolder) throws IOException {
        List<FastIgnoreRule> ignoreRules = GitHelper.getIgnoredFiles(sourceFolderPath);
        copyDirectory(destinationFolder, ignoreRules);
        return SnapshotTypeHint.ANY;
    }

    private void copyDirectory(Path destinationFolder,
                               List<FastIgnoreRule> ignoreRules) throws IOException {
        final CombinedFileFilter combinedFilter = new CombinedFileFilter(
                new ExcludeGitDirectoryFileFilter(sourceFolderPath),
                new IgnoreRulesFilter(sourceFolderPath, ignoreRules),
                new MaximumFileSizeLimitFilter(maximumFileSizeLimitInMB * ONE_MB)
        );

        FileUtils.copyDirectory(
                sourceFolderPath.toFile(),
                destinationFolder.toFile(),
                combinedFilter
        );
    }

}

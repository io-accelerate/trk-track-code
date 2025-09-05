package tdl.record.sourcecode.content;

import java.io.File;
import java.io.FileFilter;

public class CombinedFileFilter implements FileFilter {
    private final ExcludeGitDirectoryFileFilter excludeGitDirectoryFileFilter;
    private final IgnoreRulesFilter ignoredFilesFilter;
    private final MaximumFileSizeLimitFilter maximumFileSizeLimitFilter;

    CombinedFileFilter(ExcludeGitDirectoryFileFilter excludeGitDirectoryFileFilter,
                       IgnoreRulesFilter ignoredFilesFilter,
                       MaximumFileSizeLimitFilter maximumFileSizeLimitFilter) {
        this.excludeGitDirectoryFileFilter = excludeGitDirectoryFileFilter;
        this.ignoredFilesFilter = ignoredFilesFilter;
        this.maximumFileSizeLimitFilter = maximumFileSizeLimitFilter;
    }

    @Override
    public boolean accept(File pathname) {
        return excludeGitDirectoryFileFilter.accept(pathname) &&
                maximumFileSizeLimitFilter.accept(pathname) &&
                ignoredFilesFilter.accept(pathname);
    }
}

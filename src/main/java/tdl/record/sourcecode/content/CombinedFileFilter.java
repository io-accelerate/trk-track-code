package tdl.record.sourcecode.content;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import tdl.record.sourcecode.snapshot.helpers.ExcludeGitDirectoryFileFilter;

import java.io.File;
import java.io.FileFilter;

public class CombinedFileFilter implements FileFilter {
    private final ExcludeGitDirectoryFileFilter excludeGitDirectoryFileFilter;
    private final WildcardFileFilter ignoredFilesFilter;
    private final MaximumFileSizeLimitFilter maximumFileSizeLimitFilter;

    CombinedFileFilter(ExcludeGitDirectoryFileFilter excludeGitDirectoryFileFilter,
                       WildcardFileFilter ignoredFilesFilter,
                       MaximumFileSizeLimitFilter maximumFileSizeLimitFilter) {
        this.excludeGitDirectoryFileFilter = excludeGitDirectoryFileFilter;
        this.ignoredFilesFilter = ignoredFilesFilter;
        this.maximumFileSizeLimitFilter = maximumFileSizeLimitFilter;
    }

    @Override
    public boolean accept(File pathname) {
        return excludeGitDirectoryFileFilter.accept(pathname) &&
                !ignoredFilesFilter.accept(pathname) &&
                maximumFileSizeLimitFilter.accept(pathname);
    }
}

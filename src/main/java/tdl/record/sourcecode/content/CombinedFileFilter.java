package tdl.record.sourcecode.content;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import tdl.record.sourcecode.snapshot.helpers.ExcludeGitDirectoryFileFilter;

import java.io.File;
import java.io.FileFilter;

public class CombinedFileFilter implements FileFilter {
    private final ExcludeGitDirectoryFileFilter excludeGitDirectoryFileFilter;
    private final WildcardFileFilter ignoredFilesFilter;
    private final MinimumFileSizeFilter minimumFileSizeFilter;

    CombinedFileFilter(ExcludeGitDirectoryFileFilter excludeGitDirectoryFileFilter,
                       WildcardFileFilter ignoredFilesFilter,
                       MinimumFileSizeFilter minimumFileSizeFilter) {
        this.excludeGitDirectoryFileFilter = excludeGitDirectoryFileFilter;
        this.ignoredFilesFilter = ignoredFilesFilter;
        this.minimumFileSizeFilter = minimumFileSizeFilter;
    }

    @Override
    public boolean accept(File pathname) {
        return excludeGitDirectoryFileFilter.accept(pathname) &&
                !ignoredFilesFilter.accept(pathname) &&
                minimumFileSizeFilter.accept(pathname);
    }
}

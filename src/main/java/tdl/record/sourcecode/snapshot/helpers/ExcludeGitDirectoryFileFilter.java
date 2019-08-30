package tdl.record.sourcecode.snapshot.helpers;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import org.apache.commons.io.filefilter.IOFileFilter;

public class ExcludeGitDirectoryFileFilter implements FileFilter, IOFileFilter {

    private final Path root;

    public ExcludeGitDirectoryFileFilter(Path root) {
        this.root = root;
    }

    @Override
    public boolean accept(File pathname) {
        Path relativePath = root.relativize(pathname.toPath());
        return notPartOfGit(relativePath);
    }

    @Override
    public boolean accept(File directory, String filename) {
        Path relativePath = root.relativize(directory.toPath()).resolve(filename);
        return notPartOfGit(relativePath);
    }

    private static boolean notPartOfGit(Path relativePath) {
        return !relativePath.startsWith(".git"+ File.separator);
    }

}

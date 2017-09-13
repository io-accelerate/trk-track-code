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
        String relative = root.relativize(pathname.toPath()).toString();
        return !((pathname.isDirectory() && relative.equals(".git"))
                || relative.startsWith(".git/"));
    }

    @Override
    public boolean accept(File file, String string) {
        Path path = file.toPath().resolve(string);
        String relative = root.relativize(path).toString();
        return !((path.toFile().isDirectory() && relative.equals(".git"))
                || relative.startsWith(".git/"));
    }

}

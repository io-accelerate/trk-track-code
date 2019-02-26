package tdl.record.sourcecode.content;

import java.io.File;
import java.io.FileFilter;

public class MinimumFileSizeFilter implements FileFilter {

    private final long minimumFileSize;

    public MinimumFileSizeFilter(long minimumFileSize) {
        this.minimumFileSize = minimumFileSize;
    }

    @Override
    public boolean accept(File f) {
        return f.length() <= minimumFileSize;
    }
}

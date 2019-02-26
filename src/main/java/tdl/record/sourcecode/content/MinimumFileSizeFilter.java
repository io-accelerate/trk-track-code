package tdl.record.sourcecode.content;

import org.apache.commons.io.filefilter.IOFileFilter;

import java.io.File;
import java.io.FileFilter;

public class MinimumFileSizeFilter implements FileFilter, IOFileFilter {

    private final long minimumFileSize;

    public MinimumFileSizeFilter(long minimumFileSize) {
        this.minimumFileSize = minimumFileSize;
    }

    @Override
    public boolean accept(File f) {
        return f.length() <= minimumFileSize;
    }

    @Override
    public boolean accept(File dir, String name) {
        File file = new File(dir.toString() + File.separator + name);
        return file.length() <= minimumFileSize;
    }
}

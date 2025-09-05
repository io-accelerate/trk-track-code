package io.accelerate.track.code.content;

import java.io.File;
import java.io.FileFilter;

public class MaximumFileSizeLimitFilter implements FileFilter {

    private final long maximumFileSizeLimitInBytes;

    MaximumFileSizeLimitFilter(long maximumFileSizeLimitInBytes) {
        this.maximumFileSizeLimitInBytes = maximumFileSizeLimitInBytes;
    }

    @Override
    public boolean accept(File f) {
        return f.length() <= maximumFileSizeLimitInBytes;
    }
}

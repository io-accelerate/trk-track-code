package io.accelerate.tracking.code.content;

import org.apache.commons.io.filefilter.IOFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;

public class ExcludeGitDirectoryFileFilter implements FileFilter, IOFileFilter {

    private final Path rootDir;

    public ExcludeGitDirectoryFileFilter(Path rootDir) {
        this.rootDir = rootDir;
    }

    @Override
    public boolean accept(File targetPath) {
        Path relativePath = rootDir.relativize(targetPath.toPath());
        return notPartOfGit(relativePath);
    }

    @Override
    public boolean accept(File directory, String targetPath) {
        return accept(directory.toPath().resolve(targetPath).toFile());
    }

    private static boolean notPartOfGit(Path relativePath) {
        return !relativePath.startsWith(".git"+ File.separator);
    }

}

package tdl.record.sourcecode.content;

import org.apache.commons.io.filefilter.IOFileFilter;
import org.eclipse.jgit.ignore.FastIgnoreRule;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.util.List;

class IgnoreRulesFilter implements FileFilter, IOFileFilter {
    private final Path rootDir;
    private final List<FastIgnoreRule> ignoreRules;

    IgnoreRulesFilter(Path rootDir, List<FastIgnoreRule> ignoreRules) {
        this.rootDir = rootDir;
        this.ignoreRules = ignoreRules;
    }

    @Override
    public boolean accept(File targetPath) {
        Path relativePath = rootDir.relativize(targetPath.toPath());
        boolean isDirectory = targetPath.isDirectory();
        return !matchesIgnoreRule(relativePath, isDirectory);
    }

    @Override
    public boolean accept(File dir, String targetPath) {
        return accept(dir.toPath().resolve(targetPath).toFile());
    }

    private boolean matchesIgnoreRule(Path targetPath, boolean isDirectory) {
        boolean matchesIgnoreRule = false;
        String pathAsString = targetPath.toString();
        for (FastIgnoreRule ignoreRule : this.ignoreRules) {
            matchesIgnoreRule |= ignoreRule.isMatch(pathAsString, isDirectory);
        }
        return matchesIgnoreRule;
    }
}

package tdl.record.sourcecode.content;

import org.apache.commons.io.filefilter.IOFileFilter;
import org.eclipse.jgit.ignore.FastIgnoreRule;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

class IgnoreRulesFilter implements FileFilter, IOFileFilter {
    private final List<FastIgnoreRule> ignoreRules;

    IgnoreRulesFilter(List<FastIgnoreRule> ignoreRules) {
        this.ignoreRules = ignoreRules;
    }

    @Override
    public boolean accept(File dir, String filename) {
        return accept(dir.toPath().resolve(filename).toFile());
    }

    @Override
    public boolean accept(File pathname) {
        return !matchesIgnoreRule(pathname);
    }

    private boolean matchesIgnoreRule(File pathname) {
        boolean matchesIgnoreRule = false;
        for (FastIgnoreRule ignoreRule : this.ignoreRules) {
            matchesIgnoreRule |= ignoreRule.isMatch(pathname.getAbsolutePath(), pathname.isDirectory());
        }
        return matchesIgnoreRule;
    }
}

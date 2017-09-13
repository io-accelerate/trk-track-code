package tdl.record.sourcecode.content;

import java.io.File;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.DepthWalk.RevWalk;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;
import tdl.record.sourcecode.snapshot.helpers.ExcludeGitDirectoryFileFilter;
import tdl.record.sourcecode.snapshot.helpers.GitHelper;

public class CopyFromDirectorySourceCodeProvider implements SourceCodeProvider {

    private final Path sourceFolderPath;

    private final IOFileFilter filter;

    private CopyFromGitSourceCodeProvider gitSourceCodeProvider;

    public CopyFromDirectorySourceCodeProvider(Path sourceFolderPath) {
        this.sourceFolderPath = sourceFolderPath;
        filter = new ExcludeGitDirectoryFileFilter(sourceFolderPath);
        try {
            initGitIfAvailable();
        } catch (IOException ex) {
            //Do nothing.
        }
    }

    private void initGitIfAvailable() throws IOException {
        if (!GitHelper.isGitDirectory(sourceFolderPath)) {
            return;
        }
        gitSourceCodeProvider = new CopyFromGitSourceCodeProvider(sourceFolderPath);
    }

    public boolean isGit() {
        return gitSourceCodeProvider != null;
    }

    @Override
    public void retrieveAndSaveTo(Path destinationFolder) throws IOException {
        if (!isGit()) {
            copyDirectory(destinationFolder);
        } else {
            gitSourceCodeProvider.retrieveAndSaveTo(destinationFolder);
        }
    }

    private void copyDirectory(Path destinationFolder) throws IOException {
        FileUtils.copyDirectory(
                sourceFolderPath.toFile(),
                destinationFolder.toFile(),
                filter
        );
    }
}

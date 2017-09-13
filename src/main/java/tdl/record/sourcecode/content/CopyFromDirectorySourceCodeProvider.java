package tdl.record.sourcecode.content;

import java.io.File;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
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

    private Git git;

    public CopyFromDirectorySourceCodeProvider(Path sourceFolderPath) {
        this.sourceFolderPath = sourceFolderPath;
        filter = new ExcludeGitDirectoryFileFilter(sourceFolderPath);
        initGitIfAvailable();
    }

    private void initGitIfAvailable() {
        if (!GitHelper.isGitDirectory(sourceFolderPath)) {
            return;
        }
        try {
            git = Git.open(sourceFolderPath.toFile());
        } catch (IOException ex) {
            git = null;
        }
    }

    public boolean isGit() {
        return git != null;
    }

    @Override
    public void retrieveAndSaveTo(Path destinationFolder) throws IOException {
        if (!isGit()) {
            copyDirectory(destinationFolder);
        } else {
            walkGitAndCopyFiles(destinationFolder);
        }
    }

    private void copyDirectory(Path destinationFolder) throws IOException {
        FileUtils.copyDirectory(
                sourceFolderPath.toFile(),
                destinationFolder.toFile(),
                filter
        );
    }

    private void walkGitAndCopyFiles(Path destPath) throws IOException {
        copyTree(git, destPath);
        copyUntracked(git, destPath);
    }

    private static void copyTree(Git git, Path destPath) throws IOException {
        Repository repo = git.getRepository();

        TreeWalk treeWalk = createTreeWalkForCopying(repo);
        Path srcPath = repo.getWorkTree().toPath();

        while (treeWalk.next()) {
            String path = treeWalk.getPathString();
            copyFile(srcPath, destPath, path);
        }
    }

    private static void copyUntracked(Git git, Path destPath) {
        Path srcPath = git.getRepository().getWorkTree().toPath();
        try {
            Status status;
            status = git.status().call();
            status.getUntracked().stream().forEach((path) -> {
                try {
                    copyFile(srcPath, destPath, path);
                } catch (IOException ex) {
                    //Do nothing
                }
            });
        } catch (GitAPIException | NoWorkTreeException ex) {
            //Do nothing.
        }
    }

    private static void copyFile(Path srcPath, Path destPath, String path) throws IOException {
        Path destFile = destPath.resolve(path);
        Path srcFile = srcPath.resolve(path);
        FileUtils.copyFile(srcFile.toFile(), destFile.toFile());
    }

    private static TreeWalk createTreeWalkForCopying(Repository repo) throws IOException {
        ObjectId lastCommitId = repo.resolve(Constants.HEAD);
        RevWalk revWalk = new RevWalk(repo, 1);
        RevCommit commit = revWalk.parseCommit(lastCommitId);
        RevTree tree = commit.getTree();

        TreeWalk treeWalk = new TreeWalk(repo);
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);
        return treeWalk;
    }
}

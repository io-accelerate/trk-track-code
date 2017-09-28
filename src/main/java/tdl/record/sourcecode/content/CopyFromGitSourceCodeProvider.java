package tdl.record.sourcecode.content;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.DepthWalk;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;

public class CopyFromGitSourceCodeProvider implements SourceCodeProvider {

    private final Git git;

    public CopyFromGitSourceCodeProvider(Path sourceFolderPath) throws IOException {
        git = Git.open(sourceFolderPath.toFile());
    }

    @Override
    public void retrieveAndSaveTo(Path destinationFolder) throws IOException {
        walkGitAndCopyFiles(destinationFolder);
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
            copyFileIfExists(srcPath, destPath, path);
        }
    }

    private static void copyUntracked(Git git, Path destPath) {
        Path srcPath = git.getRepository().getWorkTree().toPath();
        try {
            Status status;
            status = git.status().call();
            status.getUntracked().stream().forEach((path) -> {
                try {
                    copyFileIfExists(srcPath, destPath, path);
                } catch (IOException ex) {
                    //Do nothing
                }
            });
        } catch (GitAPIException | NoWorkTreeException ex) {
            //Do nothing.
        }
    }

    private static void copyFileIfExists(Path srcPath, Path destPath, String path) throws IOException {
        File srcFile = srcPath.resolve(path).toFile();
        if (srcFile.exists()) {
            File destFile = destPath.resolve(path).toFile();
            FileUtils.copyFile(srcFile, destFile);
        }
    }

    private static TreeWalk createTreeWalkForCopying(Repository repo) throws IOException {
        ObjectId lastCommitId = repo.resolve(Constants.HEAD);
        DepthWalk.RevWalk revWalk = new DepthWalk.RevWalk(repo, 1);
        RevCommit commit = revWalk.parseCommit(lastCommitId);
        RevTree tree = commit.getTree();

        TreeWalk treeWalk = new TreeWalk(repo);
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);
        return treeWalk;
    }
}

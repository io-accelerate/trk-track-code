package tdl.record.sourcecode.snapshot.helpers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

import jgit.hack.ApplyCommandFixed;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.archive.ArchiveFormats;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

public class GitHelper {

    private static String ARCHIVE_FORMAT_ZIP = "zip";

    public static boolean isGitDirectory(Path path) {
        File directory = path.toFile();
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        builder.addCeilingDirectory(directory);
        builder.findGitDir(directory);
        return builder.getGitDir() != null;
    }

    public static void exportArchive(Git git, OutputStream outputStream)
            throws GitAPIException, IOException {
        ArchiveFormats.registerAll();
        git.archive()
                .setTree(git.getRepository().resolve("master"))
                .setFormat(ARCHIVE_FORMAT_ZIP)
                .setOutputStream(outputStream)
                .call();
        ArchiveFormats.unregisterAll();
    }

    public static void exportDiff(Git git, OutputStream outputStream) throws Exception {
        Repository repository = git.getRepository();
        ObjectId oldHead = repository.resolve("HEAD^^{tree}");
        ObjectId head = repository.resolve("HEAD^{tree}");
        if (oldHead == null) {
            return;
        }

        try (ObjectReader reader = repository.newObjectReader()) {
            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            oldTreeIter.reset(reader, oldHead);
            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            newTreeIter.reset(reader, head);
            git.diff()
                    .setNewTree(newTreeIter)
                    .setOldTree(oldTreeIter)
                    .setOutputStream(outputStream)
                    .call();
        }
    }

    public static void applyDiff(Git git, InputStream inputStream) throws Exception {
        // Had to use hacked command to prevent bug affecting empty files and binary files
        new ApplyCommandFixed(git.getRepository())
                .setPatch(inputStream)
                .call();
    }
}

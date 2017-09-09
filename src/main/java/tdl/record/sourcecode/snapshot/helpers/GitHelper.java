package tdl.record.sourcecode.snapshot.helpers;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.archive.ArchiveFormats;

public class GitHelper {

    public static String ARCHIVE_FORMAT_ZIP = "zip";

    public static boolean isGitDirectory(Path path) {
        File directory = path.toFile();
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        builder.addCeilingDirectory(directory);
        builder.findGitDir(directory);
        return builder.getGitDir() != null;
    }

    public static void exportGitArchive(Git git, OutputStream outputStream)
            throws GitAPIException, IOException {
        ArchiveFormats.registerAll();
        ObjectId tree = git.getRepository().resolve("HEAD");
        git.archive()
                .setTree(git.getRepository().resolve("master"))
                .setFormat(ARCHIVE_FORMAT_ZIP)
                .setOutputStream(outputStream)
                .call();
        ArchiveFormats.unregisterAll();
    }
}

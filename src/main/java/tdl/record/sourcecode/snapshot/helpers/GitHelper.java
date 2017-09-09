package tdl.record.sourcecode.snapshot.helpers;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.archive.ArchiveFormats;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

public class GitHelper {

    public static String ARCHIVE_FORMAT_ZIP = "zip";

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

    public static List<DiffEntry> exportDiff(Git git) throws Exception {
        List<DiffEntry> diffs = new ArrayList<>();
        try {
            Repository repository = git.getRepository();
            ObjectId oldHead = repository.resolve("HEAD^^{tree}");
            ObjectId head = repository.resolve("HEAD^{tree}");
            if (oldHead == null) {
                return diffs;
            }

            try (ObjectReader reader = repository.newObjectReader()) {
                CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
                oldTreeIter.reset(reader, oldHead);
                CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
                newTreeIter.reset(reader, head);
                diffs = git.diff()
                        .setNewTree(newTreeIter)
                        .setOldTree(oldTreeIter)
                        .call();
            }
        } catch (IOException | GitAPIException | RevisionSyntaxException ex) {
            //Do nothing
        }
        return diffs;
    }
}

package io.accelerate.tracking.code.snapshot.helpers;

import io.accelerate.tracking.code.record.SourceCodeRecorder;
import jgit.hack.ApplyCommandFixed;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.archive.ArchiveFormats;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.ignore.FastIgnoreRule;
import org.eclipse.jgit.ignore.IgnoreNode;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class GitHelper {
    private static final Logger log = getLogger(GitHelper.class);

    private static String ARCHIVE_FORMAT_ZIP = "zip";

    private static final String FALLBACK_DIFF_ALGORITHM = DiffAlgorithm.SupportedAlgorithm.HISTOGRAM.toString().toLowerCase();

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
        fixDiffAlgorithmIfNotSupported(repository);

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

    private static void fixDiffAlgorithmIfNotSupported(Repository repository) throws IOException, ConfigInvalidException {
        repository.getConfig().load();
        String configuredDiffAlgorithm = repository
                .getConfig()
                .getString(
                        ConfigConstants.CONFIG_DIFF_SECTION,
                        null,
                        ConfigConstants.CONFIG_KEY_ALGORITHM
                );

        DiffAlgorithm.SupportedAlgorithm supportedAlgorithm = null;
        try {
            if (configuredDiffAlgorithm != null) {
                supportedAlgorithm = DiffAlgorithm.SupportedAlgorithm.valueOf(configuredDiffAlgorithm.toUpperCase());
            }
        } catch (IllegalArgumentException ex) {
            // do nothing - means git config might has been set to an unsupported diff algorithm
        } finally {
            if (supportedAlgorithm == null) {
                repository.getConfig().setString(
                        ConfigConstants.CONFIG_DIFF_SECTION,
                        null,
                        ConfigConstants.CONFIG_KEY_ALGORITHM,
                        FALLBACK_DIFF_ALGORITHM
                );

                log.debug("Warning: local or global git config file is set to use an unsupported diff algorithm: {}", configuredDiffAlgorithm);
                log.debug("It has been overridden to use the 'histogram' diff algorithm.");
            }
        }
    }

    public static void applyDiff(Git git, InputStream inputStream) throws Exception {
        // Had to use hacked command to prevent bug affecting empty files and binary files
        new ApplyCommandFixed(git.getRepository())
                .setPatch(inputStream)
                .call();
    }

    public static void addAndCommit(Git git) throws GitAPIException {
        git.add()
                .addFilepattern(".")
                .call();
        git.commit()
                .setAll(true)
                .setMessage("Commit")
                .call();
    }

    public static int getCommitCount(Git git) throws GitAPIException {
        Iterable<RevCommit> commits = git.log().call();
        int count = 0;
        for (Object ignored : commits) {
            count++;
        }
        return count;
    }

    public static void tag(Git git, String name) throws GitAPIException {
        git.tag().setName(name).call();
    }

    public static List<String> getTags(Git git) throws GitAPIException {
        return git.tagList().call().stream()
                .map(Ref::getName)
                .map(s -> s.replaceAll("refs/tags/", ""))
                .collect(Collectors.toList());
    }

    public static List<FastIgnoreRule> getIgnoredFiles(Path sourceFolderPath) throws IOException {
        List<FastIgnoreRule> results = new ArrayList<>();
        String pathToGitIgnore = sourceFolderPath.toString() + File.separator + ".gitignore";
        if (!new File(pathToGitIgnore).exists()) {
            return results;
        }

        IgnoreNode ignoreNode = new IgnoreNode();
        InputStream gitignoreFileStream;
        gitignoreFileStream = new FileInputStream(new File(pathToGitIgnore));
        ignoreNode.parse(gitignoreFileStream);
        return ignoreNode.getRules();
    }
}

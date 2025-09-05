package io.accelerate.tracking.code.snapshot.helpers;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.util.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import io.accelerate.tracking.code.test.FileTestHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static io.accelerate.tracking.code.snapshot.helpers.GitHelper.addAndCommit;

public class JGitUnsupportedDiffAlgoIssueTest {

    @TempDir
    Path folder;

    private static Stream<Arguments> diffAlgorithm() {
        return Stream.of(
                Arguments.of("patience"),
                Arguments.of("myers"),
                Arguments.of("histogram"),
                Arguments.of("xxxx"),
                Arguments.of(""),
                Arguments.of(" "),
                Arguments.of(null, "argAddedInOrderToPassNull")
        );
    }

    private Git initialiseGit(File directory, String diffAlgorithm) throws GitAPIException {
        Git git = Git.init().setDirectory(directory).call();
        Repository repository = git.getRepository();
        repository.getConfig().setString(
            ConfigConstants.CONFIG_DIFF_SECTION,
            null,
            ConfigConstants.CONFIG_KEY_ALGORITHM,
                diffAlgorithm);
        return git;
    }

    @ParameterizedTest
    @MethodSource("diffAlgorithm")
    public void exportPatchAndApply(String diffAlgorithm) throws Exception {
        File directory = Files.createTempDirectory(folder, "dir").toFile();
        Git git = initialiseGit(directory, diffAlgorithm);
        addAndCommit(git);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            GitHelper.exportDiff(git, os);
            assertTrue(os.toByteArray().length == 0);
        }

        FileTestHelper.appendStringToFile(directory.toPath(), "file1.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory.toPath(), "file2.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory.toPath(), "file3.txt", "Test\n");
        addAndCommit(git);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            GitHelper.exportDiff(git, os);
            assertTrue(os.toByteArray().length > 0);
        }

        FileTestHelper.appendStringToFile(directory.toPath(), "file1.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory.toPath(), "file2.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory.toPath(), "file3.txt", "Test\n");
        addAndCommit(git);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            GitHelper.exportDiff(git, os);
            assertTrue(os.toByteArray().length > 0);
        }
    }

    @ParameterizedTest
    @MethodSource("diffAlgorithm")
    public void exportDiffOnEmptyFiles(String diffAlgorithm) throws Exception {
        File directory = Files.createTempDirectory(folder, "dir").toFile();
        Git git = initialiseGit(directory, diffAlgorithm);
        addAndCommit(git);
        //
        File newFile = new File(directory, "testfile.txt");
        FileUtils.createNewFile(newFile);
        assertTrue(newFile.length() == 0);
        addAndCommit(git);
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            GitHelper.exportDiff(git, os);
            //System.out.println(os.toString());
        }
    }

    @Test
    public void applyPatch() throws Exception {
        File directory1 = folder.resolve("directory1").toFile();
        File directory2 = folder.resolve("directory2").toFile();

        Git git1 = Git.init().setDirectory(directory1).call();
        addAndCommit(git1);

        Git git2 = Git.init().setDirectory(directory2).call();
        addAndCommit(git2);

        FileTestHelper.appendStringToFile(directory1.toPath(), "file1.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory1.toPath(), "file2.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory1.toPath(), "file3.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory2.toPath(), "file1.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory2.toPath(), "file2.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory2.toPath(), "file3.txt", "Test\n");

        addAndCommit(git1);
        addAndCommit(git2);

        FileTestHelper.appendStringToFile(directory1.toPath(), "file1.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory1.toPath(), "file2.txt", "Test\n");
        FileTestHelper.appendStringToFile(directory1.toPath(), "file3.txt", "Test\n");

        addAndCommit(git1);

        byte[] diff;
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            GitHelper.exportDiff(git1, os);
            diff = os.toByteArray();
        }

        try (ByteArrayInputStream is = new ByteArrayInputStream(diff)) {
            GitHelper.applyDiff(git2, is);
        }

        assertTrue(FileTestHelper.isDirectoryEqualsWithoutGit(directory1.toPath(), directory2.toPath()));
    }
}

package tdl.record.sourcecode;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import support.TestGeneratedSrcsFile;
import tdl.record.sourcecode.snapshot.SnapshotTypeHint;
import tdl.record.sourcecode.snapshot.file.ToGitConverter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static support.TestUtils.writeFile;

public class ExportSegmentsCommandTest {

    private static final boolean STOP_ON_ERRORS = true;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public TestGeneratedSrcsFile recorder = new TestGeneratedSrcsFile(Arrays.asList(
            (Path dst) -> {
                writeFile(dst, "test1.txt", "TEST1");
                return SnapshotTypeHint.KEY;
            },
            (Path dst) -> {
                writeFile(dst, "test1.txt", "TEST1TEST2");
                return SnapshotTypeHint.PATCH;
            },
            (Path dst) -> {
                writeFile(dst, "test2.txt", "TEST1TEST2");
                return SnapshotTypeHint.PATCH;
            },
            (Path dst) -> {
                writeFile(dst, "test2.txt", "TEST1TEST2");
                writeFile(dst, "subdir/test3.txt", "TEST3");
                return SnapshotTypeHint.KEY;
            },
            (Path dst) -> {
                // Empty folder
                return SnapshotTypeHint.PATCH;
            },
            (Path dst) -> {
                writeFile(dst, "test1.txt", "TEST1TEST2");
                return SnapshotTypeHint.PATCH;
            },
            (Path dst) -> {
                writeFile(dst, "test1.txt", "TEST1TEST2");
                return SnapshotTypeHint.KEY;
            },
            (Path dst) -> {
                writeFile(dst, "test1.txt", "TEST1TEST2");
                return SnapshotTypeHint.PATCH;
            },
            (Path dst) -> {
                writeFile(dst, "test1.txt", "TEST1TEST2");
                return SnapshotTypeHint.PATCH;
            },
            (Path dst) -> {
                writeFile(dst, "test1.txt", "TEST1TEST2");
                return SnapshotTypeHint.KEY;
            }
    ), Arrays.asList("tag1", "x", "tag12", "tag3", "tag3"));

    @Test
    public void run() throws IOException {
        Path inputFilePath = recorder.getFilePath();

        {
            Path newSrcsFilePath = exportTimestamp(inputFilePath, 0);
            assertTrue(newSrcsFilePath.toFile().exists());

            verifySegmentsInfo(newSrcsFilePath, Arrays.asList("type KEY", "tag1"));

            verifyFileAndFileContents(newSrcsFilePath, "test1.txt", "TEST1");
        }

        {
            Path newSrcsFilePath = exportTimestamp(inputFilePath, 1);
            assertTrue(newSrcsFilePath.toFile().exists());

            verifySegmentsInfo(newSrcsFilePath, Arrays.asList("tag1", "x", "type PATCH"));

            verifyFileAndFileContents(newSrcsFilePath, "test1.txt", "TEST1TEST2");
        }

        {
            Path newSrcsFilePath = exportTimestamp(inputFilePath, 2);
            assertTrue(newSrcsFilePath.toFile().exists());

            verifySegmentsInfo(newSrcsFilePath, Arrays.asList("tag tag1", "tag x", "type PATCH", "tag x", "tag tag12"));

            verifyFileAndFileContents(newSrcsFilePath, "test2.txt", "TEST1TEST2");
        }

        {
            Path newSrcsFilePath = exportTimestamp(inputFilePath, 3);
            assertTrue(newSrcsFilePath.toFile().exists());

            verifySegmentsInfo(newSrcsFilePath, Arrays.asList("tag tag3", "type KEY"));

            verifyFileAndFileContents(newSrcsFilePath, "test2.txt", "TEST1TEST2");
            verifyFileAndFileContents(newSrcsFilePath, "subdir/test3.txt", "TEST3");
        }

        {
            Path newSrcsFilePath = exportTag(inputFilePath, "tag12");
            assertTrue(newSrcsFilePath.toFile().exists());

            verifySegmentsInfo(newSrcsFilePath, Arrays.asList("tag tag12", "type KEY", "tag tag12", "type PATCH", "tag tag12"));

            verifyFileAndFileContents(newSrcsFilePath, "test2.txt", "TEST1TEST2");
        }

        {
            Path newSrcsFilePath = exportTag(inputFilePath, "tag3");
            assertTrue(newSrcsFilePath.toFile().exists());

            verifySegmentsInfo(newSrcsFilePath, Arrays.asList("tag tag3", "type KEY"));

            verifyFileAndFileContents(newSrcsFilePath, "test2.txt", "TEST1TEST2");
        }
    }

    private void verifySegmentsInfo(Path newSrcsFilePath, List<String> searchValues) {
        List<String> info = listInfoFrom(newSrcsFilePath);
        assertThat(info.size(), is(greaterThan(0)));
        for (String value: searchValues) {
            assertThat(String.format("%s could not be found", value),
                    info.stream()
                    .filter(eachString -> eachString.contains(value))
                    .collect(Collectors.toList())
                    .size(),
                    is(greaterThan(0)));
        }
    }

    private void verifyFileAndFileContents(Path newSrcsFilePath, String fileName, String fileContent) throws IOException {
        Path outputDir = folder.newFolder().toPath();
        ToGitConverter toGitConverter = new ToGitConverter(
                newSrcsFilePath,
                outputDir,
                System.out::println,
                STOP_ON_ERRORS);

        try {
            toGitConverter.convert();
            Path targetFile = concatenate(outputDir, "/" + fileName);
            assertThat(String.format("File %s does not exists or incorrect path/filename", fileName),
                    Files.exists(targetFile),
                    is(true));
            assertThat(String.format("File %s does not contain the expected string: %s", fileName, fileContent),
                    FileUtils.readFileToString(targetFile.toFile(), StandardCharsets.UTF_8),
                    is(fileContent));
        } catch (Exception e) {
            fail("Failed to convert the srcs file due to: " + e.getMessage());
        }
    }

    private Path concatenate(Path outputDir, String fileName) {
        return Paths.get(outputDir.toString() + fileName);
    }

    private List<String> listInfoFrom(Path newFilePath) {
        ListCommand listCommand = new ListCommand();
        listCommand.inputFilePath = newFilePath.toString();
        listCommand.run();
        return listCommand.getGatheredInfo();
    }

    private Path exportTimestamp(Path inputFilePath, int timestamp) throws IOException {
        Path filePath = folder.newFile().toPath();
        ExportSegmentsCommand exportSegmentsCommand = new ExportSegmentsCommand(inputFilePath.toString(), filePath.toString(), timestamp);
        exportSegmentsCommand.run();
        return filePath;
    }

    private Path exportTag(Path inputFilePath, String tag) throws IOException {
        Path filePath = folder.newFile().toPath();
        ExportSegmentsCommand exportSegmentsCommand = new ExportSegmentsCommand(inputFilePath.toString(), filePath.toString(), tag);
        exportSegmentsCommand.run();
        return filePath;
    }
}

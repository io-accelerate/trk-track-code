package tdl.record.sourcecode;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import support.TestGeneratedSrcsFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertTrue;
import static support.TestUtils.writeFile;

public class ExportSegmentsCommandTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public TestGeneratedSrcsFile recorder = new TestGeneratedSrcsFile(Arrays.asList(
            dst -> writeFile(dst, "test1.txt", "TEST1"), //key
            dst -> writeFile(dst, "test1.txt", "TEST1TEST2"), //patch
            dst -> writeFile(dst, "test2.txt", "TEST1TEST2"), //patch
            dst -> { //key
                writeFile(dst, "test2.txt", "TEST1TEST2");
                writeFile(dst, "subdir/test3.txt", "TEST3");
            },
            dst -> {/* Empty folder */ }, //patch
            dst -> writeFile(dst, "test1.txt", "TEST1TEST2"), //patch
            dst -> writeFile(dst, "test1.txt", "TEST1TEST2"), //key
            dst -> writeFile(dst, "test1.txt", "TEST1TEST2"), //patch
            dst -> writeFile(dst, "test1.txt", "TEST1TEST2"), //patch
            dst -> writeFile(dst, "test1.txt", "TEST1TEST2") //key
    ), Arrays.asList("tag1", "x", "tag12", "tag3", "tag3"));

    @Test
    public void run() throws IOException {
        Path inputFilePath = recorder.getFilePath();

        {
            Path newSrcsFilePath = exportTimestamp(inputFilePath, 0);
            assertTrue(newSrcsFilePath.toFile().exists());

            List<String> info = listInfoFrom(newSrcsFilePath);

            assertThat(info.size(), is(greaterThan(0)));
            assertThat(info.get(0), containsString("type KEY"));
            assertThat(info.get(0), containsString("tag1"));
        }

        {
            Path newSrcsFilePath = exportTimestamp(inputFilePath, 1);
            assertTrue(newSrcsFilePath.toFile().exists());

            List<String> info = listInfoFrom(newSrcsFilePath);

            assertThat(info.size(), is(greaterThan(0)));
            assertThat(info.get(0), containsString("tag1"));
            assertThat(info.get(1), containsString("x"));
            assertThat(info.get(1), containsString("type PATCH"));
        }

        {
            Path newSrcsFilePath = exportTimestamp(inputFilePath, 2);
            assertTrue(newSrcsFilePath.toFile().exists());

            List<String> info = listInfoFrom(newSrcsFilePath);

            assertThat(info.size(), is(greaterThan(0)));
            assertThat(info.get(0), containsString("tag tag1"));
            assertThat(info.get(1), containsString("tag x"));
            assertThat(info.get(1), containsString("type PATCH"));
            assertThat(info.get(1), containsString("tag x"));
            assertThat(info.get(2), containsString("tag tag12"));
        }

        {
            Path newSrcsFilePath = exportTimestamp(inputFilePath, 3);
            assertTrue(newSrcsFilePath.toFile().exists());

            List<String> info = listInfoFrom(newSrcsFilePath);

            assertThat(info.size(), is(greaterThan(0)));
            assertThat(info.get(0), containsString("tag tag3"));
            assertThat(info.get(0), containsString("type KEY"));
        }

        {
            Path newSrcsFilePath = exportTag(inputFilePath, "tag12");
            assertTrue(newSrcsFilePath.toFile().exists());

            List<String> info = listInfoFrom(newSrcsFilePath);

            assertThat(info.size(), is(greaterThan(0)));
            assertThat(info.get(0), containsString("tag tag12"));
            assertThat(info.get(0), containsString("type KEY"));
            assertThat(info.get(1), containsString("tag tag12"));
            assertThat(info.get(1), containsString("type PATCH"));
            assertThat(info.get(2), containsString("tag tag12"));
        }

        {
            Path newSrcsFilePath = exportTag(inputFilePath, "tag3");
            assertTrue(newSrcsFilePath.toFile().exists());

            List<String> info = listInfoFrom(newSrcsFilePath);

            assertThat(info.size(), is(greaterThan(0)));
            assertThat(info.get(0), containsString("tag tag3"));
            assertThat(info.get(0), containsString("type KEY"));
        }
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

package tdl.record.sourcecode;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import support.TestGeneratedSrcsFile;
import tdl.record.sourcecode.snapshot.SnapshotTypeHint;

import java.nio.file.Path;
import java.util.Arrays;

import static support.TestUtils.exportToGit;
import static support.TestUtils.writeFile;
import static support.recording.TestRecordingFrame.asFrame;

public class ConvertToGit_ZeroLengthPatch_EdgeTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public TestGeneratedSrcsFile srcsFile = new TestGeneratedSrcsFile(Arrays.asList(
            asFrame((Path dst) -> {
                writeFile(dst, "test.txt", "BODY\n");
                return SnapshotTypeHint.KEY;
            }),
            // No changes
            asFrame((Path dst) -> {
                writeFile(dst, "test.txt", "BODY\n");
                return SnapshotTypeHint.PATCH;
            })
    ));


    @Test
    public void exportTest() throws Exception {
        exportToGit(srcsFile, folder.newFolder());
    }
}

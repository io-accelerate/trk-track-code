package tdl.record.sourcecode;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import support.TestGeneratedSrcsFile;
import tdl.record.sourcecode.snapshot.SnapshotTypeHint;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import static support.TestUtils.exportToGit;
import static support.TestUtils.writeFile;

public class ConvertToGit_ZeroLengthPatch_EdgeTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public TestGeneratedSrcsFile srcsFile = new TestGeneratedSrcsFile(Arrays.asList(
            (Path dst) -> {
                writeFile(dst, "test.txt", "BODY\n");
                return SnapshotTypeHint.KEY;
            },
            // No changes
            (Path dst) -> {
                writeFile(dst, "test.txt", "BODY\n");
                return SnapshotTypeHint.PATCH;
            }
    ), Collections.emptyList());


    @Test
    public void exportTest() throws Exception {
        exportToGit(srcsFile, folder.newFolder());
    }
}

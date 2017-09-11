package acceptance;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import support.TestSourceStreamRecorder;
import support.content.MultiStepSourceCodeProvider;
import support.time.FakeTimeSource;
import tdl.record.sourcecode.content.SourceCodeProvider;
import tdl.record.sourcecode.record.SourceCodeRecorder;
import tdl.record.sourcecode.snapshot.file.SnapshotFileSegment;
import tdl.record.sourcecode.snapshot.file.SnapshotsFileReader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.eclipse.jgit.api.Git;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import tdl.record.sourcecode.snapshot.KeySnapshot;
import tdl.record.sourcecode.snapshot.PatchSnapshot;
import tdl.record.sourcecode.snapshot.Snapshot;

public class CanRecordSourceCodeAccTest {

    @Rule
    public TemporaryFolder sourceCodeFolder = new TemporaryFolder();

    @Rule
    public TemporaryFolder outputFolder = new TemporaryFolder();

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void should_be_able_to_record_history_at_a_given_rate() throws Exception {
        Path outputFilePath = outputFolder.newFile("output.srcs").toPath();

        // TODO Add more events including subfolder
        List<SourceCodeProvider> events = Arrays.asList(
                dst -> writeTextFile(dst, "test1.txt", "TEST1"),
                dst -> writeTextFile(dst, "test1.txt", "TEST1TEST2"),
                dst -> writeTextFile(dst, "test2.txt", "TEST1TEST2"),
                dst -> {
                    /* Empty folder */ });

        // TODO Change the KeySnapshotSpacing to be greater than 1
        SourceCodeRecorder sourceCodeRecorder = new SourceCodeRecorder.Builder(new MultiStepSourceCodeProvider(events), outputFilePath)
                .withTimeSource(new FakeTimeSource())
                .withSnapshotEvery(1, TimeUnit.SECONDS)
                .withKeySnapshotSpacing(1)
                .build();
        sourceCodeRecorder.start(Duration.of(events.size(), ChronoUnit.SECONDS));
        sourceCodeRecorder.close();

        // Verify snapshot content
        // TODO Assert on the timestamps and on the type of SNAPSHOT ( KEY / PATCH )
        try (SnapshotsFileReader reader = new SnapshotsFileReader(outputFilePath.toFile())) {
            File testDir = testFolder.getRoot();
            Git git = Git.init().setDirectory(testDir).call();
            List<SnapshotFileSegment> snapshots = reader.getSnapshots();
            Assert.assertEquals(4, snapshots.size());

            Assert.assertTrue(snapshots.get(0).data.length > 0);
            Snapshot snapshot0 = snapshots.get(0).getSnapshot();
            Assert.assertTrue(snapshot0 instanceof KeySnapshot);
//            snapshot0.restoreSnapshot(git);
//            Assert.assertEquals("TEST1", readString(testDir, "test1.txt"));

            Assert.assertTrue(snapshots.get(1).data.length > 0);
            Snapshot snapshot1 = snapshots.get(1).getSnapshot();
            Assert.assertTrue(snapshot1 instanceof KeySnapshot);
//            snapshot1.restoreSnapshot(git);
//            Assert.assertEquals("TEST1TEST2", readString(testDir, "test1.txt"));

            Assert.assertTrue(snapshots.get(2).data.length > 0);
            Snapshot snapshot2 = snapshots.get(2).getSnapshot();
            Assert.assertTrue(snapshot2 instanceof KeySnapshot);
//            snapshot2.restoreSnapshot(git);
//            Assert.assertEquals("TEST1TEST2", readString(testDir, "test1.txt"));
//            Assert.assertEquals("TEST1TEST2", readString(testDir, "test2.txt"));

            Assert.assertTrue(snapshots.get(3).data.length > 0);
            Snapshot snapshot3 = snapshots.get(3).getSnapshot();
            Assert.assertTrue(snapshot3 instanceof KeySnapshot);
//            snapshot3.restoreSnapshot(git);
//            Assert.assertEquals("TEST1TEST2", readString(testDir, "test1.txt"));
//            Assert.assertEquals("TEST1TEST2", readString(testDir, "test2.txt"));

        }
    }

    private String readString(File parent, String path) throws IOException {
        return FileUtils.readFileToString(new File(parent, path), Charset.defaultCharset());
    }

    @Test
    public void should_minimize_the_size_of_the_stream() throws Exception {
        Path onlyKeySnapshotsPath = outputFolder.newFile("only_key_snapshots.bin").toPath();
        Path patchesAndKeySnapshotsPath = outputFolder.newFile("patches_and_snapshots.bin").toPath();

        Path staticFolder = Paths.get("./src/test/resources/large_folder");
        int numberOfSnapshots = 10;
        TestSourceStreamRecorder.recordFolder(staticFolder, onlyKeySnapshotsPath,
                numberOfSnapshots, 1);
        TestSourceStreamRecorder.recordFolder(staticFolder, patchesAndKeySnapshotsPath,
                numberOfSnapshots, 5);

        long onlyKeySizeKB = onlyKeySnapshotsPath.toFile().length() / 1000;
        System.out.println("onlyKeySnapshots = " + onlyKeySizeKB + " KB");
        long patchesAndKeysSizeKB = patchesAndKeySnapshotsPath.toFile().length() / 1000;
        System.out.println("patchesAndKeySnapshots = " + patchesAndKeysSizeKB + " KB");
        assertThat("Size reduction", (int) (onlyKeySizeKB / patchesAndKeysSizeKB), equalTo(4));
    }

    //~~~~~ Helpers
    private void writeTextFile(Path destinationFolder, String childFile, String content) throws IOException {
        File newFile1 = destinationFolder.resolve(childFile).toFile();
        FileUtils.writeStringToFile(newFile1, content, StandardCharsets.US_ASCII);
    }

    private String getFileContentFromZipByteArray(String path, byte[] bytes) throws IOException {
        ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(bytes));
        ZipEntry entry;
        while ((entry = zip.getNextEntry()) != null) {
            if (entry.getName().equals(path)) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                IOUtils.copy(zip, out);
                return out.toString();
            }
        }
        return null;
    }
}

package acceptance;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import support.TestSourceStreamRecorder;
import support.content.MultiStepSourceCodeProvider;
import support.time.FakeTimeSource;
import tdl.record.sourcecode.content.SourceCodeProvider;
import tdl.record.sourcecode.record.SourceCodeRecorder;
import tdl.record.sourcecode.record.SourceCodeRecorderException;
import tdl.record.sourcecode.snapshot.file.SnapshotFileSegment;
import tdl.record.sourcecode.snapshot.file.SnapshotsFileReader;
import tdl.record.sourcecode.snapshot.helpers.DirectoryDiffUtils;
import tdl.record.sourcecode.snapshot.helpers.DirectoryPatch;
import tdl.record.sourcecode.time.SystemMonotonicTimeSource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static tdl.record.sourcecode.snapshot.file.SnapshotFileSegment.TYPE_KEY;

public class CanRecordSourceCodeAccTest {

    public static final int TIME_TO_TAKE_A_SNAPSHOT = 1000;
    public static final Duration INDEFINITE = Duration.of(999, ChronoUnit.HOURS);
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void should_be_able_to_record_history_at_a_given_rate() throws Exception {
        Path outputFilePath = testFolder.newFile("output.srcs").toPath();

        List<SourceCodeProvider> sourceCodeHistory = Arrays.asList(
                dst -> writeTextFile(dst, "test1.txt", "TEST1"),
                dst -> writeTextFile(dst, "test1.txt", "TEST1TEST2"),
                dst -> writeTextFile(dst, "test2.txt", "TEST1TEST2"),
                dst -> {
                    writeTextFile(dst, "test2.txt", "TEST1TEST2");
                    writeTextFile(dst, "subdir/test3.txt", "TEST3");
                },
                dst -> {
                    /* Empty folder */ });

        // TODO Change the KeySnapshotSpacing to be greater than 1
        SourceCodeRecorder sourceCodeRecorder = new SourceCodeRecorder.Builder(new MultiStepSourceCodeProvider(sourceCodeHistory), outputFilePath)
                .withTimeSource(new FakeTimeSource())
                .withSnapshotEvery(1, TimeUnit.SECONDS)
                .withKeySnapshotSpacing(1)
                .build();
        sourceCodeRecorder.start(Duration.of(sourceCodeHistory.size(), ChronoUnit.SECONDS));
        sourceCodeRecorder.close();

        try (SnapshotsFileReader reader = new SnapshotsFileReader(outputFilePath.toFile())) {
            List<SnapshotFileSegment> snapshots = reader.getSnapshots();
            assertSnapshotTypesAre(Arrays.asList(TYPE_KEY, TYPE_KEY, TYPE_KEY, TYPE_KEY, TYPE_KEY), snapshots);
            assertContentMatches(sourceCodeHistory, snapshots);
            assertTimestampsAreConsistentWith(1, TimeUnit.SECONDS, snapshots);
        }
    }

    @Test
    public void should_be_able_to_tag_a_particular_moment() throws Exception {
        Path outputFilePath = testFolder.newFile("tagged_snapshots.srcs").toPath();

        List<SourceCodeProvider> sourceCodeHistory = Arrays.asList(
                dst -> writeTextFile(dst, "test1.txt", "TEST1"),
                dst -> writeTextFile(dst, "test1.txt", "TEST2")
        );

        // Run a recording on a separate thread
        SourceCodeRecorder sourceCodeRecorder = new SourceCodeRecorder.Builder(new MultiStepSourceCodeProvider(sourceCodeHistory), outputFilePath)
                .withTimeSource(new SystemMonotonicTimeSource())
                .withSnapshotEvery(999, TimeUnit.HOURS)
                .withKeySnapshotSpacing(1)
                .build();
        Thread recordingThread = new Thread(() -> {
            try {
                sourceCodeRecorder.start(INDEFINITE);
            } catch (SourceCodeRecorderException e) {
                e.printStackTrace();
            }
            sourceCodeRecorder.close();
        });
        recordingThread.start();

        // Trigger the tagged snapshot
        Thread.sleep(TIME_TO_TAKE_A_SNAPSHOT);
        sourceCodeRecorder.tagCurrentState("testTag");
        Thread.sleep(TIME_TO_TAKE_A_SNAPSHOT);
        sourceCodeRecorder.stop();

        // Wait for recording to finish
        recordingThread.join();

        try (SnapshotsFileReader reader = new SnapshotsFileReader(outputFilePath.toFile())) {
            List<SnapshotFileSegment> snapshots = reader.getSnapshots();
            assertThat(snapshots.size(), is(2));
            //TODO check for the tag
        }

    }

    @Test
    public void should_minimize_the_size_of_the_stream() throws Exception {
        Path onlyKeySnapshotsPath = testFolder.newFile("only_key_snapshots.bin").toPath();
        Path patchesAndKeySnapshotsPath = testFolder.newFile("patches_and_snapshots.bin").toPath();

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

    private void assertSnapshotTypesAre(List<Integer> expectedSnapshotTypes, List<SnapshotFileSegment> actualSnapshots) {
        List<Integer> snapshotTypes = actualSnapshots.stream().map(snapshotFileSegment -> snapshotFileSegment.type)
                .collect(Collectors.toList());
        assertThat(snapshotTypes, equalTo(expectedSnapshotTypes));
    }

    private void assertContentMatches(List<SourceCodeProvider> sourceCodeHistory, List<SnapshotFileSegment> snapshots) {
        assertThat(snapshots.size(), equalTo(sourceCodeHistory.size()));
        for (int i = 0; i < snapshots.size(); i++) {
            //noinspection ConstantConditions
            assertThat("Data of snapshot " + i,
                    snapshots.get(i), hasSameData(sourceCodeHistory.get(i)));
        }
    }

    private Matcher<SnapshotFileSegment> hasSameData(SourceCodeProvider sourceCodeProvider) {
        return new TypeSafeMatcher<SnapshotFileSegment>() {

            private Path actual;
            private Path expected;

            @Override
            protected boolean matchesSafely(SnapshotFileSegment snapshotSegment) {
                try {
                    expected = testFolder.newFolder().toPath();
                    actual = testFolder.newFolder().toPath();
                    Git git = Git.init().setDirectory(actual.toFile()).call();

                    sourceCodeProvider.retrieveAndSaveTo(expected);
                    snapshotSegment.getSnapshot().restoreSnapshot(git);
                    DirectoryPatch patch = DirectoryDiffUtils.diffDirectories(expected, actual);
                    Map filtered = patch.getPatches().entrySet()
                            .stream()
                            .filter(map -> !map.getKey().startsWith(".git"))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                    return filtered.isEmpty();
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Matches the contents of the corresponding event");
            }

            @Override
            protected void describeMismatchSafely(SnapshotFileSegment snapshotSegment, Description mismatchDescription) {
                mismatchDescription.appendText("there differences detected:\n");
                mismatchDescription.appendText("expected: ").appendText(expected.toString()).appendText("\n");
                mismatchDescription.appendText("actual:   ").appendText(actual.toString());
            }
        };
    }

    private void assertTimestampsAreConsistentWith(int time, TimeUnit unit, List<SnapshotFileSegment> snapshots) {
        for (int i = 0; i < snapshots.size(); i++) {
            assertThat("Timestamp of snapshot " + i,
                    (double) snapshots.get(i).timestamp, closeTo(unit.toSeconds(time * i), 0.01));
        }
    }
}

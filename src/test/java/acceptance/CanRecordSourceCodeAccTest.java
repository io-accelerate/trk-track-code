package acceptance;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
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
import tdl.record.sourcecode.snapshot.file.SnapshotFileSegment;
import tdl.record.sourcecode.snapshot.file.SnapshotsFileReader;
import tdl.record.sourcecode.snapshot.file.ToGitConverter;
import tdl.record.sourcecode.snapshot.helpers.DirectoryDiffUtils;
import tdl.record.sourcecode.snapshot.helpers.DirectoryPatch;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static tdl.record.sourcecode.snapshot.file.SnapshotFileSegment.TYPE_KEY;

public class CanRecordSourceCodeAccTest {

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

        SourceCodeRecorder sourceCodeRecorder = new SourceCodeRecorder.Builder(new MultiStepSourceCodeProvider(sourceCodeHistory), outputFilePath)
                .withTimeSource(new FakeTimeSource())
                .withSnapshotEvery(1, TimeUnit.SECONDS)
                .withKeySnapshotSpacing(1)
                .build();
        sourceCodeRecorder.start(Duration.of(sourceCodeHistory.size(), ChronoUnit.SECONDS));
        sourceCodeRecorder.close();

        // Test the structure of the file
        try (SnapshotsFileReader reader = new SnapshotsFileReader(outputFilePath.toFile())) {
            List<SnapshotFileSegment> snapshots = reader.getSnapshots();
            assertSnapshotTypesAre(Arrays.asList(TYPE_KEY, TYPE_KEY, TYPE_KEY, TYPE_KEY, TYPE_KEY), snapshots);
            assertTimestampsAreConsistentWith(1, TimeUnit.SECONDS, snapshots);
        }

        // Test the contents of the file
        File gitExportFolder = testFolder.newFolder();
        ToGitConverter converter = new ToGitConverter(outputFilePath, gitExportFolder.toPath());
        converter.convert();
        assertContentMatches(sourceCodeHistory, gitExportFolder);
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
        List<Integer> snapshotTypes = actualSnapshots.stream().map(snapshotFileSegment -> snapshotFileSegment.getType())
                .collect(Collectors.toList());
        assertThat(snapshotTypes, equalTo(expectedSnapshotTypes));
    }

    private void assertContentMatches(List<SourceCodeProvider> sourceCodeHistory, File gitExportFolder) throws Exception {
        Git git = Git.open(gitExportFolder);
        Iterator<RevCommit> commitsIterator = git.log().call().iterator();
        List<String> commitIdsInChronologicalOrder = new ArrayList<>();
        while (commitsIterator.hasNext()) {
            commitIdsInChronologicalOrder.add(0, commitsIterator.next().getName());
        }

        assertThat("Number of captured snapshots", commitIdsInChronologicalOrder.size(), equalTo(sourceCodeHistory.size()));
        Path expected;
        for (int i = 0; i < commitIdsInChronologicalOrder.size(); i++) {            
            expected = testFolder.newFolder().toPath();
            sourceCodeHistory.get(i).retrieveAndSaveTo(expected);
            git.checkout().setName(commitIdsInChronologicalOrder.get(i)).call();

            //noinspection ConstantConditions
            assertThat("Data of snapshot " + i, gitExportFolder.toPath(), hasSameData(expected));
        }
    }

    private Matcher<Path> hasSameData(Path expected) {
        return new TypeSafeMatcher<Path>() {

            @Override
            protected boolean matchesSafely(Path actual) {
                try {
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
                description.appendText("Matches the contents of folder");
            }

            @Override
            protected void describeMismatchSafely(Path actual, Description mismatchDescription) {
                mismatchDescription.appendText("differences detected:\n");
                mismatchDescription.appendText("expected: ").appendText(expected.toString()).appendText("\n");
                mismatchDescription.appendText("actual:   ").appendText(actual.toString());
            }
        };
    }

    private void assertTimestampsAreConsistentWith(int time, TimeUnit unit, List<SnapshotFileSegment> snapshots) {
        for (int i = 0; i < snapshots.size(); i++) {
            assertThat("Timestamp of snapshot " + i,
                    (double) snapshots.get(i).getTimestamp(), closeTo(unit.toSeconds(time * i), 0.01));
        }
    }
}

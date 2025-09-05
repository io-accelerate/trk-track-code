package io.accelerate.tracking.code.test.support;

import io.accelerate.tracking.code.test.support.content.RandomSourceCodeProvider;
import io.accelerate.tracking.code.test.support.time.FakeTimeSource;
import io.accelerate.tracking.code.content.CopyFromDirectorySourceCodeProvider;
import io.accelerate.tracking.code.content.SourceCodeProvider;
import io.accelerate.tracking.code.record.SourceCodeRecorder;

import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

public class TestSourceStreamRecorder {

    private static int maximumFileSizeLimitInMB = 2;

    public static void recordFolder(Path sourceFolderPath, Path outputFilePath,
                                    int numberOfSnapshots, int keySnapshotSpacing) throws Exception {
        SourceCodeProvider sourceCodeProvider =
                new CopyFromDirectorySourceCodeProvider(sourceFolderPath, maximumFileSizeLimitInMB);
        SourceCodeRecorder sourceCodeRecorder = new SourceCodeRecorder.Builder(sourceCodeProvider, outputFilePath)
                .withTimeSource(new FakeTimeSource())
                .withSnapshotEvery(1, TimeUnit.SECONDS)
                .withKeySnapshotSpacing(keySnapshotSpacing)
                .build();
        sourceCodeRecorder.start(Duration.of(numberOfSnapshots, ChronoUnit.SECONDS));
        sourceCodeRecorder.close();
    }

    public static void recordRandom(Path outputFilePath, int numberOfSnapshots, int keySnapshotSpacing) throws Exception {
        SourceCodeProvider sourceCodeProvider =
                new RandomSourceCodeProvider();
        SourceCodeRecorder sourceCodeRecorder = new SourceCodeRecorder.Builder(sourceCodeProvider, outputFilePath)
                .withTimeSource(new FakeTimeSource())
                .withSnapshotEvery(1, TimeUnit.SECONDS)
                .withKeySnapshotSpacing(keySnapshotSpacing)
                .build();
        sourceCodeRecorder.start(Duration.of(numberOfSnapshots, ChronoUnit.SECONDS));
        sourceCodeRecorder.close();
    }

}

package tdl.record.sourcecode;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import tdl.record.sourcecode.snapshot.file.Segment;
import tdl.record.sourcecode.snapshot.file.Reader;

@Parameters(commandDescription = "Export a snapshot of a SCRS file.")
public class ExportCommand extends Command {

    @Parameter(names = {"-i", "--input"}, required = true, description = "The SRCS input file.")
    private String inputFilePath;

    @Parameter(names = {"-o", "--output"}, required = true, description = "The directory. This will be cleaned.")
    private String outputDirPath;

    @Parameter(names = {"-t", "--time"}, required = true, description = "The time in seconds.")
    private long time;

    public ExportCommand() {
    }

    public ExportCommand(String inputFilePath, String outputDirPath, long time) {
        this.inputFilePath = inputFilePath;
        this.outputDirPath = outputDirPath;
        this.time = time;
    }

    @Override
    public void run() {
        File file = Paths.get(inputFilePath).toFile();

        try (Reader reader = new Reader(file)) {
            Git git = initGit();
            int index = reader.getIndexBeforeOrEqualsTimestamp(time);
            List<Segment> segments = reader.getReplayableSnapshotSegmentsUntil(index);
            segments.forEach(segment -> {
                try {
                    segment.getSnapshot().restoreSnapshot(git);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private Git initGit() {
        try {
            File outputDir = Paths.get(outputDirPath).toFile();
            if (outputDir.isFile()) {
                throw new RuntimeException("Output is not a directory");
            } else if (!outputDir.exists()) {
                FileUtils.forceMkdir(outputDir);
            }
            FileUtils.cleanDirectory(outputDir);
            Git git = Git.init().setDirectory(outputDir).call();
            return git;
        } catch (IOException | RuntimeException | GitAPIException ex) {
            throw new RuntimeException(ex);
        }
    }
}

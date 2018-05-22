package tdl.record.sourcecode;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import tdl.record.sourcecode.snapshot.file.Reader;
import tdl.record.sourcecode.snapshot.file.Segment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

@Parameters(commandDescription = "Export a snapshot of a SCRS file.")
public class ExportCommand extends Command {

    @Parameter(names = {"-i", "--input"}, required = true, description = "The SRCS input file.")
    private String inputFilePath;

    @Parameter(names = {"-o", "--output"}, required = true, description = "The directory. This will be cleaned.")
    private String outputDirPath;

    @Parameter(names = {"-ts", "--timestamp"}, description = "Export the state of the repo at the given timestamp.")
    private long time = 0;

    @Parameter(names = {"--tag"}, description = "Export a specific tag")
    private String tag = "";


    ExportCommand() {
    }

    ExportCommand(String inputFilePath, String outputDirPath, long time) {
        this.inputFilePath = inputFilePath;
        this.outputDirPath = outputDirPath;
        this.time = time;
        this.tag = "";
    }

    ExportCommand(String inputFilePath, String outputDirPath, String tag) {
        this.inputFilePath = inputFilePath;
        this.outputDirPath = outputDirPath;
        this.time = 0;
        this.tag = tag;
    }

    @Override
    public void run() {
        File file = Paths.get(inputFilePath).toFile();


        try (Reader reader = new Reader(file)) {
            Git git = initGit();

            int index;
            if (!tag.isEmpty()) {
                index = reader.getIndexBeforeForTag(tag);
            } else {
                index = reader.getIndexBeforeOrEqualsTimestamp(time);
            }
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

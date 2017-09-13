package tdl.record.sourcecode;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Date;
import tdl.record.sourcecode.snapshot.file.SnapshotFileSegment;
import tdl.record.sourcecode.snapshot.file.SnapshotsFileReader;

@Parameters(commandDescription = "Export a snapshot of a SCRS file.")
public class ExportCommand extends Command {

    @Parameter(names = {"-i", "--input"}, required = true, description = "The SRCS input file.")
    private String inputFilePath;

    @Parameter(names = {"-t", "--time"}, required = true, description = "The time. Available format: <m>m for minute, <s>s for seconds, or <m>m<s>s for both, e.g. 1m20s")
    private String time;

    @Override
    public void run() {
        File file = Paths.get(inputFilePath).toFile();
        try (SnapshotsFileReader reader = new SnapshotsFileReader(file)) {
            Date expected = expectedDateTime(reader.getStartTimestamp());
            while (reader.hasNext()) {
                SnapshotFileSegment segment = reader.next();
                Date current = segment.getTimestampAsDate();
                if (current.before(expected)) {
                    continue;
                }
                System.out.println(expected);
                exportSnapshot(segment);
                break;
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private Date expectedDateTime(Date startTime) {
        int seconds = parseTimeToSeconds();
        return null;
    }

    private int parseTimeToSeconds() {
        return 1;
    }

    private void exportSnapshot(SnapshotFileSegment segment) {

    }
}

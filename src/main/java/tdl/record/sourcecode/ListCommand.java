package tdl.record.sourcecode;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.MessageFormat;
import tdl.record.sourcecode.snapshot.file.SnapshotFileSegment;
import tdl.record.sourcecode.snapshot.file.SnapshotsFileReader;

@Parameters(commandDescription = "List snapshots in the file.")
public class ListCommand extends Command {

    @Parameter(names = {"-i", "--input"}, description = "The SRCS input file.", required = true)
    private String inputFilePath;

    @Override
    public void run() {
        File file = Paths.get(inputFilePath).toFile();
        try (SnapshotsFileReader reader = new SnapshotsFileReader(file)) {
            int index = 0;
            while (reader.hasNext()) {
                SnapshotFileSegment segment = reader.next();
                printSnapshot(segment, index);
                index++;
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void printSnapshot(SnapshotFileSegment segment, int index) {
        String message = MessageFormat.format(
                "Snapshot #{0,number}: \n"
                + "\tTime {1,date} {1,time}",
                index, segment.getTimestampAsDate());
        System.out.println(message);

    }
}

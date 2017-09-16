package tdl.record.sourcecode;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.MessageFormat;
import org.apache.commons.codec.binary.Hex;
import tdl.record.sourcecode.snapshot.KeySnapshot;
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
        String type = segment.getSnapshot() instanceof KeySnapshot ? "KEY" : "PATCH";
        String template = "Snapshot #{0,number}: \n"
                + "\tOffset    {1,number}\n"
                + "\tType      {2}\n"
                + "\tTime      {3,number}s\n"
                + "\tSize      {4,number}b\n"
                + "\tChecksum  {5}\n";
        String checksum = Hex.encodeHexString(segment.checksum);
        long size = segment.size + SnapshotFileSegment.HEADER_SIZE;
        String message = MessageFormat.format(
                template,
                //
                index,
                segment.address,
                type,
                segment.timestamp,
                size,
                checksum
        );
        System.out.println(message);

    }
}

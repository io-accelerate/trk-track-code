package tdl.record.sourcecode;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Date;
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
            Date start = new Date(reader.getFileHeader().getTimestamp());
            int index = 0;
            while (reader.hasNext()) {
                SnapshotFileSegment segment = reader.next();
                printSnapshot(segment, index, start);
                index++;
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void printSnapshot(SnapshotFileSegment segment, int index, Date start) {
        String type = segment.getSnapshot() instanceof KeySnapshot ? "KEY" : "PATCH";
        long size = segment.size + SnapshotFileSegment.HEADER_SIZE;
        String checksum = Hex.encodeHexString(segment.checksum);
        String recorded = (new Date(start.getTime() + segment.timestamp)).toString();
        String infoLine = String.format("#%4d | recorded %40s | time %4s | type %-5s | offset %5d | size %5d | checksum %40s",
                index, recorded, segment.timestamp, type, segment.address, size, checksum);
        System.out.println(infoLine);
    }
}

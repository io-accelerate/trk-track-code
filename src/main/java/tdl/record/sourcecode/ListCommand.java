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

    private static void printSnapshot(SnapshotFileSegment segment, int index) {
        String type = segment.getSnapshot() instanceof KeySnapshot ? "KEY" : "PATCH";
        long size = segment.size + SnapshotFileSegment.HEADER_SIZE;
        String checksum = Hex.encodeHexString(segment.checksum);

        String infoLine = String.format("#%4d | time %4s | type %-5s | offset %5d | size %5d | checksum %40s",
                index, segment.timestamp, type, segment.address, size, checksum);
        System.out.println(infoLine);

    }
}

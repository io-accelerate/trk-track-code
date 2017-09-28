package tdl.record.sourcecode;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Date;
import org.apache.commons.codec.binary.Hex;
import tdl.record.sourcecode.snapshot.KeySnapshot;
import tdl.record.sourcecode.snapshot.file.Segment;
import tdl.record.sourcecode.snapshot.file.Reader;

@Parameters(commandDescription = "List snapshots in the file.")
public class ListCommand extends Command {

    @Parameter(names = {"-i", "--input"}, description = "The SRCS input file.", required = true)
    public String inputFilePath;

    @Override
    public void run() {
        File file = Paths.get(inputFilePath).toFile();
        try (Reader reader = new Reader(file)) {
            Date start = new Date(reader.getFileHeader().getTimestamp());
            System.out.println("Recording Start Time: " + start.toString());
            int index = 0;
            while (reader.hasNext()) {
                Segment segment = reader.nextSegment();
                printSnapshot(segment, index);
                index++;
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void printSnapshot(Segment segment, int index) {
        String type = segment.getSnapshot() instanceof KeySnapshot ? "KEY" : "PATCH";
        long size = segment.getSize() + Segment.HEADER_SIZE;
        String checksum = Hex.encodeHexString(segment.getChecksum());
        String infoLine = String.format("#%4d | time %4s | type %-5s | offset %5d | size %5d | checksum %8s.. | tag %s",
                index, segment.getTimestampSec(), type, segment.getAddress(), size, checksum.substring(0, 8), segment.getTag());
        System.out.println(infoLine);
    }
}

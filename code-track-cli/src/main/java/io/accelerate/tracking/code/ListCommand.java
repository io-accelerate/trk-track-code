package io.accelerate.tracking.code;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.apache.commons.codec.binary.Hex;
import io.accelerate.tracking.code.snapshot.file.Reader;
import io.accelerate.tracking.code.snapshot.file.Segment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Parameters(commandDescription = "List snapshots in the file.")
public class ListCommand extends Command {

    @Parameter(names = {"-i", "--input"}, description = "The SRCS input file.", required = true)
    public String inputFilePath;
    private List<String> gatheredInfo;

    @Override
    public void run() {
        File file = Paths.get(inputFilePath).toFile();
        try (Reader reader = new Reader(file)) {
            Date start = new Date(reader.getFileHeader().getTimestamp());
            System.out.println("Recording Start Time: " + start.toString());
            int index = 0;
            gatheredInfo = new ArrayList<>();
            while (reader.hasNext()) {
                Segment segment = reader.nextSegment();
                gatherInfo(segment, index);
                index++;
            }
            printSnapshot();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<String> getGatheredInfo() {
        return gatheredInfo;
    }

    private void printSnapshot() {
        gatheredInfo.forEach(System.out::println);
    }

    private void gatherInfo(Segment segment, int index) {
        String type = segment.getSnapshot().getType().name();
        long size = segment.getSize() + Segment.HEADER_SIZE;
        String checksum = Hex.encodeHexString(segment.getChecksum());
        gatheredInfo.add(String.format("#%4d | time %4s | type %-5s | offset %10d | size %7d | checksum %8s.. | tag %s",
                index, segment.getTimestampSec(), type, segment.getAddress(), size, checksum.substring(0, 8), segment.getTag()));
    }
}

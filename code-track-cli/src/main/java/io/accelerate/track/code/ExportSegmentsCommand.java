package io.accelerate.track.code;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import io.accelerate.track.code.snapshot.file.*;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import static org.slf4j.LoggerFactory.getLogger;

@Parameters(commandDescription = "Export segments in a SCRS file into a separate SRCS file.")
public class ExportSegmentsCommand extends Command {

    private static final boolean APPEND_TO_THE_END_OF_THE_FILE = true;
    @Parameter(names = {"-i", "--input"}, required = true, description = "The SRCS input file.")
    private String inputFilePath;

    @Parameter(names = {"-o", "--output"}, required = true, description = "Path to the output segments file. Freshly created.")
    private String outputFilePath;

    @Parameter(names = {"-ts", "--timestamp"}, description = "Export the state of the repo at the given timestamp.")
    private long timeStamp = 0;

    @Parameter(names = {"--tag"}, description = "Export a specific tag")
    private String tag = "";

    private long recordedTimestamp;
    private FileOutputStream outputStream;

    ExportSegmentsCommand() {
    }

    ExportSegmentsCommand(String inputFilePath, String outputFilePath, long timeStamp) {
        this.inputFilePath = inputFilePath;
        this.outputFilePath = outputFilePath;
        this.timeStamp = timeStamp;
        this.tag = "";
    }

    ExportSegmentsCommand(String inputFilePath, String outputFilePath, String tag) {
        this.inputFilePath = inputFilePath;
        this.outputFilePath = outputFilePath;
        this.timeStamp = 0;
        this.tag = tag;
    }

    @Override
    public void run() {
        File file = Paths.get(inputFilePath).toFile();

        try (Reader reader = new Reader(file)) {
            int index;
            if (!tag.isEmpty()) {
                index = reader.getIndexBeforeForTag(tag);
            } else {
                index = reader.getIndexBeforeOrEqualsTimestamp(timeStamp);
            }
            List<Segment> segments = reader.getReplayableSnapshotSegmentsUntil(index);
            writeSegmentsToFile(segments);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void writeSegmentsToFile(List<Segment> segmentsRetrieved) throws IOException {
        File outputFile = Paths.get(outputFilePath).toFile();
        if (outputFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            outputFile.delete();
            //noinspection ResultOfMethodCallIgnored
            outputFile.createNewFile();
        }
        TagManager tagManager = new TagManager();

        outputStream = new FileOutputStream(outputFile, APPEND_TO_THE_END_OF_THE_FILE);
        recordedTimestamp = timeStamp;

        if (outputFile.length() == 0) { //new file
            try {
                writeHeader();
            } catch (IOException ex) {
                getLogger(Writer.class.getName()).error(
                        "Warning! Encountered exception while writing header. " +
                                "Continuing to process the segments.", ex);
            }
        }

        for (Segment segment : segmentsRetrieved) {
            try {
                if (TagManager.isTag(tag)) {
                    segment.setTag(tagManager.asValidTag(tag));
                }
                IOUtils.write(segment.asBytes(), outputStream);
            } catch (IOException ex) {
                getLogger(Writer.class.getName()).error(
                        "Warning! Encountered exception while writing segments. " +
                                "Continuing to process the rest of segments.", ex);
            }
        }

        IOUtils.closeQuietly(outputStream);
    }

    private void writeHeader() throws IOException {
        Header header = new Header();
        header.setTimestamp(recordedTimestamp);
        byte[] data = header.asBytes();
        IOUtils.write(data, outputStream);
    }
}

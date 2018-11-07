package tdl.record.sourcecode;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.apache.commons.io.IOUtils;
import tdl.record.sourcecode.snapshot.Snapshot;
import tdl.record.sourcecode.snapshot.file.Header;
import tdl.record.sourcecode.snapshot.file.Reader;
import tdl.record.sourcecode.snapshot.file.Segment;
import tdl.record.sourcecode.snapshot.file.TagManager;
import tdl.record.sourcecode.snapshot.file.Writer;
import tdl.record.sourcecode.time.SystemMonotonicTimeSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Parameters(commandDescription = "Export a snapshot of a SCRS file into an SRCS file.")
public class ExportPatchCommand extends Command {

    public static final boolean APPEND_TO_THE_END_OF_THE_FILE = true;
    @Parameter(names = {"-i", "--input"}, required = true, description = "The SRCS input file.")
    private String inputFilePath;

    @Parameter(names = {"-o", "--output"}, required = true, description = "Path to the output patch file. Freshly created.")
    private String outputFilePath;

    @Parameter(names = {"-ts", "--timestamp"}, description = "Export the state of the repo at the given timestamp.")
    private List<Long> time = Collections.EMPTY_LIST;

    @Parameter(names = {"--tag"}, description = "Export a specific tag")
    private String tag = "";

    private List<Snapshot> snapshotsRetrieved = new ArrayList<>();
    private long recordedTimestamp;
    private FileOutputStream outputStream;
    private SystemMonotonicTimeSource timeSource = new SystemMonotonicTimeSource();
    ;

    ExportPatchCommand() {
    }

    ExportPatchCommand(String inputFilePath, String outputFilePath, List<Long> time) {
        this.inputFilePath = inputFilePath;
        this.outputFilePath = outputFilePath;
        this.time = time;
        this.tag = "";
    }

    ExportPatchCommand(String inputFilePath, String outputFilePath, String tag) {
        this.inputFilePath = inputFilePath;
        this.outputFilePath = outputFilePath;
        this.time = Collections.EMPTY_LIST;
        this.tag = tag;
    }

    @Override
    public void run() {
        File file = Paths.get(inputFilePath).toFile();

        try (Reader reader = new Reader(file)) {
            int index;
            for (Long eachTimeSegment : time) {
                if (!tag.isEmpty()) {
                    index = reader.getIndexBeforeForTag(tag);
                } else {
                    index = reader.getIndexBeforeOrEqualsTimestamp(eachTimeSegment);
                }
                List<Segment> segments = reader.getReplayableSnapshotSegmentsUntil(index);
                writeSegmentsToFile(segments);
                break;
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void writeSegmentsToFile(List<Segment> segmentsRetrieved) throws IOException {
        File outputFile = Paths.get(outputFilePath).toFile();
        if (outputFile.exists()) {
            outputFile.delete();
            outputFile.createNewFile();
        }
        TagManager tagManager = new TagManager();

        outputStream = new FileOutputStream(outputFile, APPEND_TO_THE_END_OF_THE_FILE);
        recordedTimestamp = time.get(0);

        if (outputFile.length() == 0) { //new file
            writeHeader();
        }

        for (Segment segment : segmentsRetrieved) {
            try {
                if (TagManager.isTag(tag)) {
                    segment.setTag(tagManager.asValidTag(tag));
                }
                IOUtils.write(segment.asBytes(), outputStream);
            } catch (IOException ex) {
                Logger.getLogger(Writer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        IOUtils.closeQuietly(outputStream);
    }

    private void writeHeader() {
        try {
            Header header = new Header();
            header.setTimestamp(recordedTimestamp);
            byte[] data = header.asBytes();
            IOUtils.write(data, outputStream);
        } catch (IOException ex) {
            Logger.getLogger(Writer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

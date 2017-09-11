package tdl.record.sourcecode.content;

import java.io.IOException;
import java.nio.file.Path;

@FunctionalInterface
public interface SourceCodeProvider {

    void retrieveAndSaveTo(Path destinationFolder) throws IOException;
}

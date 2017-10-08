package tdl.record.sourcecode.snapshot.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class BaselineReader extends Reader {

    public BaselineReader(File file) throws FileNotFoundException, IOException {
        super(file);
    }

}

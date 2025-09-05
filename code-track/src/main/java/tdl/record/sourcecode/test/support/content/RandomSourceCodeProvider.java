package tdl.record.sourcecode.test.support.content;

import org.apache.commons.io.FileUtils;
import tdl.record.sourcecode.content.SourceCodeProvider;
import tdl.record.sourcecode.snapshot.SnapshotTypeHint;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Random;
import java.util.UUID;

public class RandomSourceCodeProvider implements SourceCodeProvider{
    @Override
    public SnapshotTypeHint retrieveAndSaveTo(Path destinationFolder) throws IOException {
        createRandomFile(destinationFolder);
        return SnapshotTypeHint.ANY;
    }

    private void createRandomFile(Path directoryPath) throws IOException {
        String name = UUID.randomUUID().toString();
        File newFile = directoryPath.resolve(name).toFile();
        boolean newFileResult = newFile.createNewFile();
        if (!newFileResult) {
            throw new IOException("File already exists");
        }
        FileUtils.writeStringToFile(newFile, name, StandardCharsets.US_ASCII);
        byte[] random = new byte[1000];
        new Random().nextBytes(random);
        FileUtils.writeByteArrayToFile(newFile, random, true);
    }
}

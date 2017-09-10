package support.content;

import tdl.record.sourcecode.content.SourceCodeProvider;

import java.io.IOException;
import java.nio.file.Path;

public class MultiStepSourceCodeProvider implements SourceCodeProvider {
    private SourceCodeProvider[] sourceCodeProviders;
    private int index;

    public MultiStepSourceCodeProvider(SourceCodeProvider ... sourceCodeProviders) {
        this.sourceCodeProviders = sourceCodeProviders;
        index = 0;
    }

    @Override
    public void retrieveAndSaveTo(Path destinationFolder) throws IOException {
        if (index < sourceCodeProviders.length) {
            sourceCodeProviders[index].retrieveAndSaveTo(destinationFolder);
            index++;
        }
    }
}

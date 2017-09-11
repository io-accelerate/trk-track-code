package support.content;

import tdl.record.sourcecode.content.SourceCodeProvider;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class MultiStepSourceCodeProvider implements SourceCodeProvider {
    private List<SourceCodeProvider> sourceCodeProviders;
    private int index;

    public MultiStepSourceCodeProvider(List<SourceCodeProvider> sourceCodeProviders) {
        this.sourceCodeProviders = sourceCodeProviders;
        index = 0;
    }

    @Override
    public void retrieveAndSaveTo(Path destinationFolder) throws IOException {
        if (index < sourceCodeProviders.size()) {
            sourceCodeProviders.get(index).retrieveAndSaveTo(destinationFolder);
            index++;
        }
    }
}

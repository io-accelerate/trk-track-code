package tdl.record.sourcecode.snapshot;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import tdl.record.sourcecode.snapshot.helpers.ExcludeGitDirectoryFileFilter;
import tdl.record.sourcecode.snapshot.helpers.GitHelper;

public class KeySnapshot extends Snapshot {

    public static KeySnapshot takeSnapshotFromGit(Git git) {
        KeySnapshot keySnapshot = new KeySnapshot();
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            GitHelper.exportArchive(git, os);
            keySnapshot.data = os.toByteArray();
        } catch (GitAPIException | IOException ex) {
            throw new RuntimeException(ex);
        }
        return keySnapshot;
    }

    public static KeySnapshot createSnapshotFromBytes(byte[] data) {
        KeySnapshot snapshot = new KeySnapshot();
        snapshot.data = data;
        return snapshot;
    }

    @Override
    public void restoreSnapshot(Git git) throws Exception {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {
            File outputDir = git.getRepository().getWorkTree();
            cleanDirectory(outputDir);
            unzip(inputStream, outputDir);
        }
    }

    private void cleanDirectory(File directory) {
        IOFileFilter filter = new ExcludeGitDirectoryFileFilter(directory.toPath());
        FileUtils.listFiles(directory, filter, filter).forEach(File::delete);
    }

    public static int ZIP_BUFFER_SIZE = 1024;

    public static void unzip(InputStream inputStream, File outputDir) {
        Path outputPath = outputDir.toPath();
        try (ZipInputStream zis = new ZipInputStream(inputStream);) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                extractEntry(zis, entry, outputPath);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void extractEntry(ZipInputStream zis, ZipEntry entry, Path outputPath) throws IOException {
        Path newFilePath = outputPath.resolve(entry.getName());
        if (entry.isDirectory()) {
            Files.createDirectories(newFilePath);
            return;
        }
        if (!Files.exists(newFilePath.getParent())) {
            Files.createDirectories(newFilePath.getParent());
        }
        try (OutputStream bos = Files.newOutputStream(newFilePath)) {
            byte[] buffer = new byte[1024];
            int location;
            while ((location = zis.read(buffer)) != -1) {
                bos.write(buffer, 0, location);
            }
        }
    }
}

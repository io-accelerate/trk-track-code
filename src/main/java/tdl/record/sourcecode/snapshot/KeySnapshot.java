package tdl.record.sourcecode.snapshot;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import tdl.record.sourcecode.snapshot.helpers.DirectoryZip;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
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

    public void restoreSnapshot(Path destinationDirectory) throws IOException {
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(data))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                File file = new File(destinationDirectory.toFile(), entry.getName());
                FileUtils.copyToFile(zip, file);
            }
        }
    }

    @Override
    public void restoreSnapshot(Git git) throws Exception {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {
            File outputDir = git.getRepository().getDirectory();
            unzip(inputStream, outputDir);
        }
    }
    
    public static int ZIP_BUFFER_SIZE = 1024;

    public static void unzip(InputStream inputStream, File outputDir) {
        try (ZipInputStream zis = new ZipInputStream(inputStream);) {
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();
                File newFile = outputDir.toPath().resolve(fileName).toFile();
                FileUtils.forceMkdirParent(newFile);
                try (FileOutputStream output = new FileOutputStream(newFile)) {
                    IOUtils.copy(inputStream, output, ZIP_BUFFER_SIZE);
                }
                ze = zis.getNextEntry();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

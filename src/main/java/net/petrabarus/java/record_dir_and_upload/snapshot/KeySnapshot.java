package net.petrabarus.java.record_dir_and_upload.snapshot;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import net.petrabarus.java.record_dir_and_upload.snapshot.helpers.DirectoryZip;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class KeySnapshot extends BaseSnapshot {

    public static KeySnapshot takeSnapshotFromDirectory(Path directory) throws IOException {
        KeySnapshot keySnapshot = new KeySnapshot();
        try (ByteArrayOutputStream os = new ByteArrayOutputStream();
                DirectoryZip directorySnapshot = new DirectoryZip(directory, os);) {
            directorySnapshot.compress();
            keySnapshot.data = os.toByteArray();
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
}

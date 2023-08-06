package tdl.record.sourcecode.snapshot;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.Git;
import tdl.record.sourcecode.snapshot.helpers.GitHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class PatchSnapshot extends Snapshot {

    private PatchSnapshot() {
        super(SnapshotType.PATCH);
    }

    public static PatchSnapshot takeSnapshotFromGit(Git git) {
        PatchSnapshot snapshot = new PatchSnapshot();

        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            GitHelper.exportDiff(git, buffer);
            byte[] diff = buffer.toByteArray();
            snapshot.data = compress(diff);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return snapshot;
    }

    public static PatchSnapshot createSnapshotFromBytes(byte[] data) {
        PatchSnapshot snapshot = new PatchSnapshot();
        snapshot.data = data;
        return snapshot;
    }

    @Override
    public void restoreSnapshot(Git git) throws Exception {
        byte[] decompressed = decompress(data);
        GitHelper.applyDiff(git, new ByteArrayInputStream(decompressed));
    }

    public static byte[] compress(byte[] data) throws IOException {
        byte[] compressed;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length)) {
            try (GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
                gzip.write(data);
            }
            compressed = bos.toByteArray();
        }
        return compressed;
    }

    public static byte[] decompress(byte[] compressed) throws IOException {
        byte[] decompressed;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
                GZIPInputStream gis = new GZIPInputStream(bis)) {
            decompressed = IOUtils.toByteArray(gis);
        }
        return decompressed;
    }
}

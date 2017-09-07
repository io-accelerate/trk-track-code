package tdl.record.sourcecode.snapshot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import difflib.Delta;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import tdl.record.sourcecode.snapshot.helpers.DeltaSerializerDeserializer;
import tdl.record.sourcecode.snapshot.helpers.DirectoryDiffUtils;
import tdl.record.sourcecode.snapshot.helpers.DirectoryPatch;

public class PatchSnapshot extends Snapshot {

    public static PatchSnapshot takeSnapshotFromDirectories(Path previous, Path current) throws IOException {
        PatchSnapshot snapshot = new PatchSnapshot();
        DirectoryPatch patches = DirectoryDiffUtils.diffDirectories(previous, current);
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutput out = new ObjectOutputStream(bos)) {
            Gson gson = buildGson();
            String json = gson.toJson(patches);
            byte[] compressed = compress(json);
            out.writeObject(compressed);
            snapshot.data = bos.toByteArray();
        }
        return snapshot;
    }

    public static PatchSnapshot createSnapshotFromBytes(byte[] data) {
        PatchSnapshot snapshot = new PatchSnapshot();
        snapshot.data = data;
        return snapshot;
    }

    public void restoreSnapshot(Path destinationDirectory) throws IOException {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            byte[] compressed = (byte[]) ois.readObject();
            String json = decompress(compressed);
            Gson gson = buildGson();
            DirectoryPatch patches = gson.fromJson(json, DirectoryPatch.class);
            DirectoryDiffUtils.patch(destinationDirectory, patches);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Gson buildGson() {
        return new GsonBuilder()
                .registerTypeAdapter(Delta.class, new DeltaSerializerDeserializer.Serializer())
                .registerTypeAdapter(Delta.class, new DeltaSerializerDeserializer.Deserializer())
                .create();
    }

    public static byte[] compress(String data) throws IOException {
        byte[] compressed;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length())) {
            try (GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
                gzip.write(data.getBytes());
            }
            compressed = bos.toByteArray();
        }
        return compressed;
    }

    public static String decompress(byte[] compressed) throws IOException {
        StringBuilder sb;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
             GZIPInputStream gis = new GZIPInputStream(bis);
             BufferedReader br = new BufferedReader(new InputStreamReader(gis, "UTF-8"))) {
            sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
}

package net.petrabarus.java.record_dir_and_upload.snapshot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import difflib.Delta;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import net.petrabarus.java.record_dir_and_upload.snapshot.helpers.DeltaSerializerDeserializer;
import net.petrabarus.java.record_dir_and_upload.snapshot.helpers.DirectoryDiffUtils;
import net.petrabarus.java.record_dir_and_upload.snapshot.helpers.DirectoryPatch;

public class PatchSnapshot extends Snapshot {

    public static PatchSnapshot takeSnapshotFromDirectories(Path previous, Path current) throws IOException {
        PatchSnapshot snapshot = new PatchSnapshot();
        DirectoryPatch patches = DirectoryDiffUtils.diffDirectories(previous, current);
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutput out = new ObjectOutputStream(bos)) {
            Gson gson = buildGson();
            String json = gson.toJson(patches);
            out.writeObject(json);
            snapshot.data = bos.toByteArray();
        }
        return snapshot;
    }

    public static PatchSnapshot createSnapshotFromBytes(byte[] data) {
        PatchSnapshot snapshot = new PatchSnapshot();
        snapshot.data = data;
        return snapshot;
    }

    public void restoreSnapshot(Path destinationDirectory) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            String json = (String) ois.readObject();
            Gson gson = buildGson();
            DirectoryPatch patches = gson.fromJson(json, DirectoryPatch.class);
            DirectoryDiffUtils.patch(destinationDirectory, patches);
        }
    }

    private static Gson buildGson() {
        return new GsonBuilder()
                .registerTypeAdapter(Delta.class, new DeltaSerializerDeserializer.Serializer())
                .registerTypeAdapter(Delta.class, new DeltaSerializerDeserializer.Deserializer())
                .create();
    }
}

package net.petrabarus.java.record_dir_and_upload.snapshot.helpers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import difflib.ChangeDelta;
import difflib.Chunk;
import difflib.DeleteDelta;
import difflib.Delta;
import difflib.InsertDelta;
import java.lang.reflect.Type;

public class DeltaSerializerDeserializer {

    public static class Serializer implements JsonSerializer<Delta> {

        @Override
        public JsonElement serialize(Delta t, Type type, JsonSerializationContext jsc) {

            JsonObject object = new JsonObject();
            if (t instanceof InsertDelta) {
                object.add("type", new JsonPrimitive("insert"));
            } else if (t instanceof DeleteDelta) {
                object.add("type", new JsonPrimitive("delete"));
            } else if (t instanceof ChangeDelta) {
                object.add("type", new JsonPrimitive("change"));
            } else {
                throw new RuntimeException("Unknown type");
            }
            object.add("original", jsc.serialize(t.getOriginal()));
            object.add("revised", jsc.serialize(t.getRevised()));
            return object;
        }

    }

    public static class Deserializer implements JsonDeserializer<Delta> {

        @Override
        public Delta deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
            JsonObject json = je.getAsJsonObject();
            String deltaType = json.get("type").getAsString();
            Chunk original = jdc.deserialize(json.getAsJsonObject("original"), Chunk.class);
            Chunk revised = jdc.deserialize(json.getAsJsonObject("revised"), Chunk.class);
            switch (deltaType) {
                case "change":
                    return new ChangeDelta(original, revised);
                case "insert":
                    return new InsertDelta(original, revised);
                case "delete":
                    return new DeleteDelta(original, revised);
                default:
                    throw new RuntimeException("Unrecognized type: " + deltaType);
            }
        }

    }
}

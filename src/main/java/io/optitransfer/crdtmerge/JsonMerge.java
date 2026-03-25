package io.optitransfer.crdtmerge;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;

/**
 * Deep conflict-free JSON merge using CRDT semantics.
 * Keys unique to either side: preserved.
 * Keys in both, both objects: recursively merged.
 * Keys in both, both arrays: concatenated and deduplicated.
 * Keys in both, different scalar: B wins (LWW default).
 */
public class JsonMerge {

    public static JsonObject mergeJson(JsonObject a, JsonObject b) {
        return mergeJsonObjects(a, b);
    }

    private static JsonObject mergeJsonObjects(JsonObject a, JsonObject b) {
        JsonObject result = new JsonObject();

        Set<String> allKeys = new LinkedHashSet<>();
        for (String key : a.keySet()) allKeys.add(key);
        for (String key : b.keySet()) allKeys.add(key);

        for (String key : allKeys) {
            JsonElement valA = a.get(key);
            JsonElement valB = b.get(key);

            if (valA == null) {
                result.add(key, valB.deepCopy());
            } else if (valB == null) {
                result.add(key, valA.deepCopy());
            } else if (valA.isJsonObject() && valB.isJsonObject()) {
                result.add(key, mergeJsonObjects(valA.getAsJsonObject(), valB.getAsJsonObject()));
            } else if (valA.isJsonArray() && valB.isJsonArray()) {
                result.add(key, mergeArrays(valA.getAsJsonArray(), valB.getAsJsonArray()));
            } else if (valA.equals(valB)) {
                result.add(key, valA.deepCopy());
            } else {
                result.add(key, valB.deepCopy());
            }
        }
        return result;
    }

    private static JsonArray mergeArrays(JsonArray a, JsonArray b) {
        Set<String> seen = new LinkedHashSet<>();
        JsonArray result = new JsonArray();

        for (JsonElement item : a) {
            String key = elementKey(item);
            if (seen.add(key)) {
                result.add(item.deepCopy());
            }
        }
        for (JsonElement item : b) {
            String key = elementKey(item);
            if (seen.add(key)) {
                result.add(item.deepCopy());
            }
        }
        return result;
    }

    private static String elementKey(JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            TreeMap<String, String> sorted = new TreeMap<>();
            for (String key : obj.keySet()) {
                sorted.put(key, obj.get(key).toString());
            }
            return sorted.toString();
        } else if (element.isJsonArray()) {
            StringBuilder sb = new StringBuilder("[");
            for (JsonElement e : element.getAsJsonArray()) {
                sb.append(e.toString()).append(",");
            }
            sb.append("]");
            return sb.toString();
        } else {
            return element.toString();
        }
    }
}

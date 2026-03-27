/*
 * Copyright 2026 Ryan Gillespie
 * SPDX-License-Identifier: Apache-2.0
 *
 * Commercial licensing: data@optitransfer.ch, rgillespie83@icloud.com
 */

package io.optitransfer.crdtmerge;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;
import static org.junit.Assert.*;

public class JsonMergeTest {

    @Test
    public void testMergeDisjointKeys() {
        JsonObject a = new JsonObject();
        a.addProperty("name", "Alice");
        JsonObject b = new JsonObject();
        b.addProperty("age", 30);
        JsonObject merged = CrdtMerge.mergeJson(a, b);
        assertEquals("Alice", merged.get("name").getAsString());
        assertEquals(30, merged.get("age").getAsInt());
    }

    @Test
    public void testMergeSameKeys() {
        JsonObject a = new JsonObject();
        a.addProperty("name", "Alice");
        JsonObject b = new JsonObject();
        b.addProperty("name", "Alice");
        JsonObject merged = CrdtMerge.mergeJson(a, b);
        assertEquals("Alice", merged.get("name").getAsString());
    }

    @Test
    public void testMergeConflictBWins() {
        JsonObject a = new JsonObject();
        a.addProperty("name", "Alice");
        JsonObject b = new JsonObject();
        b.addProperty("name", "Bob");
        JsonObject merged = CrdtMerge.mergeJson(a, b);
        assertEquals("Bob", merged.get("name").getAsString());
    }

    @Test
    public void testMergeNestedObjects() {
        JsonObject a = JsonParser.parseString("{\"config\": {\"theme\": \"dark\", \"lang\": \"en\"}}").getAsJsonObject();
        JsonObject b = JsonParser.parseString("{\"config\": {\"theme\": \"light\", \"font\": \"mono\"}}").getAsJsonObject();
        JsonObject merged = CrdtMerge.mergeJson(a, b);
        JsonObject config = merged.getAsJsonObject("config");
        assertEquals("light", config.get("theme").getAsString());
        assertEquals("en", config.get("lang").getAsString());
        assertEquals("mono", config.get("font").getAsString());
    }

    @Test
    public void testMergeArrays() {
        JsonObject a = JsonParser.parseString("{\"tags\": [\"a\", \"b\"]}").getAsJsonObject();
        JsonObject b = JsonParser.parseString("{\"tags\": [\"b\", \"c\"]}").getAsJsonObject();
        JsonObject merged = CrdtMerge.mergeJson(a, b);
        JsonArray tags = merged.getAsJsonArray("tags");
        assertEquals(3, tags.size());
    }

    @Test
    public void testMergeArraysWithDuplicateObjects() {
        JsonObject a = JsonParser.parseString("{\"items\": [{\"id\": 1}]}").getAsJsonObject();
        JsonObject b = JsonParser.parseString("{\"items\": [{\"id\": 1}, {\"id\": 2}]}").getAsJsonObject();
        JsonObject merged = CrdtMerge.mergeJson(a, b);
        JsonArray items = merged.getAsJsonArray("items");
        assertEquals(2, items.size());
    }

    @Test
    public void testMergeEmptyObjects() {
        JsonObject a = new JsonObject();
        JsonObject b = new JsonObject();
        JsonObject merged = CrdtMerge.mergeJson(a, b);
        assertEquals(0, merged.size());
    }

    @Test
    public void testMergeAEmptyBHasData() {
        JsonObject a = new JsonObject();
        JsonObject b = new JsonObject();
        b.addProperty("key", "value");
        JsonObject merged = CrdtMerge.mergeJson(a, b);
        assertEquals("value", merged.get("key").getAsString());
    }

    @Test
    public void testMergeDeeplyNested() {
        JsonObject a = JsonParser.parseString(
                "{\"a\": {\"b\": {\"c\": {\"val\": 1}}}}").getAsJsonObject();
        JsonObject b = JsonParser.parseString(
                "{\"a\": {\"b\": {\"c\": {\"val\": 2, \"extra\": true}}}}").getAsJsonObject();
        JsonObject merged = CrdtMerge.mergeJson(a, b);
        JsonObject c = merged.getAsJsonObject("a").getAsJsonObject("b").getAsJsonObject("c");
        assertEquals(2, c.get("val").getAsInt());
        assertTrue(c.get("extra").getAsBoolean());
    }

    @Test
    public void testMergeMixedTypes() {
        JsonObject a = new JsonObject();
        a.addProperty("field", "string_value");
        JsonObject b = new JsonObject();
        b.add("field", JsonParser.parseString("{\"nested\": true}"));
        JsonObject merged = CrdtMerge.mergeJson(a, b);
        assertTrue(merged.get("field").isJsonObject());
    }

    @Test
    public void testMergePreservesAllKeysFromBothSides() {
        JsonObject a = JsonParser.parseString("{\"a\": 1, \"b\": 2, \"c\": 3}").getAsJsonObject();
        JsonObject b = JsonParser.parseString("{\"b\": 20, \"d\": 4, \"e\": 5}").getAsJsonObject();
        JsonObject merged = CrdtMerge.mergeJson(a, b);
        assertEquals(5, merged.size());
        assertEquals(1, merged.get("a").getAsInt());
        assertEquals(20, merged.get("b").getAsInt());
        assertEquals(3, merged.get("c").getAsInt());
        assertEquals(4, merged.get("d").getAsInt());
        assertEquals(5, merged.get("e").getAsInt());
    }
}

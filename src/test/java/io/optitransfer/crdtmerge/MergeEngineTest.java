package io.optitransfer.crdtmerge;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class MergeEngineTest {

    private Map<String, Object> row(Object... kvs) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kvs.length; i += 2) {
            m.put((String) kvs[i], kvs[i + 1]);
        }
        return m;
    }

    @Test
    public void testMergeDisjointRows() {
        List<Map<String, Object>> a = List.of(row("id", 1, "name", "Alice"));
        List<Map<String, Object>> b = List.of(row("id", 2, "name", "Bob"));
        List<Map<String, Object>> merged = CrdtMerge.merge(a, b, "id");
        assertEquals(2, merged.size());
    }

    @Test
    public void testMergeOverlappingRows() {
        List<Map<String, Object>> a = List.of(row("id", 1, "name", "Alice", "age", 30));
        List<Map<String, Object>> b = List.of(row("id", 1, "name", "Alice B", "age", 30));
        List<Map<String, Object>> merged = CrdtMerge.merge(a, b, "id");
        assertEquals(1, merged.size());
        assertEquals("Alice B", merged.get(0).get("name"));
        assertEquals(30, merged.get(0).get("age"));
    }

    @Test
    public void testMergePreservesUniqueColumns() {
        List<Map<String, Object>> a = List.of(row("id", 1, "colA", "a"));
        List<Map<String, Object>> b = List.of(row("id", 1, "colB", "b"));
        List<Map<String, Object>> merged = CrdtMerge.merge(a, b, "id");
        assertEquals(1, merged.size());
        assertEquals("a", merged.get(0).get("colA"));
        assertEquals("b", merged.get(0).get("colB"));
    }

    @Test
    public void testMergeWithTimestamp() {
        List<Map<String, Object>> a = List.of(row("id", 1, "name", "Old", "ts", 1.0));
        List<Map<String, Object>> b = List.of(row("id", 1, "name", "New", "ts", 2.0));
        List<Map<String, Object>> merged = MergeEngine.merge(a, b, "id", "ts", "latest");
        assertEquals("New", merged.get(0).get("name"));
    }

    @Test
    public void testMergeWithTimestampOlderWins() {
        List<Map<String, Object>> a = List.of(row("id", 1, "name", "Newer", "ts", 5.0));
        List<Map<String, Object>> b = List.of(row("id", 1, "name", "Older", "ts", 1.0));
        List<Map<String, Object>> merged = MergeEngine.merge(a, b, "id", "ts", "latest");
        assertEquals("Newer", merged.get(0).get("name"));
    }

    @Test
    public void testMergePreferA() {
        List<Map<String, Object>> a = List.of(row("id", 1, "name", "A_val"));
        List<Map<String, Object>> b = List.of(row("id", 1, "name", "B_val"));
        List<Map<String, Object>> merged = MergeEngine.merge(a, b, "id", null, "a");
        assertEquals("A_val", merged.get(0).get("name"));
    }

    @Test
    public void testMergeEmptyA() {
        List<Map<String, Object>> a = Collections.emptyList();
        List<Map<String, Object>> b = List.of(row("id", 1, "name", "Bob"));
        List<Map<String, Object>> merged = CrdtMerge.merge(a, b, "id");
        assertEquals(1, merged.size());
        assertEquals("Bob", merged.get(0).get("name"));
    }

    @Test
    public void testMergeEmptyB() {
        List<Map<String, Object>> a = List.of(row("id", 1, "name", "Alice"));
        List<Map<String, Object>> b = Collections.emptyList();
        List<Map<String, Object>> merged = CrdtMerge.merge(a, b, "id");
        assertEquals(1, merged.size());
    }

    @Test
    public void testMergeBothEmpty() {
        List<Map<String, Object>> merged = CrdtMerge.merge(
                Collections.emptyList(), Collections.emptyList(), "id");
        assertTrue(merged.isEmpty());
    }

    @Test
    public void testMergeNullFillsFromOtherSide() {
        List<Map<String, Object>> a = List.of(row("id", 1, "name", "Alice"));
        List<Map<String, Object>> b = List.of(row("id", 1, "email", "alice@test.com"));
        List<Map<String, Object>> merged = CrdtMerge.merge(a, b, "id");
        assertEquals(1, merged.size());
        assertEquals("Alice", merged.get(0).get("name"));
        assertEquals("alice@test.com", merged.get(0).get("email"));
    }
}

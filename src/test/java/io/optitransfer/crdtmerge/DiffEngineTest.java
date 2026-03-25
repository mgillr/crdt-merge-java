package io.optitransfer.crdtmerge;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class DiffEngineTest {

    private Map<String, Object> row(Object... kvs) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kvs.length; i += 2) {
            m.put((String) kvs[i], kvs[i + 1]);
        }
        return m;
    }

    @Test
    public void testDiffAddedRows() {
        List<Map<String, Object>> a = new ArrayList<>();
        a.add(row("id", 1, "name", "Alice"));
        List<Map<String, Object>> b = new ArrayList<>();
        b.add(row("id", 1, "name", "Alice"));
        b.add(row("id", 2, "name", "Bob"));
        DiffEngine.DiffResult result = CrdtMerge.diff(a, b, "id");
        assertEquals(1, result.getAdded().size());
        assertEquals("Bob", result.getAdded().get(0).get("name"));
    }

    @Test
    public void testDiffRemovedRows() {
        List<Map<String, Object>> a = new ArrayList<>();
        a.add(row("id", 1, "name", "Alice"));
        a.add(row("id", 2, "name", "Bob"));
        List<Map<String, Object>> b = new ArrayList<>();
        b.add(row("id", 1, "name", "Alice"));
        DiffEngine.DiffResult result = CrdtMerge.diff(a, b, "id");
        assertEquals(1, result.getRemoved().size());
        assertEquals("Bob", result.getRemoved().get(0).get("name"));
    }

    @Test
    public void testDiffModifiedRows() {
        List<Map<String, Object>> a = new ArrayList<>();
        a.add(row("id", 1, "name", "Alice", "age", 30));
        List<Map<String, Object>> b = new ArrayList<>();
        b.add(row("id", 1, "name", "Alice", "age", 31));
        DiffEngine.DiffResult result = CrdtMerge.diff(a, b, "id");
        assertEquals(1, result.getModified().size());
        assertEquals(0, result.getUnchanged());
        DiffEngine.ModifiedRow mod = result.getModified().get(0);
        assertEquals(1, mod.getKey());
        assertEquals(1, mod.getChanges().size());
        assertEquals("age", mod.getChanges().get(0).getField());
        assertEquals(30, mod.getChanges().get(0).getOldValue());
        assertEquals(31, mod.getChanges().get(0).getNewValue());
    }

    @Test
    public void testDiffUnchangedRows() {
        List<Map<String, Object>> a = new ArrayList<>();
        a.add(row("id", 1, "name", "Alice"));
        List<Map<String, Object>> b = new ArrayList<>();
        b.add(row("id", 1, "name", "Alice"));
        DiffEngine.DiffResult result = CrdtMerge.diff(a, b, "id");
        assertEquals(0, result.getAdded().size());
        assertEquals(0, result.getRemoved().size());
        assertEquals(0, result.getModified().size());
        assertEquals(1, result.getUnchanged());
    }

    @Test
    public void testDiffBothEmpty() {
        DiffEngine.DiffResult result = CrdtMerge.diff(
                new ArrayList<>(), new ArrayList<>(), "id");
        assertEquals(0, result.getAdded().size());
        assertEquals(0, result.getRemoved().size());
        assertEquals(0, result.getModified().size());
        assertEquals(0, result.getUnchanged());
    }

    @Test
    public void testDiffSummary() {
        List<Map<String, Object>> a = new ArrayList<>();
        a.add(row("id", 1, "name", "Alice"));
        a.add(row("id", 2, "name", "Bob"));
        List<Map<String, Object>> b = new ArrayList<>();
        b.add(row("id", 1, "name", "Alice Updated"));
        b.add(row("id", 3, "name", "Charlie"));
        DiffEngine.DiffResult result = CrdtMerge.diff(a, b, "id");
        String summary = result.getSummary();
        assertTrue(summary.contains("+1 added"));
        assertTrue(summary.contains("-1 removed"));
        assertTrue(summary.contains("~1 modified"));
    }

    @Test
    public void testDiffNewColumnInB() {
        List<Map<String, Object>> a = new ArrayList<>();
        a.add(row("id", 1, "name", "Alice"));
        List<Map<String, Object>> b = new ArrayList<>();
        b.add(row("id", 1, "name", "Alice", "email", "a@b.com"));
        DiffEngine.DiffResult result = CrdtMerge.diff(a, b, "id");
        assertEquals(1, result.getModified().size());
        DiffEngine.FieldChange change = result.getModified().get(0).getChanges().get(0);
        assertEquals("email", change.getField());
        assertNull(change.getOldValue());
        assertEquals("a@b.com", change.getNewValue());
    }
}

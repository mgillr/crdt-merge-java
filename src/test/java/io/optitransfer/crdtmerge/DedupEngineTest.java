/*
 * Copyright 2026 Ryan Gillespie
 * SPDX-License-Identifier: Apache-2.0
 *
 * Commercial licensing: data@optitransfer.ch, rgillespie83@icloud.com
 */

package io.optitransfer.crdtmerge;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class DedupEngineTest {

    private Map<String, Object> row(Object... kvs) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kvs.length; i += 2) {
            m.put((String) kvs[i], kvs[i + 1]);
        }
        return m;
    }

    @Test
    public void testExactDedupRemovesDuplicates() {
        List<Map<String, Object>> items = new ArrayList<>();
        items.add(row("id", 1, "name", "Alice"));
        items.add(row("id", 1, "name", "Alice"));
        items.add(row("id", 2, "name", "Bob"));
        DedupEngine.DedupResult result = CrdtMerge.dedup(items, "id", 1.0);
        assertEquals(2, result.getUnique().size());
        assertEquals(1, result.getDuplicatesRemoved());
    }

    @Test
    public void testExactDedupNoDuplicates() {
        List<Map<String, Object>> items = new ArrayList<>();
        items.add(row("id", 1, "name", "Alice"));
        items.add(row("id", 2, "name", "Bob"));
        DedupEngine.DedupResult result = CrdtMerge.dedup(items, "id", 1.0);
        assertEquals(2, result.getUnique().size());
        assertEquals(0, result.getDuplicatesRemoved());
    }

    @Test
    public void testFuzzyDedupSimilarStrings() {
        List<Map<String, Object>> items = new ArrayList<>();
        items.add(row("id", 1, "name", "John Smith"));
        items.add(row("id", 2, "name", "Jon Smith"));
        items.add(row("id", 3, "name", "Jane Doe"));
        DedupEngine.DedupResult result = CrdtMerge.dedup(items, "id", 0.5);
        assertEquals(2, result.getUnique().size());
    }

    @Test
    public void testFuzzyDedupHighThreshold() {
        List<Map<String, Object>> items = new ArrayList<>();
        items.add(row("id", 1, "name", "John Smith"));
        items.add(row("id", 2, "name", "Jon Smith"));
        DedupEngine.DedupResult result = CrdtMerge.dedup(items, "id", 0.99);
        assertEquals(2, result.getUnique().size());
    }

    @Test
    public void testFuzzyDedupEmptyInput() {
        DedupEngine.DedupResult result = CrdtMerge.dedup(new ArrayList<>(), "id", 0.85);
        assertTrue(result.getUnique().isEmpty());
        assertEquals(0, result.getDuplicatesRemoved());
    }

    @Test
    public void testJaccardSimilarityIdentical() {
        double sim = DedupEngine.jaccardSimilarity("hello world", "hello world");
        assertEquals(1.0, sim, 0.001);
    }

    @Test
    public void testJaccardSimilarityCompletelyDifferent() {
        double sim = DedupEngine.jaccardSimilarity("abc", "xyz");
        assertEquals(0.0, sim, 0.001);
    }

    @Test
    public void testJaccardSimilarityPartial() {
        double sim = DedupEngine.jaccardSimilarity("hello", "hallo");
        assertTrue(sim > 0.0);
        assertTrue(sim < 1.0);
    }

    @Test
    public void testBigramsGeneration() {
        Set<String> bg = DedupEngine.bigrams("abc");
        assertTrue(bg.contains("ab"));
        assertTrue(bg.contains("bc"));
        assertEquals(2, bg.size());
    }

    @Test
    public void testBigramsSingleChar() {
        Set<String> bg = DedupEngine.bigrams("a");
        assertEquals(1, bg.size());
        assertTrue(bg.contains("a"));
    }

    @Test
    public void testDedupResultToString() {
        DedupEngine.DedupResult result = new DedupEngine.DedupResult(
                List.of(row("id", 1)), 2, Collections.emptyList());
        assertTrue(result.toString().contains("unique=1"));
        assertTrue(result.toString().contains("removed=2"));
    }
}

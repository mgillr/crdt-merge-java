/*
 * Copyright 2026 Ryan Gillespie
 * SPDX-License-Identifier: Apache-2.0
 *
 * Commercial licensing: data@optitransfer.ch, rgillespie83@icloud.com
 */

package io.optitransfer.crdtmerge;

import io.optitransfer.crdtmerge.core.ORSet;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Set;

public class ORSetTest {

    @Test
    public void testEmptySet() {
        ORSet<String> s = new ORSet<>();
        assertTrue(s.elements().isEmpty());
        assertEquals(0, s.size());
    }

    @Test
    public void testAddAndContains() {
        ORSet<String> s = new ORSet<>();
        s.add("hello");
        assertTrue(s.contains("hello"));
        assertFalse(s.contains("world"));
    }

    @Test
    public void testRemove() {
        ORSet<String> s = new ORSet<>();
        s.add("hello");
        s.remove("hello");
        assertFalse(s.contains("hello"));
        assertEquals(0, s.size());
    }

    @Test
    public void testElements() {
        ORSet<String> s = new ORSet<>();
        s.add("a");
        s.add("b");
        s.add("c");
        Set<String> elems = s.elements();
        assertEquals(3, elems.size());
        assertTrue(elems.contains("a"));
        assertTrue(elems.contains("b"));
        assertTrue(elems.contains("c"));
    }

    @Test
    public void testMergeUnion() {
        ORSet<String> a = new ORSet<>();
        a.add("x");
        ORSet<String> b = new ORSet<>();
        b.add("y");
        ORSet<String> merged = a.merge(b);
        assertTrue(merged.contains("x"));
        assertTrue(merged.contains("y"));
        assertEquals(2, merged.size());
    }

    @Test
    public void testMergeAddWinsOverConcurrentRemove() {
        ORSet<String> base = new ORSet<>();
        base.add("item");

        ORSet<String> a = base.merge(new ORSet<>());
        a.add("item");

        ORSet<String> b = base.merge(new ORSet<>());
        b.remove("item");

        ORSet<String> merged = a.merge(b);
        assertTrue(merged.contains("item"));
    }

    @Test
    public void testMergeCommutativity() {
        ORSet<String> a = new ORSet<>();
        a.add("x");
        a.add("y");
        ORSet<String> b = new ORSet<>();
        b.add("y");
        b.add("z");
        ORSet<String> ab = a.merge(b);
        ORSet<String> ba = b.merge(a);
        assertEquals(ab.elements(), ba.elements());
    }

    @Test
    public void testMergeIdempotency() {
        ORSet<String> a = new ORSet<>();
        a.add("x");
        a.add("y");
        ORSet<String> merged = a.merge(a);
        assertEquals(a.elements(), merged.elements());
        assertEquals(a, merged);
    }

    @Test
    public void testRemoveNonExistent() {
        ORSet<String> s = new ORSet<>();
        s.remove("nonexistent");
        assertEquals(0, s.size());
    }

    @Test
    public void testAddReturnTag() {
        ORSet<String> s = new ORSet<>();
        String tag = s.add("hello");
        assertNotNull(tag);
        assertEquals(12, tag.length());
    }

    @Test
    public void testToString() {
        ORSet<String> s = new ORSet<>();
        s.add("a");
        assertTrue(s.toString().contains("size=1"));
    }
}

/*
 * Copyright 2026 Ryan Gillespie
 * SPDX-License-Identifier: Apache-2.0
 *
 * Commercial licensing: data@optitransfer.ch, rgillespie83@icloud.com
 */

package io.optitransfer.crdtmerge;

import io.optitransfer.crdtmerge.core.GCounter;
import org.junit.Test;
import static org.junit.Assert.*;

public class GCounterTest {

    @Test
    public void testInitialValueIsZero() {
        GCounter c = new GCounter();
        assertEquals(0, c.value());
    }

    @Test
    public void testIncrementSingleNode() {
        GCounter c = new GCounter();
        c.increment("A");
        assertEquals(1, c.value());
        c.increment("A");
        assertEquals(2, c.value());
    }

    @Test
    public void testIncrementMultipleNodes() {
        GCounter c = new GCounter();
        c.increment("A", 5);
        c.increment("B", 3);
        assertEquals(8, c.value());
    }

    @Test
    public void testConstructorWithInitial() {
        GCounter c = new GCounter("A", 10);
        assertEquals(10, c.value());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeIncrementThrows() {
        GCounter c = new GCounter();
        c.increment("A", -1);
    }

    @Test
    public void testMergeBasic() {
        GCounter a = new GCounter();
        a.increment("A", 5);
        GCounter b = new GCounter();
        b.increment("B", 3);
        GCounter merged = a.merge(b);
        assertEquals(8, merged.value());
    }

    @Test
    public void testMergeCommutativity() {
        GCounter a = new GCounter();
        a.increment("A", 5);
        a.increment("B", 2);
        GCounter b = new GCounter();
        b.increment("A", 3);
        b.increment("B", 7);
        GCounter ab = a.merge(b);
        GCounter ba = b.merge(a);
        assertEquals(ab.value(), ba.value());
        assertEquals(ab, ba);
    }

    @Test
    public void testMergeAssociativity() {
        GCounter a = new GCounter();
        a.increment("X", 1);
        GCounter b = new GCounter();
        b.increment("Y", 2);
        GCounter c = new GCounter();
        c.increment("Z", 3);
        GCounter ab_c = a.merge(b).merge(c);
        GCounter a_bc = a.merge(b.merge(c));
        assertEquals(ab_c, a_bc);
    }

    @Test
    public void testMergeIdempotency() {
        GCounter a = new GCounter();
        a.increment("A", 5);
        a.increment("B", 3);
        GCounter merged = a.merge(a);
        assertEquals(a, merged);
    }

    @Test
    public void testMergeTakesMax() {
        GCounter a = new GCounter();
        a.increment("A", 5);
        GCounter b = new GCounter();
        b.increment("A", 3);
        GCounter merged = a.merge(b);
        assertEquals(5, merged.value());
    }

    @Test
    public void testToString() {
        GCounter c = new GCounter();
        c.increment("A", 5);
        assertTrue(c.toString().contains("value=5"));
    }

    @Test
    public void testZeroIncrement() {
        GCounter c = new GCounter();
        c.increment("A", 0);
        assertEquals(0, c.value());
    }
}

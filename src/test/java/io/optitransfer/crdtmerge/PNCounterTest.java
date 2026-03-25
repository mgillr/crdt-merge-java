package io.optitransfer.crdtmerge;

import io.optitransfer.crdtmerge.core.PNCounter;
import org.junit.Test;
import static org.junit.Assert.*;

public class PNCounterTest {

    @Test
    public void testInitialValueIsZero() {
        PNCounter c = new PNCounter();
        assertEquals(0, c.value());
    }

    @Test
    public void testIncrement() {
        PNCounter c = new PNCounter();
        c.increment("A");
        assertEquals(1, c.value());
    }

    @Test
    public void testDecrement() {
        PNCounter c = new PNCounter();
        c.increment("A", 5);
        c.decrement("A", 2);
        assertEquals(3, c.value());
    }

    @Test
    public void testNegativeValue() {
        PNCounter c = new PNCounter();
        c.decrement("A", 5);
        assertEquals(-5, c.value());
    }

    @Test
    public void testMergeCommutativity() {
        PNCounter a = new PNCounter();
        a.increment("A", 5);
        a.decrement("A", 1);
        PNCounter b = new PNCounter();
        b.increment("B", 3);
        b.decrement("B", 2);
        PNCounter ab = a.merge(b);
        PNCounter ba = b.merge(a);
        assertEquals(ab.value(), ba.value());
        assertEquals(ab, ba);
    }

    @Test
    public void testMergeAssociativity() {
        PNCounter a = new PNCounter();
        a.increment("A", 3);
        PNCounter b = new PNCounter();
        b.decrement("B", 1);
        PNCounter c = new PNCounter();
        c.increment("C", 2);
        assertEquals(a.merge(b).merge(c), a.merge(b.merge(c)));
    }

    @Test
    public void testMergeIdempotency() {
        PNCounter a = new PNCounter();
        a.increment("A", 5);
        a.decrement("A", 2);
        assertEquals(a, a.merge(a));
    }

    @Test
    public void testToString() {
        PNCounter c = new PNCounter();
        c.increment("A", 3);
        assertTrue(c.toString().contains("value=3"));
    }
}

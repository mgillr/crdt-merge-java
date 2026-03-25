package io.optitransfer.crdtmerge;

import io.optitransfer.crdtmerge.core.LWWRegister;
import org.junit.Test;
import static org.junit.Assert.*;

public class LWWRegisterTest {

    @Test
    public void testDefaultValue() {
        LWWRegister<String> reg = new LWWRegister<>();
        assertNull(reg.get());
        assertEquals(0.0, reg.getTimestamp(), 0.001);
    }

    @Test
    public void testSetAndGet() {
        LWWRegister<String> reg = new LWWRegister<>();
        reg.set("hello", 1.0);
        assertEquals("hello", reg.get());
        assertEquals(1.0, reg.getTimestamp(), 0.001);
    }

    @Test
    public void testMergeLatestWins() {
        LWWRegister<String> a = new LWWRegister<>("old", 1.0, "A");
        LWWRegister<String> b = new LWWRegister<>("new", 2.0, "B");
        LWWRegister<String> merged = a.merge(b);
        assertEquals("new", merged.get());
        assertEquals(2.0, merged.getTimestamp(), 0.001);
    }

    @Test
    public void testMergeOlderLoses() {
        LWWRegister<String> a = new LWWRegister<>("newer", 5.0, "A");
        LWWRegister<String> b = new LWWRegister<>("older", 1.0, "B");
        LWWRegister<String> merged = a.merge(b);
        assertEquals("newer", merged.get());
    }

    @Test
    public void testMergeTieBreaksOnNodeId() {
        LWWRegister<String> a = new LWWRegister<>("a_val", 1.0, "A");
        LWWRegister<String> b = new LWWRegister<>("b_val", 1.0, "B");
        LWWRegister<String> merged = a.merge(b);
        assertEquals("b_val", merged.get());
    }

    @Test
    public void testMergeCommutativity() {
        LWWRegister<String> a = new LWWRegister<>("a_val", 1.0, "A");
        LWWRegister<String> b = new LWWRegister<>("b_val", 2.0, "B");
        assertEquals(a.merge(b), b.merge(a));
    }

    @Test
    public void testMergeIdempotency() {
        LWWRegister<String> a = new LWWRegister<>("val", 1.0, "A");
        assertEquals(a, a.merge(a));
    }

    @Test
    public void testConstructorWithTimestamp() {
        LWWRegister<Integer> reg = new LWWRegister<>(42, 3.14);
        assertEquals(Integer.valueOf(42), reg.get());
        assertEquals(3.14, reg.getTimestamp(), 0.001);
    }

    @Test
    public void testToString() {
        LWWRegister<String> reg = new LWWRegister<>("test", 1.0);
        assertTrue(reg.toString().contains("test"));
        assertTrue(reg.toString().contains("1.0"));
    }
}

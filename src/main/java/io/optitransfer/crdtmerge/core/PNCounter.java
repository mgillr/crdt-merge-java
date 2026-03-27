/*
 * Copyright 2026 Ryan Gillespie
 * SPDX-License-Identifier: Apache-2.0
 *
 * Commercial licensing: data@optitransfer.ch, rgillespie83@icloud.com
 */

package io.optitransfer.crdtmerge.core;

import java.io.Serializable;
import java.util.Objects;

/**
 * Positive-Negative counter — supports both increment and decrement.
 * Internally two G-Counters: one for increments, one for decrements.
 */
public class PNCounter implements Serializable {
    private static final long serialVersionUID = 1L;

    private GCounter pos;
    private GCounter neg;

    public PNCounter() {
        this.pos = new GCounter();
        this.neg = new GCounter();
    }

    public long value() {
        return pos.value() - neg.value();
    }

    public void increment(String nodeId) {
        pos.increment(nodeId, 1);
    }

    public void increment(String nodeId, long amount) {
        pos.increment(nodeId, amount);
    }

    public void decrement(String nodeId) {
        neg.increment(nodeId, 1);
    }

    public void decrement(String nodeId, long amount) {
        neg.increment(nodeId, amount);
    }

    public PNCounter merge(PNCounter other) {
        PNCounter result = new PNCounter();
        result.pos = this.pos.merge(other.pos);
        result.neg = this.neg.merge(other.neg);
        return result;
    }

    public GCounter getPositive() { return pos; }
    public GCounter getNegative() { return neg; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PNCounter pn)) return false;
        return Objects.equals(pos, pn.pos) && Objects.equals(neg, pn.neg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, neg);
    }

    @Override
    public String toString() {
        return "PNCounter(value=" + value() + ")";
    }
}

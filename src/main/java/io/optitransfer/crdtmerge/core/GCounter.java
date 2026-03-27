/*
 * Copyright 2026 Ryan Gillespie
 * SPDX-License-Identifier: Apache-2.0
 *
 * Commercial licensing: data@optitransfer.ch, rgillespie83@icloud.com
 */

package io.optitransfer.crdtmerge.core;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Grow-only counter. Each node has its own slot; value = sum of all slots.
 * <p>
 * Satisfies CRDT convergence: commutative, associative, idempotent merge.
 * Perfect for: page views, download counts, event counters — anything that only goes up.
 */
public class GCounter implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<String, Long> counts;

    public GCounter() {
        this.counts = new HashMap<>();
    }

    public GCounter(String nodeId, long initial) {
        this.counts = new HashMap<>();
        if (nodeId != null && initial > 0) {
            this.counts.put(nodeId, initial);
        }
    }

    /** Current value: sum of all node slots. */
    public long value() {
        long sum = 0;
        for (long v : counts.values()) {
            sum += v;
        }
        return sum;
    }

    /** Increment the counter for the given node by 1. */
    public void increment(String nodeId) {
        increment(nodeId, 1);
    }

    /** Increment the counter for the given node by the specified amount. */
    public void increment(String nodeId, long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("GCounter only supports non-negative increments");
        }
        counts.merge(nodeId, amount, Long::sum);
    }

    /** Merge with another GCounter, taking the max per node. Returns a new GCounter. */
    public GCounter merge(GCounter other) {
        GCounter result = new GCounter();
        Set<String> allKeys = new HashSet<>(this.counts.keySet());
        allKeys.addAll(other.counts.keySet());
        for (String k : allKeys) {
            long a = this.counts.getOrDefault(k, 0L);
            long b = other.counts.getOrDefault(k, 0L);
            result.counts.put(k, Math.max(a, b));
        }
        return result;
    }

    /** Returns an unmodifiable view of the internal counts. */
    public Map<String, Long> getCounts() {
        return Collections.unmodifiableMap(counts);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GCounter gc)) return false;
        return Objects.equals(counts, gc.counts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(counts);
    }

    @Override
    public String toString() {
        return "GCounter(value=" + value() + ", nodes=" + counts.size() + ")";
    }
}

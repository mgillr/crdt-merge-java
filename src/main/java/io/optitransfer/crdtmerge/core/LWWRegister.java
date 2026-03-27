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
 * Last-Writer-Wins Register — stores a single value, latest timestamp wins.
 * On timestamp tie, the higher nodeId wins for determinism.
 *
 * @param <T> the type of value stored
 */
public class LWWRegister<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private T value;
    private double timestamp;
    private String nodeId;

    public LWWRegister() {
        this(null, 0.0, "");
    }

    public LWWRegister(T value, double timestamp) {
        this(value, timestamp, "");
    }

    public LWWRegister(T value, double timestamp, String nodeId) {
        this.value = value;
        this.timestamp = timestamp;
        this.nodeId = nodeId != null ? nodeId : "";
    }

    public T get() { return value; }
    public double getTimestamp() { return timestamp; }
    public String getNodeId() { return nodeId; }

    public void set(T value, double timestamp) {
        set(value, timestamp, "");
    }

    public void set(T value, double timestamp, String nodeId) {
        this.value = value;
        this.timestamp = timestamp;
        this.nodeId = nodeId != null ? nodeId : "";
    }

    public LWWRegister<T> merge(LWWRegister<T> other) {
        if (other.timestamp > this.timestamp) {
            return new LWWRegister<>(other.value, other.timestamp, other.nodeId);
        } else if (other.timestamp == this.timestamp) {
            if (other.nodeId.compareTo(this.nodeId) > 0) {
                return new LWWRegister<>(other.value, other.timestamp, other.nodeId);
            }
        }
        return new LWWRegister<>(this.value, this.timestamp, this.nodeId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LWWRegister<?> that)) return false;
        return Double.compare(that.timestamp, timestamp) == 0
                && Objects.equals(value, that.value)
                && Objects.equals(nodeId, that.nodeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, timestamp, nodeId);
    }

    @Override
    public String toString() {
        return "LWWRegister(value=" + value + ", ts=" + timestamp + ")";
    }
}

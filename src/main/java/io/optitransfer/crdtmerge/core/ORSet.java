package io.optitransfer.crdtmerge.core;

import java.io.Serializable;
import java.util.*;

/**
 * Observed-Remove Set — add and remove elements without conflicts.
 * Each element is tagged with a unique ID on add. Remove kills specific tags,
 * so concurrent add+remove resolves correctly (add-wins semantics).
 *
 * @param <T> the type of elements in the set
 */
public class ORSet<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<T, Set<String>> elements;

    public ORSet() {
        this.elements = new HashMap<>();
    }

    public String add(T element) {
        String tag = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        elements.computeIfAbsent(element, k -> new HashSet<>()).add(tag);
        return tag;
    }

    public void remove(T element) {
        if (elements.containsKey(element)) {
            elements.put(element, new HashSet<>());
        }
    }

    public boolean contains(T element) {
        Set<String> tags = elements.get(element);
        return tags != null && !tags.isEmpty();
    }

    public Set<T> elements() {
        Set<T> result = new HashSet<>();
        for (Map.Entry<T, Set<String>> entry : elements.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public ORSet<T> merge(ORSet<T> other) {
        ORSet<T> result = new ORSet<>();
        Set<T> allElements = new HashSet<>(this.elements.keySet());
        allElements.addAll(other.elements.keySet());
        for (T e : allElements) {
            Set<String> tagsA = this.elements.getOrDefault(e, Collections.emptySet());
            Set<String> tagsB = other.elements.getOrDefault(e, Collections.emptySet());
            Set<String> merged = new HashSet<>(tagsA);
            merged.addAll(tagsB);
            result.elements.put(e, merged);
        }
        return result;
    }

    public int size() { return elements().size(); }

    public Map<T, Set<String>> getElementsMap() {
        return Collections.unmodifiableMap(elements);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ORSet<?> orSet)) return false;
        return Objects.equals(elements, orSet.elements);
    }

    @Override
    public int hashCode() { return Objects.hash(elements); }

    @Override
    public String toString() { return "ORSet(size=" + size() + ")"; }
}

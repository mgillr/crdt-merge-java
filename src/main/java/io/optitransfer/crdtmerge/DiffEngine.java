package io.optitransfer.crdtmerge;

import java.util.*;

/**
 * Structural diff engine — shows what changed between two datasets.
 */
public class DiffEngine {

    public static class FieldChange {
        private final String field;
        private final Object oldValue;
        private final Object newValue;

        public FieldChange(String field, Object oldValue, Object newValue) {
            this.field = field;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        public String getField() { return field; }
        public Object getOldValue() { return oldValue; }
        public Object getNewValue() { return newValue; }

        @Override
        public String toString() { return field + ": " + oldValue + " -> " + newValue; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FieldChange fc)) return false;
            return Objects.equals(field, fc.field)
                    && Objects.equals(oldValue, fc.oldValue)
                    && Objects.equals(newValue, fc.newValue);
        }

        @Override
        public int hashCode() { return Objects.hash(field, oldValue, newValue); }
    }

    public static class ModifiedRow {
        private final Object key;
        private final List<FieldChange> changes;

        public ModifiedRow(Object key, List<FieldChange> changes) {
            this.key = key;
            this.changes = changes;
        }

        public Object getKey() { return key; }
        public List<FieldChange> getChanges() { return changes; }

        @Override
        public String toString() { return "ModifiedRow(key=" + key + ", changes=" + changes + ")"; }
    }

    public static class DiffResult {
        private final List<Map<String, Object>> added;
        private final List<Map<String, Object>> removed;
        private final List<ModifiedRow> modified;
        private final int unchanged;

        public DiffResult(List<Map<String, Object>> added, List<Map<String, Object>> removed,
                          List<ModifiedRow> modified, int unchanged) {
            this.added = added;
            this.removed = removed;
            this.modified = modified;
            this.unchanged = unchanged;
        }

        public List<Map<String, Object>> getAdded() { return added; }
        public List<Map<String, Object>> getRemoved() { return removed; }
        public List<ModifiedRow> getModified() { return modified; }
        public int getUnchanged() { return unchanged; }

        public String getSummary() {
            return "+" + added.size() + " added, -" + removed.size()
                    + " removed, ~" + modified.size() + " modified, ="
                    + unchanged + " unchanged";
        }

        @Override
        public String toString() { return "DiffResult(" + getSummary() + ")"; }
    }

    public static DiffResult diff(List<Map<String, Object>> a, List<Map<String, Object>> b, String key) {
        Map<Object, Map<String, Object>> indexA = buildIndex(a, key);
        Map<Object, Map<String, Object>> indexB = buildIndex(b, key);

        List<Map<String, Object>> added = new ArrayList<>();
        for (Map.Entry<Object, Map<String, Object>> entry : indexB.entrySet()) {
            if (!indexA.containsKey(entry.getKey())) {
                added.add(entry.getValue());
            }
        }

        List<Map<String, Object>> removed = new ArrayList<>();
        for (Map.Entry<Object, Map<String, Object>> entry : indexA.entrySet()) {
            if (!indexB.containsKey(entry.getKey())) {
                removed.add(entry.getValue());
            }
        }

        List<ModifiedRow> modified = new ArrayList<>();
        int unchanged = 0;

        for (Object k : indexA.keySet()) {
            if (!indexB.containsKey(k)) continue;
            Map<String, Object> rowA = indexA.get(k);
            Map<String, Object> rowB = indexB.get(k);

            if (rowA.equals(rowB)) {
                unchanged++;
            } else {
                List<FieldChange> changes = new ArrayList<>();
                Set<String> allCols = new HashSet<>(rowA.keySet());
                allCols.addAll(rowB.keySet());
                for (String col : allCols) {
                    Object va = rowA.get(col);
                    Object vb = rowB.get(col);
                    if (!Objects.equals(va, vb)) {
                        changes.add(new FieldChange(col, va, vb));
                    }
                }
                modified.add(new ModifiedRow(k, changes));
            }
        }

        return new DiffResult(added, removed, modified, unchanged);
    }

    private static Map<Object, Map<String, Object>> buildIndex(List<Map<String, Object>> rows, String key) {
        Map<Object, Map<String, Object>> index = new LinkedHashMap<>();
        for (Map<String, Object> r : rows) {
            Object k = r.get(key);
            if (k != null) index.put(k, r);
        }
        return index;
    }
}

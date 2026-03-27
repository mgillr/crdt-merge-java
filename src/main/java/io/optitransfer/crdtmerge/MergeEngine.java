/*
 * Copyright 2026 Ryan Gillespie
 * SPDX-License-Identifier: Apache-2.0
 *
 * Commercial licensing: data@optitransfer.ch, rgillespie83@icloud.com
 */

package io.optitransfer.crdtmerge;

import io.optitransfer.crdtmerge.core.LWWRegister;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * CRDT-powered tabular merge — conflict-free merge of two lists of maps (table rows).
 */
public class MergeEngine {

    public static List<Map<String, Object>> merge(
            List<Map<String, Object>> a,
            List<Map<String, Object>> b,
            String key) {
        return merge(a, b, key, null, "latest");
    }

    public static List<Map<String, Object>> merge(
            List<Map<String, Object>> a,
            List<Map<String, Object>> b,
            String key,
            String timestampCol,
            String prefer) {

        List<String> allColumns = collectColumns(a, b);

        if (key == null) {
            List<Map<String, Object>> combined = new ArrayList<>(a);
            combined.addAll(b);
            return dedupRecords(combined, null);
        }

        Map<Object, Map<String, Object>> indexA = buildIndex(a, key);
        Map<Object, Map<String, Object>> indexB = buildIndex(b, key);

        List<Object> allKeys = new ArrayList<>();
        Set<Object> seen = new LinkedHashSet<>();
        for (Map<String, Object> row : a) {
            Object k = row.get(key);
            if (k != null && seen.add(k)) allKeys.add(k);
        }
        for (Map<String, Object> row : b) {
            Object k = row.get(key);
            if (k != null && seen.add(k)) allKeys.add(k);
        }

        List<Map<String, Object>> merged = new ArrayList<>();
        for (Object k : allKeys) {
            Map<String, Object> rowA = indexA.get(k);
            Map<String, Object> rowB = indexB.get(k);

            if (rowA != null && rowB == null) {
                merged.add(rowA);
            } else if (rowB != null && rowA == null) {
                merged.add(rowB);
            } else if (rowA != null && rowB != null) {
                merged.add(mergeRows(rowA, rowB, allColumns, timestampCol, prefer));
            }
        }

        return dedupRecords(merged, key);
    }

    static Map<String, Object> mergeRows(
            Map<String, Object> rowA,
            Map<String, Object> rowB,
            List<String> columns,
            String timestampCol,
            String prefer) {

        double tsA = 0.0, tsB = 0.0;
        if (timestampCol != null) {
            tsA = toDouble(rowA.get(timestampCol));
            tsB = toDouble(rowB.get(timestampCol));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        for (String col : columns) {
            Object valA = rowA.get(col);
            Object valB = rowB.get(col);

            if (valA == null && valB != null) {
                result.put(col, valB);
            } else if (valB == null && valA != null) {
                result.put(col, valA);
            } else if (Objects.equals(valA, valB)) {
                result.put(col, valA);
            } else {
                if (timestampCol != null) {
                    LWWRegister<Object> regA = new LWWRegister<>(valA, tsA, "a");
                    LWWRegister<Object> regB = new LWWRegister<>(valB, tsB, "b");
                    result.put(col, regA.merge(regB).get());
                } else if ("a".equals(prefer)) {
                    result.put(col, valA);
                } else {
                    result.put(col, valB);
                }
            }
        }
        return result;
    }

    static List<Map<String, Object>> dedupRecords(List<Map<String, Object>> records, String excludeKey) {
        Set<String> seen = new LinkedHashSet<>();
        List<Map<String, Object>> unique = new ArrayList<>();
        for (Map<String, Object> r : records) {
            String h = rowHash(r, excludeKey);
            if (seen.add(h)) {
                unique.add(r);
            }
        }
        return unique;
    }

    static String rowHash(Map<String, Object> row, String excludeKey) {
        List<String> parts = new ArrayList<>();
        TreeMap<String, Object> sorted = new TreeMap<>(row);
        for (Map.Entry<String, Object> e : sorted.entrySet()) {
            if (excludeKey != null && excludeKey.equals(e.getKey())) continue;
            parts.add(e.getKey() + "=" + e.getValue());
        }
        String joined = String.join("|", parts);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(joined.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                sb.append(String.format("%02x", digest[i]));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(joined.hashCode());
        }
    }

    static List<String> collectColumns(List<Map<String, Object>> a, List<Map<String, Object>> b) {
        LinkedHashSet<String> cols = new LinkedHashSet<>();
        for (Map<String, Object> row : a) cols.addAll(row.keySet());
        for (Map<String, Object> row : b) cols.addAll(row.keySet());
        return new ArrayList<>(cols);
    }

    private static Map<Object, Map<String, Object>> buildIndex(List<Map<String, Object>> rows, String key) {
        Map<Object, Map<String, Object>> index = new LinkedHashMap<>();
        for (Map<String, Object> r : rows) {
            Object k = r.get(key);
            if (k != null) {
                index.put(k, r);
            }
        }
        return index;
    }

    private static double toDouble(Object val) {
        if (val == null) return 0.0;
        if (val instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(val.toString()); }
        catch (NumberFormatException e) { return 0.0; }
    }
}

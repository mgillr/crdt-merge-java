package io.optitransfer.crdtmerge;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * High-performance deduplication with exact and fuzzy matching.
 * Fuzzy matching uses Jaccard similarity on character bigrams.
 */
public class DedupEngine {

    /** Result of a dedup operation. */
    public static class DedupResult {
        private final List<Map<String, Object>> unique;
        private final int duplicatesRemoved;
        private final List<int[]> duplicateGroups;

        public DedupResult(List<Map<String, Object>> unique, int duplicatesRemoved, List<int[]> duplicateGroups) {
            this.unique = unique;
            this.duplicatesRemoved = duplicatesRemoved;
            this.duplicateGroups = duplicateGroups;
        }

        public List<Map<String, Object>> getUnique() { return unique; }
        public int getDuplicatesRemoved() { return duplicatesRemoved; }
        public List<int[]> getDuplicateGroups() { return duplicateGroups; }

        @Override
        public String toString() {
            return "DedupResult(unique=" + unique.size() + ", removed=" + duplicatesRemoved + ")";
        }
    }

    public static DedupResult dedup(List<Map<String, Object>> items, String key, double threshold) {
        if (threshold >= 1.0) {
            return exactDedup(items, key);
        } else {
            return fuzzyDedup(items, key, threshold);
        }
    }

    public static DedupResult exactDedup(List<Map<String, Object>> items, String key) {
        Set<Object> seenKeys = new LinkedHashSet<>();
        List<Map<String, Object>> unique = new ArrayList<>();
        int removed = 0;
        List<int[]> groups = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            Map<String, Object> item = items.get(i);
            Object keyVal = item.get(key);
            if (keyVal != null && !seenKeys.add(keyVal)) {
                removed++;
            } else {
                unique.add(item);
            }
        }
        return new DedupResult(unique, removed, groups);
    }

    public static DedupResult fuzzyDedup(List<Map<String, Object>> items, String key, double threshold) {
        if (items.isEmpty()) {
            return new DedupResult(Collections.emptyList(), 0, Collections.emptyList());
        }

        List<Map<String, Object>> unique = new ArrayList<>();
        List<String> uniqueTexts = new ArrayList<>();
        int removed = 0;
        List<int[]> groups = new ArrayList<>();

        unique.add(items.get(0));
        uniqueTexts.add(textOfRecord(items.get(0), key));

        for (int i = 1; i < items.size(); i++) {
            String text = textOfRecord(items.get(i), key);
            boolean isDup = false;
            for (String existingText : uniqueTexts) {
                if (jaccardSimilarity(text, existingText) >= threshold) {
                    isDup = true;
                    break;
                }
            }
            if (isDup) {
                removed++;
            } else {
                unique.add(items.get(i));
                uniqueTexts.add(text);
            }
        }
        return new DedupResult(unique, removed, groups);
    }

    /** Jaccard similarity = |intersection| / |union| of bigram sets. */
    public static double jaccardSimilarity(String a, String b) {
        Set<String> bigramsA = bigrams(a);
        Set<String> bigramsB = bigrams(b);
        if (bigramsA.isEmpty() && bigramsB.isEmpty()) return 1.0;
        if (bigramsA.isEmpty() || bigramsB.isEmpty()) return 0.0;

        Set<String> intersection = new HashSet<>(bigramsA);
        intersection.retainAll(bigramsB);

        Set<String> union = new HashSet<>(bigramsA);
        union.addAll(bigramsB);

        return (double) intersection.size() / union.size();
    }

    /** Generate character bigrams from a string. */
    public static Set<String> bigrams(String text) {
        String normalized = normalize(text);
        Set<String> result = new HashSet<>();
        if (normalized.length() < 2) {
            if (!normalized.isEmpty()) result.add(normalized);
            return result;
        }
        for (int i = 0; i < normalized.length() - 1; i++) {
            result.add(normalized.substring(i, i + 2));
        }
        return result;
    }

    static String normalize(String text) {
        return text.toLowerCase().trim().replaceAll("\\s+", " ");
    }

    static String textOfRecord(Map<String, Object> record, String excludeKey) {
        TreeMap<String, Object> sorted = new TreeMap<>(record);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> e : sorted.entrySet()) {
            if (excludeKey != null && excludeKey.equals(e.getKey())) continue;
            if (e.getValue() != null) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(e.getValue());
            }
        }
        return sb.toString();
    }

    static String hashText(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(normalize(text).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}

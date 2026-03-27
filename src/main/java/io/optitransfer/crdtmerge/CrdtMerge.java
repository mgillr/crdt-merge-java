/*
 * Copyright 2026 Ryan Gillespie
 * SPDX-License-Identifier: Apache-2.0
 *
 * Commercial licensing: data@optitransfer.ch, rgillespie83@icloud.com
 */

package io.optitransfer.crdtmerge;

import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;

/**
 * Public facade for crdt-merge — conflict-free merge, dedup &amp; diff for any dataset.
 * All methods are static for easy use.
 */
public class CrdtMerge {

    public static final String VERSION = "0.1.0";

    /** Merge two lists of maps (tabular data) by primary key. */
    public static List<Map<String, Object>> merge(
            List<Map<String, Object>> a,
            List<Map<String, Object>> b,
            String key) {
        return MergeEngine.merge(a, b, key);
    }

    /** Deduplicate a list of records. */
    public static DedupEngine.DedupResult dedup(
            List<Map<String, Object>> items,
            String key,
            double threshold) {
        return DedupEngine.dedup(items, key, threshold);
    }

    /** Compute structural diff between two datasets. */
    public static DiffEngine.DiffResult diff(
            List<Map<String, Object>> a,
            List<Map<String, Object>> b,
            String key) {
        return DiffEngine.diff(a, b, key);
    }

    /** Deep merge two JSON objects. */
    public static JsonObject mergeJson(JsonObject a, JsonObject b) {
        return JsonMerge.mergeJson(a, b);
    }
}

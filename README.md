<div align="center">

# 🔀 crdt-merge

**Conflict-free merge, dedup & diff for any dataset — powered by CRDTs**

[![Maven Central](https://img.shields.io/maven-central/v/io.optitransfer/crdt-merge.svg)](https://central.sonatype.com/artifact/io.optitransfer/crdt-merge)
[![Java 17+](https://img.shields.io/badge/Java-17+-red.svg)](https://openjdk.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/MIT)
[![Tests: 79/79](https://img.shields.io/badge/tests-79%2F79-brightgreen.svg)](https://github.com/mgillr/crdt-merge-java)

**Merge any two datasets in one function call. No conflicts. No coordination. No data loss.**

[Quick Start](#-quick-start) • [API Reference](#-api-reference) • [Why CRDTs](#-why-crdts) • [All Languages](#-available-in-every-language)

</div>

---

## 🌐 Available in Every Language

| Language | Package | Install | Repo |
|---|---|---|---|
| **Python** 🐍 | `crdt-merge` | `pip install crdt-merge` | [crdt-merge](https://github.com/mgillr/crdt-merge) |
| **TypeScript** | `crdt-merge` | `npm install crdt-merge` | [crdt-merge-ts](https://github.com/mgillr/crdt-merge-ts) |
| **Rust** 🦀 | `crdt-merge` | `cargo add crdt-merge` | [crdt-merge-rs](https://github.com/mgillr/crdt-merge-rs) |
| **Java** ☕ | `io.optitransfer:crdt-merge` | Maven / Gradle | **You are here** |
| **CLI** 🖥️ | included in Rust | `cargo install crdt-merge` | [crdt-merge-rs](https://github.com/mgillr/crdt-merge-rs) |

> **[🤗 Try it in the browser →](https://huggingface.co/spaces/Optitransfer/crdt-merge)**

---

## 🎯 The Problem

You have two versions of a dataset. Maybe two Spark jobs ran in parallel. Maybe two microservices updated the same records. Maybe you're merging data from multiple sources.

**Today:** Write custom merge scripts, lose data, or block on a coordinator.

**With crdt-merge:** One method call. Zero conflicts. Mathematically guaranteed.

```java
List<Map<String, Object>> merged = CrdtMerge.merge(datasetA, datasetB, "id"); // done.
```

## ⚡ Quick Start

### Maven

```xml
<dependency>
  <groupId>io.optitransfer</groupId>
  <artifactId>crdt-merge</artifactId>
  <version>0.1.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'io.optitransfer:crdt-merge:0.1.0'
```

### From Source

```bash
git clone https://github.com/mgillr/crdt-merge-java.git
cd crdt-merge-java
mvn package
```

## 📖 API Reference

### Merge Two Datasets

```java
import io.optitransfer.crdtmerge.CrdtMerge;

List<Map<String, Object>> teamA = List.of(
    Map.of("id", 1, "name", "Alice", "role", "engineer"),
    Map.of("id", 2, "name", "Bob", "role", "designer")
);

List<Map<String, Object>> teamB = List.of(
    Map.of("id", 2, "name", "Robert", "role", "designer"),
    Map.of("id", 3, "name", "Charlie", "role", "pm")
);

List<Map<String, Object>> merged = CrdtMerge.merge(teamA, teamB, "id");
// id=1: Alice (only in A — preserved)
// id=2: Robert (B wins — latest)
// id=3: Charlie (only in B — preserved)
```

### Deduplicate

```java
import io.optitransfer.crdtmerge.DedupEngine;

List<Map<String, Object>> data = List.of(
    Map.of("name", "Alice"),
    Map.of("name", "Alicia"),
    Map.of("name", "Bob")
);

DedupEngine.DedupResult result = CrdtMerge.dedup(data, "name", 0.7);
System.out.println("Unique: " + result.unique.size());
System.out.println("Duplicates: " + result.duplicates.size());
```

### Structural Diff

```java
import io.optitransfer.crdtmerge.DiffEngine;

DiffEngine.DiffResult diff = CrdtMerge.diff(oldData, newData, "id");
System.out.println(diff.summary);
// "+5 added, -2 removed, ~3 modified, =990 unchanged"
```

### Deep JSON Merge

```java
import com.google.gson.JsonObject;

JsonObject configA = JsonParser.parseString(
    "{\"model\": {\"name\": \"bert\", \"layers\": 12}, \"tags\": [\"nlp\"]}"
).getAsJsonObject();

JsonObject configB = JsonParser.parseString(
    "{\"model\": {\"name\": \"bert-large\", \"dropout\": 0.1}, \"tags\": [\"qa\"]}"
).getAsJsonObject();

JsonObject merged = CrdtMerge.mergeJson(configA, configB);
// {"model": {"name": "bert-large", "layers": 12, "dropout": 0.1}, "tags": ["nlp", "qa"]}
```

### Core CRDT Types

```java
import io.optitransfer.crdtmerge.crdt.*;

// Distributed counter
GCounter counterA = new GCounter();
counterA.increment("server-1", 100);

GCounter counterB = new GCounter();
counterB.increment("server-2", 200);

GCounter merged = counterA.merge(counterB);
System.out.println(merged.value()); // 300

// Last-writer-wins register
LWWRegister<String> regA = new LWWRegister<>("Alice", 1000L);
LWWRegister<String> regB = new LWWRegister<>("Alicia", 2000L);
System.out.println(regA.merge(regB).value()); // "Alicia" (later wins)

// Observed-remove set
ORSet<String> setA = new ORSet<>();
setA.add("item1");
ORSet<String> setB = new ORSet<>();
setB.add("item2");
ORSet<String> mergedSet = setA.merge(setB);
System.out.println(mergedSet.contains("item1")); // true
System.out.println(mergedSet.contains("item2")); // true
```

## 🧠 Why CRDTs

**CRDT** = Conflict-free Replicated Data Type. A data structure with one mathematical superpower:

> **Any two copies can merge — in any order, at any time — and the result is always identical and always correct.**

Three mathematical guarantees (proven, not hoped):

| Property | What it means |
|---|---|
| **Commutative** | `merge(A, B) == merge(B, A)` — order doesn't matter |
| **Associative** | `merge(merge(A, B), C) == merge(A, merge(B, C))` — grouping doesn't matter |
| **Idempotent** | `merge(A, A) == A` — re-merging is safe |

This means: **zero coordination, zero locks, zero conflicts.**

### Built-in CRDT Types

| Type | Use Case | Example |
|---|---|---|
| `GCounter` | Grow-only counters | Download counts, page views |
| `PNCounter` | Increment + decrement | Stock levels, balances |
| `LWWRegister<T>` | Single value (latest wins) | Name, email, status fields |
| `ORSet<T>` | Add/remove set | Tags, memberships, dedup sets |

## Features

- **Tabular Merge** — Merge two lists of maps by primary key using CRDT LWW semantics
- **Deduplication** — Exact and fuzzy dedup using Jaccard similarity on character bigrams
- **Structural Diff** — See added, removed, and modified rows between two datasets
- **JSON Merge** — Deep merge of nested JSON objects with conflict-free resolution
- **Core CRDTs** — Production-ready GCounter, PNCounter, LWWRegister, ORSet
- **Zero config** — One dependency (Gson), works with any Map/List data

## 🏗️ Use Cases

- **Spark pipelines**: Merge partitioned outputs without a coordinator
- **Microservices**: Each service maintains local state, merge on demand
- **Event sourcing**: Merge event streams from multiple sources
- **Data lakes**: Combine datasets from different teams/regions
- **Cache reconciliation**: Merge divergent cache states after network partition

## Requirements

- Java 17+
- Gson 2.10.1+ (included via Maven)

## Building

```bash
mvn compile   # Compile
mvn test      # Run tests (79/79 passing)
mvn package   # Create JAR
```

## 📄 License

MIT — use it for anything.

---

<div align="center">

Built with math, not hope. 🧬

**[⭐ Star on GitHub](https://github.com/mgillr/crdt-merge-java)** • **[🤗 Try on HuggingFace](https://huggingface.co/spaces/Optitransfer/crdt-merge)** • **[📦 Maven Central](https://central.sonatype.com/artifact/io.optitransfer/crdt-merge)**

</div>

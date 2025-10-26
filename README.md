# City Transportation Network – Minimum Spanning Tree (MST)

Optimize a city's road network by building a **Minimum Spanning Tree** using **Prim’s** and **Kruskal’s** algorithms.  
The app reads graphs from JSON, computes two MSTs, records performance metrics, and writes results to JSON/CSV. It also includes dataset generation and JUnit 4 tests.

---

## Features
- **Custom graph model**: `Graph` & `Edge` classes (undirected, weighted).
- **Two MST algorithms**: Prim & Kruskal with operation counting.
- **I/O**: read graphs from JSON; write `output.json` and `summary.csv`.
- **Synthetic datasets**: small / medium / large connected graphs.
- **Tests**: JUnit 4 correctness & consistency tests.
- **Gradle** project layout.

---

## Project Structure (Gradle)
```
.
├─ build.gradle
├─ src
│  ├─ main
│  │  ├─ java
│  │  │  ├─ main/java/Main.java
│  │  │  └─ main/java/model_classes/{Graph.java, Edge.java, ...}
│  │  └─ resources
│  │     └─ input.json          # default input (optional, recommended)
│  └─ test
│     └─ java                   # JUnit 4 tests
└─ README.md
```
> Tip: If you currently use `src/main.java/`, consider moving to the standard `src/main/java/` and package names without dots like `main.java`.

---

## Build & Run

### Build
```bash
./gradlew clean build
```

### Run with input from resources (recommended)
Place `input.json` in `src/main/resources` and read via classpath in code.
```bash
./gradlew run
```

### Run with explicit input path
```bash
./gradlew run --args="path/to/input.json"
# or
java -jar build/libs/<artifact>.jar path/to/input.json
```

**Outputs:**
- `output.json` – detailed result per graph (both algorithms)
- `summary.csv` – per-graph comparison (time & operation counts)

> Troubleshooting: `FileNotFoundException: input.json` means the file is searched **from the working directory**. Either pass an absolute/relative path via `--args`, or put `input.json` under `src/main/resources` and load it from the classpath.

---

## JSON Formats

### Input (`input.json`)
```json
{
  "graphs": [
    {
      "id": 1,
      "nodes": ["A", "B", "C"],
      "edges": [
        {"from": "A", "to": "B", "weight": 4},
        {"from": "B", "to": "C", "weight": 2}
      ]
    }
  ]
}
```

### Output (`output.json`)
```json
{
  "results": [
    {
      "graph_id": 1,
      "input_stats": {"vertices": 5, "edges": 7},
      "prim": {
        "mst_edges": [{"from": "B", "to": "C", "weight": 2}],
        "total_cost": 16,
        "operations_count": 42,
        "execution_time_ms": 1.52
      },
      "kruskal": { ... }
    }
  ]
}
```

**Metric meanings**
- `total_cost` – sum of MST edge weights.
- `operations_count` – key operations per algorithm:
  - Prim: PQ add/poll, visited checks, comparator comparisons.
  - Kruskal: sort comparisons, `find/union` calls in DSU.
- `execution_time_ms` – wall time in milliseconds.

---

## Dataset Generation
The app can generate connected random graphs (first a random spanning tree, then extra edges). Reproducible with a fixed seed. Typical bundles:
- **Small** (4–6 vertices),
- **Medium** (10–15),
- **Large** (20–30+).

Saved as `input_small.json`, `input_medium.json`, `input_large.json` (or a single combined file).

---

## Testing (JUnit 4)
```bash
./gradlew test
```
Covers:
- Same MST total cost for Prim & Kruskal.
- |E_MST| = V − 1, MST is acyclic and connects all vertices.
- Graceful handling of disconnected graphs.
- Non-negative time and operation counts; reproducible results on the same dataset.

---

## Analytical Report (Results • Interpretation • Conclusions)

Below is a concise analysis based on your `output.json`. Times are shown in **milliseconds** (decimal dot).

### Results Summary

| Graph | V | E | MST Cost | Prim (ms) | Kruskal (ms) | Prim ops | Kruskal ops |
|---:|---:|---:|---:|---:|---:|---:|---:|
| 1 | 4 | 5 | **29** | 0.02 | 0.00 | 27 | 30 |
| 2 | 5 | 7 | **29** | 0.01 | 0.00 | 45 | 48 |
| 3 | 6 | 9 | **48** | 0.01 | 0.00 | 74 | 61 |
| 4 | 10 | 15 | **92** | 0.01 | 0.00 | 114 | 117 |
| 5 | 12 | 20 | **84** | 0.02 | 0.01 | 202 | 159 |
| 6 | 15 | 28 | **77** | 0.03 | 0.01 | 263 | 238 |
| 7 | 20 | 35 | **129** | 0.06 | 0.01 | 384 | 330 |
| 8 | 25 | 50 | **160** | 0.05 | 0.02 | 487 | 469 |
| 9 | 30 | 70 | **179** | 0.06 | 0.02 | 720 | 698 |

**Key observation:** the **MST total cost always matches** between Prim and Kruskal (as expected). Tree structure may differ; performance metrics differ more.

### Interpretation
- **Small graphs (≤ 10 vertices):** negligible differences — constant factors dominate.
- **Medium/Large graphs (12–30 vertices; E ≈ 1.5–3·V):** **Kruskal** tends to perform **fewer operations** and be faster overall.
- **Why:** Kruskal performs a single global sort + cheap DSU; Prim performs many small PQ operations during growth.

### Conclusions & Recommendations
- **Sparse graphs (E ~ V):** prefer **Kruskal** (simpler & usually faster).
- **Dense graphs (many edges):** **Prim** remains competitive (especially with an efficient priority queue).
- **Streaming/growing scenarios:** Prim is convenient when you gradually expand the tree.
- Always benchmark on **your real graph density**; constants and memory layout matter.


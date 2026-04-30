# 09 — Dijkstra's Algorithm: City Navigation

## The Story

A navigation app needs to find the fastest route between two locations in a city. Roads have travel times. The path with the fewest hops (BFS) isn't necessarily the fastest — a highway with one long hop may beat a zigzag through side streets. Dijkstra's algorithm finds the minimum-cost path on a weighted graph.

---

## The Core Idea

Dijkstra maintains a **tentative distance** for every vertex (initially ∞, except the source which is 0). It uses a **min-heap** to always process the vertex with the smallest known distance next. When it processes a vertex, it "relaxes" all outgoing edges: if going through this vertex would give a neighbor a shorter path, update the neighbor's distance.

```
Graph: 0→1(5), 0→2(10), 1→2(3)
Source: 0

Initial: dist=[0, ∞, ∞], PQ=[(0,0)]

Pop (0,0): relax 0→1: dist[1]=5, relax 0→2: dist[2]=10
          PQ=[(5,1), (10,2)]

Pop (5,1): relax 1→2: 5+3=8 < 10, dist[2]=8
          PQ=[(8,2), (10,2)]

Pop (8,2): no outgoing edges. Done.
          Stale (10,2) popped and skipped.

Result: dist=[0, 5, 8]
```

---

## Why the Greedy Step Works

Once a vertex is popped from the min-heap, its distance is **final**. Because all edge weights are non-negative, no future path can make it shorter — any path through a vertex we haven't visited yet would have to pass through the heap, which currently contains only vertices with distances ≥ the current one.

This is why **Dijkstra fails with negative edge weights** — a negative edge could create a shorter path to an already-finalized vertex.

---

## Complexity

| Operation | Complexity |
|-----------|-----------|
| Time (binary heap) | O((V + E) log V) |
| Space | O(V + E) |

For dense graphs (E ≈ V²), a Fibonacci heap gives O(E + V log V), but in practice Java's `PriorityQueue` with "lazy deletion" (skipping stale entries) is sufficient.

---

## Path Reconstruction

Track a `prev[]` array: when relaxing vertex `u → v`, set `prev[v] = u`. To reconstruct the path to `target`, follow `prev` pointers back to the source, then reverse.

---

## Commands

```bash
mvn compile exec:java
mvn test
```

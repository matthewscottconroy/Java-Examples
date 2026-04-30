# 10 — Topological Sort: Course Prerequisite Planner

## The Story

A university student wants to complete a computer science degree. Some courses have prerequisites — you can't take Algorithms before Data Structures, and you can't take OS before Algorithms. Topological sort produces a valid study sequence: a linear ordering of courses where every prerequisite appears before the course that requires it.

---

## What Topological Sort Produces

Given a directed acyclic graph (DAG), a topological ordering is a sequence of all vertices such that for every directed edge `u → v`, `u` comes before `v`.

```
Graph: CS101 → CS201 → CS202 → CS301
              ↘              ↗
              CS202 is also needed by CS401
```

A valid ordering: CS101, CS201, CS202, CS301, CS401 (among others).

Topological sort is **only defined on DAGs**. A cycle means "course A requires B which requires A" — an impossible circular prerequisite.

---

## Two Algorithms

### Kahn's Algorithm (BFS-based)

1. Compute **in-degree** (number of incoming edges) for every vertex.
2. Enqueue all vertices with in-degree 0 (no prerequisites).
3. Repeatedly dequeue a vertex, add it to the result, and for each of its successors, decrement their in-degree. If a successor's in-degree reaches 0, enqueue it.
4. If the result contains fewer than V vertices, there's a cycle.

**Intuition:** always pick something that has no remaining prerequisites.

### DFS Post-Order

Run DFS. When all of a vertex's descendants have been processed, push the vertex onto a stack. Reading the stack gives a valid topological order.

**Intuition:** a vertex goes into the result only after everything it points to is already there.

---

## Comparison

| Property | Kahn's | DFS |
|----------|--------|-----|
| Style | Iterative (BFS) | Recursive |
| Cycle detection | Output size < V | Three-color marking |
| Result stability | Depends on queue order | Depends on DFS order |
| Multiple orderings? | Yes | Yes |

Both run in O(V + E).

---

## Commands

```bash
mvn compile exec:java
mvn test
```

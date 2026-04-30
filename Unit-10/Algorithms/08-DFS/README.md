# 08 — Depth-First Search: The Dependency Analyser

## The Story

A build system needs to check whether module dependencies form a cycle — because if module A depends on B which depends on A, nothing can compile. DFS detects this by tracking which vertices are currently on the active recursion stack. If it ever reaches a vertex already on the stack, there's a cycle.

---

## How DFS Works

DFS uses a **stack** (the call stack in recursion, or an explicit stack iteratively). From a source vertex, dive as far as possible along each branch before backtracking.

```
Graph: 0→1→3, 0→2
DFS from 0:

Visit 0 → dive into 1 → dive into 3 → 3 has no unvisited neighbors → backtrack
            → 3 done (post-order add)
          → 1 done (post-order add)
       → dive into 2 → 2 has no unvisited neighbors → backtrack
          → 2 done (post-order add)
       → 0 done (post-order add)

Post-order: [3, 1, 2, 0]
```

**Post-order** means a vertex is added to the output *after* all vertices reachable from it. Reversing post-order gives a **topological sort**.

---

## Three-Color Cycle Detection

Standard visited/unvisited tracking doesn't distinguish "currently on the stack" from "fully explored". Three colors do:

| Color | Meaning |
|-------|---------|
| White (0) | Not yet visited |
| Grey (1) | Currently on the recursion stack |
| Black (2) | Fully explored, all descendants visited |

A **back edge** (edge to a grey vertex) means we've found a cycle: the path from that grey vertex through the stack back to itself forms a loop.

---

## Iterative DFS

Recursive DFS throws `StackOverflowError` on very deep graphs (10,000+ levels). The iterative version uses an explicit `Deque` as a stack and achieves the same traversal order without touching the JVM call stack.

---

## DFS vs BFS

| Property | DFS | BFS |
|----------|-----|-----|
| Structure | Stack (LIFO) | Queue (FIFO) |
| Path found | Not necessarily shortest | Shortest (hop count) |
| Memory | O(depth) | O(width) |
| Good for | Cycle detection, topo sort, SCC | Shortest paths, level-order |

---

## Commands

```bash
mvn compile exec:java
mvn test
```

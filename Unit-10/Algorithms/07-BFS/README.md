# 07 — Breadth-First Search: Office Navigation

## The Story

A new employee needs to find the shortest route from the lobby to the server room — passing through the fewest doorways. The office is a graph: rooms are vertices, doorways are edges. BFS explores rooms in order of how many doorways away they are, so the first time it reaches the server room, it has used the fewest doors possible.

---

## How BFS Works

BFS uses a **queue** (FIFO). Start at the source, mark it visited, then repeatedly:
1. Dequeue a vertex.
2. For each unvisited neighbor, mark it visited, record its distance, enqueue it.

Because the queue is FIFO, vertices are always dequeued in non-decreasing distance order. The first time you reach a vertex, you've used the minimum number of edges.

```
Graph: 0-1-2-3-4 (linear)
BFS from 0:

Queue: [0]          visited: {0}      dist: [0,-,-,-,-]
Dequeue 0 → enqueue neighbors 1
Queue: [1]          visited: {0,1}    dist: [0,1,-,-,-]
Dequeue 1 → enqueue 2
Queue: [2]          visited: {0,1,2}  dist: [0,1,2,-,-]
...
```

**Time: O(V + E)** — every vertex dequeued once, every edge examined once.  
**Space: O(V)** — queue and visited array.

---

## What BFS Guarantees That DFS Does Not

BFS finds **shortest paths by hop count** in unweighted graphs. DFS makes no such guarantee — it may reach a vertex via a long detour before finding a short path.

When edges have weights, use Dijkstra instead (which is BFS with a priority queue).

---

## BFS as a Template

Many algorithms are BFS in disguise:
- **Connected components** — run BFS from each unvisited vertex
- **Bipartite check** — BFS, alternating colors; a conflict means non-bipartite
- **0-1 BFS** — deque instead of queue; edges with weight 0 go to front, weight 1 go to back
- **Multi-source BFS** — enqueue all sources at once; each vertex gets distance to nearest source

---

## Commands

```bash
mvn compile exec:java
mvn test
```

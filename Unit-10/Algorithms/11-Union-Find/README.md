# 11 — Union-Find: Network Connectivity

## The Story

A data centre is building its network incrementally. Cables are installed one at a time, each connecting two servers. After each cable is added, engineers need to answer instantly: "Can server A reach server B?" Re-scanning the entire network graph after each cable would be O(V + E) per query. Union-Find answers each query in effectively O(1), even as the network grows.

---

## The Data Structure

Union-Find maintains a collection of **disjoint sets**, each represented as a tree. Every element points to its parent; the **root** of a tree is the canonical representative (the "name") of that set.

```
Initial (5 elements, 5 sets):
  0   1   2   3   4
  ↑   ↑   ↑   ↑   ↑
 (each is its own root)

After union(0,1), union(2,3), union(0,2):
       0
      / \
     1   2
          \
           3
```

---

## Two Critical Optimizations

Without optimizations, trees can degenerate into chains → O(n) per operation.

### Path Compression
After `find(x)`, point every node on the path directly to the root. Future finds on those nodes are O(1).

```java
int find(int x) {
    if (parent[x] != x) parent[x] = find(parent[x]);  // flatten the path
    return parent[x];
}
```

### Union by Rank
When merging two trees, attach the shorter tree under the taller one. This prevents chains from forming.

Together, these give **O(α(n)) amortized** time per operation, where α is the inverse Ackermann function — less than 5 for any realistic input.

---

## Classic Applications

**Connected components** — how many independent subgraphs?  
**Kruskal's MST** — add edges in weight order; skip if both endpoints already connected.  
**Cycle detection** — if `union(u, v)` returns false, (u, v) would form a cycle.  
**Percolation** — does liquid pass through a grid from top to bottom?

---

## Commands

```bash
mvn compile exec:java
mvn test
```

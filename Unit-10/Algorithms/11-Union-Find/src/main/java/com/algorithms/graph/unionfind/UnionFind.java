package com.algorithms.graph.unionfind;

/**
 * Union-Find (Disjoint Set Union) with path compression and union by rank.
 *
 * <p>Maintains a collection of disjoint sets. Supports two operations:
 * <ul>
 *   <li>{@link #find} — returns the canonical representative of a set. O(α(n)) amortized.</li>
 *   <li>{@link #union} — merges two sets. O(α(n)) amortized.</li>
 * </ul>
 *
 * <p>α(n) is the inverse Ackermann function — effectively constant for any
 * realistic n. Both optimisations together give near-constant amortized time.
 *
 * <p>Applications: connected components, Kruskal's MST, cycle detection,
 * network connectivity, image segmentation.
 */
public class UnionFind {

    private final int[] parent;
    private final int[] rank;
    private int components;

    public UnionFind(int n) {
        parent = new int[n];
        rank   = new int[n];
        components = n;
        for (int i = 0; i < n; i++) parent[i] = i;
    }

    /** Returns the root representative of the set containing {@code x}. */
    public int find(int x) {
        if (parent[x] != x) parent[x] = find(parent[x]);  // path compression
        return parent[x];
    }

    /**
     * Merges the sets containing {@code x} and {@code y}.
     * Returns true if they were in different sets (a merge happened).
     */
    public boolean union(int x, int y) {
        int rx = find(x), ry = find(y);
        if (rx == ry) return false;
        // Union by rank: attach smaller tree under taller tree
        if      (rank[rx] < rank[ry]) parent[rx] = ry;
        else if (rank[rx] > rank[ry]) parent[ry] = rx;
        else { parent[ry] = rx; rank[rx]++; }
        components--;
        return true;
    }

    /** Returns true if {@code x} and {@code y} are in the same set. */
    public boolean connected(int x, int y) {
        return find(x) == find(y);
    }

    /** Returns the number of disjoint sets remaining. */
    public int componentCount() {
        return components;
    }
}

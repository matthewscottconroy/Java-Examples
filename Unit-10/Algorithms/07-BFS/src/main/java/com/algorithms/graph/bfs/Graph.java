package com.algorithms.graph.bfs;

import java.util.*;

/**
 * An unweighted, undirected graph backed by an adjacency list.
 * Vertices are identified by integer IDs 0..n-1.
 */
public class Graph {

    private final int vertexCount;
    private final List<List<Integer>> adj;

    public Graph(int vertexCount) {
        this.vertexCount = vertexCount;
        this.adj = new ArrayList<>(vertexCount);
        for (int i = 0; i < vertexCount; i++) adj.add(new ArrayList<>());
    }

    public void addEdge(int u, int v) {
        adj.get(u).add(v);
        adj.get(v).add(u);
    }

    public List<Integer> neighbors(int v) { return adj.get(v); }
    public int vertexCount()              { return vertexCount; }
}

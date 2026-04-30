package com.algorithms.graph.dijkstra;

import java.util.*;

/**
 * Directed weighted graph using an adjacency list.
 * Edge weights must be non-negative (required by Dijkstra).
 */
public class WeightedGraph {

    public record Edge(int to, int weight) {}

    private final int vertexCount;
    private final List<List<Edge>> adj;

    public WeightedGraph(int vertexCount) {
        this.vertexCount = vertexCount;
        this.adj = new ArrayList<>(vertexCount);
        for (int i = 0; i < vertexCount; i++) adj.add(new ArrayList<>());
    }

    public void addEdge(int from, int to, int weight) {
        adj.get(from).add(new Edge(to, weight));
    }

    public void addUndirectedEdge(int u, int v, int weight) {
        addEdge(u, v, weight);
        addEdge(v, u, weight);
    }

    public List<Edge> neighbors(int v) { return adj.get(v); }
    public int vertexCount()           { return vertexCount; }
}

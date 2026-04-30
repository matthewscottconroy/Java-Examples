package com.algorithms.graph.topo;

import java.util.*;

/**
 * Topological sort on a directed acyclic graph (DAG).
 *
 * <p>A topological ordering is a linear ordering of vertices such that for
 * every directed edge u → v, vertex u appears before v.
 * This is only possible if the graph has no directed cycles (it's a DAG).
 *
 * <p>Two classic algorithms:
 * <ul>
 *   <li><b>Kahn's algorithm</b> — BFS-based, iteratively removes vertices with in-degree 0.
 *       Naturally detects cycles (output shorter than vertex count).</li>
 *   <li><b>DFS post-order</b> — reverse post-order of DFS is a valid topo sort.</li>
 * </ul>
 *
 * <p>Time: O(V + E).  Space: O(V).
 */
public final class TopologicalSort {

    private TopologicalSort() {}

    /**
     * Kahn's algorithm (BFS-based).
     * Returns a valid topological order, or empty if the graph has a cycle.
     */
    public static Optional<List<Integer>> kahn(int vertexCount, List<List<Integer>> adj) {
        int[] inDegree = new int[vertexCount];
        for (int u = 0; u < vertexCount; u++)
            for (int v : adj.get(u)) inDegree[v]++;

        Queue<Integer> queue = new ArrayDeque<>();
        for (int v = 0; v < vertexCount; v++)
            if (inDegree[v] == 0) queue.add(v);

        List<Integer> order = new ArrayList<>();
        while (!queue.isEmpty()) {
            int u = queue.poll();
            order.add(u);
            for (int v : adj.get(u)) {
                if (--inDegree[v] == 0) queue.add(v);
            }
        }
        return order.size() == vertexCount ? Optional.of(order) : Optional.empty();
    }

    /**
     * DFS post-order topological sort.
     * Returns a valid topological order, or empty if the graph has a cycle.
     */
    public static Optional<List<Integer>> dfs(int vertexCount, List<List<Integer>> adj) {
        int[] color = new int[vertexCount];  // 0=white, 1=grey, 2=black
        Deque<Integer> stack = new ArrayDeque<>();
        boolean[] hasCycle = {false};

        for (int v = 0; v < vertexCount; v++) {
            if (color[v] == 0) visit(v, adj, color, stack, hasCycle);
        }
        if (hasCycle[0]) return Optional.empty();

        List<Integer> order = new ArrayList<>(stack);
        return Optional.of(order);
    }

    private static void visit(int v, List<List<Integer>> adj,
                               int[] color, Deque<Integer> stack, boolean[] hasCycle) {
        if (hasCycle[0]) return;
        color[v] = 1;
        for (int nb : adj.get(v)) {
            if (color[nb] == 1) { hasCycle[0] = true; return; }
            if (color[nb] == 0) visit(nb, adj, color, stack, hasCycle);
        }
        color[v] = 2;
        stack.push(v);  // push after all descendants → stack top = earliest in order
    }

    /** Convenience: build an adjacency list for a given vertex count. */
    public static List<List<Integer>> newAdj(int n) {
        List<List<Integer>> adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        return adj;
    }
}

package com.algorithms.graph.dfs;

import java.util.*;

/**
 * Depth-first search and cycle detection on directed and undirected graphs.
 *
 * <p>DFS dives as deep as possible before backtracking. It is the foundation
 * for many graph algorithms: cycle detection, topological sort, strongly
 * connected components, and flood fill.
 *
 * <p>Time: O(V + E).  Space: O(V) for the recursion stack (or explicit stack).
 */
public final class DFS {

    private DFS() {}

    /**
     * Returns vertices reachable from {@code source} in DFS finish order
     * (post-order: a vertex appears after all vertices reachable from it).
     */
    public static List<Integer> dfsOrder(Graph g, int source) {
        boolean[] visited = new boolean[g.vertexCount()];
        List<Integer> order = new ArrayList<>();
        dfsRecursive(g, source, visited, order);
        return order;
    }

    private static void dfsRecursive(Graph g, int v, boolean[] visited, List<Integer> out) {
        visited[v] = true;
        for (int nb : g.neighbors(v)) {
            if (!visited[nb]) dfsRecursive(g, nb, visited, out);
        }
        out.add(v);  // post-order
    }

    /**
     * Returns true if the directed graph contains a cycle.
     * Uses three-color marking: white (unvisited), grey (in current path), black (done).
     */
    public static boolean hasCycleDirected(Graph g) {
        int n = g.vertexCount();
        int[] color = new int[n];  // 0=white, 1=grey, 2=black
        for (int v = 0; v < n; v++) {
            if (color[v] == 0 && cycleCheck(g, v, color)) return true;
        }
        return false;
    }

    private static boolean cycleCheck(Graph g, int v, int[] color) {
        color[v] = 1;  // grey: currently on the recursion stack
        for (int nb : g.neighbors(v)) {
            if (color[nb] == 1) return true;  // back edge → cycle
            if (color[nb] == 0 && cycleCheck(g, nb, color)) return true;
        }
        color[v] = 2;  // black: fully explored
        return false;
    }

    /**
     * Returns true if the undirected graph contains a cycle.
     */
    public static boolean hasCycleUndirected(Graph g) {
        int n = g.vertexCount();
        boolean[] visited = new boolean[n];
        for (int v = 0; v < n; v++) {
            if (!visited[v] && cycleCheckUndirected(g, v, -1, visited)) return true;
        }
        return false;
    }

    private static boolean cycleCheckUndirected(Graph g, int v, int parent, boolean[] visited) {
        visited[v] = true;
        for (int nb : g.neighbors(v)) {
            if (!visited[nb]) {
                if (cycleCheckUndirected(g, nb, v, visited)) return true;
            } else if (nb != parent) {
                return true;  // found a back edge that isn't the edge we came from
            }
        }
        return false;
    }

    /**
     * Returns all vertices reachable from {@code source} using an explicit
     * stack (iterative DFS) to avoid stack overflow on very deep graphs.
     */
    public static List<Integer> iterativeDFS(Graph g, int source) {
        boolean[] visited = new boolean[g.vertexCount()];
        List<Integer> order = new ArrayList<>();
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(source);
        while (!stack.isEmpty()) {
            int v = stack.pop();
            if (visited[v]) continue;
            visited[v] = true;
            order.add(v);
            // Push neighbors in reverse so we process them left-to-right
            List<Integer> nbrs = g.neighbors(v);
            for (int i = nbrs.size() - 1; i >= 0; i--) {
                if (!visited[nbrs.get(i)]) stack.push(nbrs.get(i));
            }
        }
        return order;
    }
}

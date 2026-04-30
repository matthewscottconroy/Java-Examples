package com.algorithms.graph.bfs;

import java.util.*;

/**
 * Breadth-first search on an unweighted graph.
 *
 * <p>BFS visits vertices level-by-level (by hop count from the source).
 * This guarantees that the first time a vertex is visited, the path taken
 * is the shortest path (fewest edges) from the source.
 *
 * <p>Time: O(V + E) — each vertex and edge examined at most once.
 * Space: O(V) — queue and visited array.
 */
public final class BFS {

    private BFS() {}

    /**
     * Returns the shortest-path distance (in hops) from {@code source} to every
     * reachable vertex. Unreachable vertices get distance -1.
     */
    public static int[] shortestDistances(Graph g, int source) {
        int n = g.vertexCount();
        int[] dist = new int[n];
        Arrays.fill(dist, -1);
        dist[source] = 0;

        Queue<Integer> queue = new ArrayDeque<>();
        queue.add(source);

        while (!queue.isEmpty()) {
            int v = queue.poll();
            for (int nb : g.neighbors(v)) {
                if (dist[nb] == -1) {
                    dist[nb] = dist[v] + 1;
                    queue.add(nb);
                }
            }
        }
        return dist;
    }

    /**
     * Returns the shortest path from {@code source} to {@code target} as an
     * ordered list of vertices, or an empty list if no path exists.
     */
    public static List<Integer> shortestPath(Graph g, int source, int target) {
        int n = g.vertexCount();
        int[] prev = new int[n];
        Arrays.fill(prev, -1);
        boolean[] visited = new boolean[n];
        visited[source] = true;

        Queue<Integer> queue = new ArrayDeque<>();
        queue.add(source);

        while (!queue.isEmpty()) {
            int v = queue.poll();
            if (v == target) break;
            for (int nb : g.neighbors(v)) {
                if (!visited[nb]) {
                    visited[nb] = true;
                    prev[nb] = v;
                    queue.add(nb);
                }
            }
        }

        if (!visited[target]) return List.of();
        List<Integer> path = new ArrayList<>();
        for (int v = target; v != -1; v = prev[v]) path.add(0, v);
        return path;
    }

    /**
     * Returns the vertices reachable from {@code source} in BFS order.
     */
    public static List<Integer> reachable(Graph g, int source) {
        int n = g.vertexCount();
        boolean[] visited = new boolean[n];
        List<Integer> order = new ArrayList<>();
        Queue<Integer> queue = new ArrayDeque<>();

        visited[source] = true;
        queue.add(source);

        while (!queue.isEmpty()) {
            int v = queue.poll();
            order.add(v);
            for (int nb : g.neighbors(v)) {
                if (!visited[nb]) {
                    visited[nb] = true;
                    queue.add(nb);
                }
            }
        }
        return order;
    }
}

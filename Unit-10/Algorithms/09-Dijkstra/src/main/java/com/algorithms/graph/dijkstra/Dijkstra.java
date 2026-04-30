package com.algorithms.graph.dijkstra;

import java.util.*;

/**
 * Dijkstra's shortest-path algorithm on a directed weighted graph.
 *
 * <p>Works correctly only with non-negative edge weights. Uses a min-heap
 * (Java's PriorityQueue) to always expand the closest unfinalized vertex.
 *
 * <p>Time: O((V + E) log V) with a binary heap.
 * Space: O(V + E).
 */
public final class Dijkstra {

    private Dijkstra() {}

    public record Result(int[] dist, int[] prev) {

        /** Returns the shortest path from source to {@code target}, or empty list. */
        public List<Integer> path(int target) {
            if (dist[target] == Integer.MAX_VALUE) return List.of();
            List<Integer> path = new ArrayList<>();
            for (int v = target; v != -1; v = prev[v]) path.add(0, v);
            return path;
        }
    }

    /**
     * Computes shortest distances from {@code source} to all vertices.
     * Unreachable vertices get distance {@link Integer#MAX_VALUE}.
     */
    public static Result compute(WeightedGraph g, int source) {
        int n = g.vertexCount();
        int[] dist = new int[n];
        int[] prev = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(prev, -1);
        dist[source] = 0;

        // PriorityQueue entry: [distance, vertex]
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(e -> e[0]));
        pq.offer(new int[]{0, source});

        while (!pq.isEmpty()) {
            int[] top = pq.poll();
            int d = top[0], v = top[1];
            if (d > dist[v]) continue;  // stale entry — skip

            for (WeightedGraph.Edge edge : g.neighbors(v)) {
                int newDist = dist[v] + edge.weight();
                if (newDist < dist[edge.to()]) {
                    dist[edge.to()] = newDist;
                    prev[edge.to()] = v;
                    pq.offer(new int[]{newDist, edge.to()});
                }
            }
        }
        return new Result(dist, prev);
    }
}

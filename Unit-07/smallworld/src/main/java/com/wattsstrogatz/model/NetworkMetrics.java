package com.wattsstrogatz.model;

import java.util.*;

/**
 * Computes the two key Watts-Strogatz metrics for a {@link Network}.
 *
 * <h2>Clustering coefficient C</h2>
 * <p>For each node i with degree k_i, count the actual edges among its
 * neighbours divided by the maximum possible k_i*(k_i−1)/2.  C is the mean
 * over all nodes with degree ≥ 2.  Time complexity: O(n · k²) for a network
 * with typical degree k.
 *
 * <h2>Average shortest path length L</h2>
 * <p>Computed via BFS from every node; only reachable pairs are averaged.
 * Returns {@link Double#POSITIVE_INFINITY} for disconnected networks.
 * Time complexity: O(n · (n + m)) where m = edge count.
 *
 * <h2>Relative metrics (C/C₀, L/L₀)</h2>
 * <p>The original paper plots normalised ratios with respect to the ring-lattice
 * baseline.  Use
 * {@link com.wattsstrogatz.simulation.WattsStrogatzSimulation#getRelativeMetrics()}
 * rather than computing these manually.
 *
 * @see com.wattsstrogatz.simulation.WattsStrogatzSimulation
 */
public final class NetworkMetrics {

    private NetworkMetrics() {}

    // -------------------------------------------------------------------------
    // Clustering coefficient
    // -------------------------------------------------------------------------

    /**
     * Computes the global average clustering coefficient.
     *
     * @param network the network to measure
     * @return average clustering coefficient in [0, 1], or 0.0 if no node
     *         has degree >= 2
     */
    public static double averageClusteringCoefficient(Network network) {
        Objects.requireNonNull(network);
        double total = 0.0;
        int    count = 0;
        for (int i = 0; i < network.getNodeCount(); i++) {
            double local = localClusteringCoefficient(network, i);
            if (local >= 0) { total += local; count++; }
        }
        return count == 0 ? 0.0 : total / count;
    }

    /**
     * Computes the local clustering coefficient for a single node.
     *
     * @param network the network
     * @param node    node index
     * @return local clustering coefficient in [0,1], or -1.0 if degree < 2
     */
    public static double localClusteringCoefficient(Network network, int node) {
        Set<Integer> nb = network.neighbours(node);
        int k = nb.size();
        if (k < 2) return -1.0;

        long maxPossible = (long) k * (k - 1) / 2;
        long actual      = 0;
        Integer[] arr    = nb.toArray(new Integer[0]);
        for (int a = 0; a < arr.length; a++)
            for (int b = a + 1; b < arr.length; b++)
                if (network.hasEdge(arr[a], arr[b])) actual++;

        return (double) actual / maxPossible;
    }

    // -------------------------------------------------------------------------
    // Average shortest path length
    // -------------------------------------------------------------------------

    /**
     * Computes the average shortest path length via BFS from every node.
     * Runs in O(n(n+m)) time.
     *
     * @param network the network to measure
     * @return average shortest path length, or POSITIVE_INFINITY if no
     *         connected pairs exist
     */
    public static double averageShortestPathLength(Network network) {
        Objects.requireNonNull(network);
        long totalLength    = 0;
        long reachablePairs = 0;
        int  n              = network.getNodeCount();
        long totalPairs     = (long) n * (n - 1) / 2;
        for (int src = 0; src < n; src++) {
            int[] dist = bfsDistances(network, src);
            for (int tgt = src + 1; tgt < n; tgt++) {
                if (dist[tgt] >= 0) { totalLength += dist[tgt]; reachablePairs++; }
            }
        }
        // Return POSITIVE_INFINITY if any pair is unreachable (disconnected graph),
        // matching the documented contract.
        if (reachablePairs < totalPairs) return Double.POSITIVE_INFINITY;
        return reachablePairs == 0
            ? Double.POSITIVE_INFINITY
            : (double) totalLength / reachablePairs;
    }

    /**
     * BFS from source; returns distance array where -1 means unreachable.
     *
     * @param network the network
     * @param source  source node
     * @return distance array
     */
    static int[] bfsDistances(Network network, int source) {
        int   n    = network.getNodeCount();
        int[] dist = new int[n];
        Arrays.fill(dist, -1);
        dist[source] = 0;
        Queue<Integer> q = new ArrayDeque<>();
        q.add(source);
        while (!q.isEmpty()) {
            int cur = q.poll();
            for (int nb : network.neighbours(cur)) {
                if (dist[nb] == -1) { dist[nb] = dist[cur] + 1; q.add(nb); }
            }
        }
        return dist;
    }

    // -------------------------------------------------------------------------
    // Snapshot
    // -------------------------------------------------------------------------

    /**
     * Computes both metrics together and returns an immutable snapshot.
     *
     * @param network the network to measure
     * @return MetricsSnapshot containing C and L
     */
    public static MetricsSnapshot snapshot(Network network) {
        return new MetricsSnapshot(
            averageClusteringCoefficient(network),
            averageShortestPathLength(network));
    }

    /**
     * Immutable pair of (clustering coefficient C, average path length L).
     */
    public static final class MetricsSnapshot {
        private final double clusteringCoefficient;
        private final double avgPathLength;

        public MetricsSnapshot(double c, double l) {
            this.clusteringCoefficient = c;
            this.avgPathLength         = l;
        }

        /** @return average clustering coefficient C in [0, 1] */
        public double getClusteringCoefficient() { return clusteringCoefficient; }

        /** @return average shortest path length L */
        public double getAvgPathLength()         { return avgPathLength; }

        @Override
        public String toString() {
            return String.format("Metrics{C=%.4f, L=%.4f}",
                clusteringCoefficient, avgPathLength);
        }
    }
}

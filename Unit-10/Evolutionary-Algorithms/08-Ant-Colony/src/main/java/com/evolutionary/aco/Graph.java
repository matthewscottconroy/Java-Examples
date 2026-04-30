package com.evolutionary.aco;

/**
 * Weighted undirected graph represented as an adjacency matrix.
 *
 * <p>Edge weight represents cost (distance, latency, etc.). A weight of 0
 * means no direct edge. All edges are symmetric: weight[i][j] == weight[j][i].
 */
public class Graph {

    private final int n;
    private final double[][] weight;

    public Graph(double[][] weight) {
        this.n = weight.length;
        this.weight = weight;
    }

    public int size() { return n; }

    public double weight(int i, int j) { return weight[i][j]; }

    public boolean hasEdge(int i, int j) { return weight[i][j] > 0; }

    /** Convenience constructor from integer matrix. */
    public static Graph of(int[][] w) {
        int n = w.length;
        double[][] d = new double[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                d[i][j] = w[i][j];
        return new Graph(d);
    }
}

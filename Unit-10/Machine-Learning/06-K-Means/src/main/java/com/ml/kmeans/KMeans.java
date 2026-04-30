package com.ml.kmeans;

import java.util.*;

/**
 * K-Means clustering (Lloyd's algorithm).
 *
 * <p>K-Means partitions m samples into k clusters by alternating between
 * two steps until convergence:
 * <ol>
 *   <li><b>Assignment</b> — assign each sample to the nearest centroid
 *       (by Euclidean distance)</li>
 *   <li><b>Update</b> — recompute each centroid as the mean of its assigned samples</li>
 * </ol>
 *
 * <p>Convergence is guaranteed (assignments and cost are monotone decreasing),
 * but K-Means may find a local optimum rather than the global optimum.
 * The K-Means++ initialisation strategy (used here) places initial centroids
 * far apart, dramatically reducing the chance of a poor local solution.
 *
 * <p>Key hyperparameter: k (number of clusters). Chosen via the elbow method —
 * plot within-cluster sum-of-squares (WCSS) against k and look for the point
 * where improvement flattens.
 */
public class KMeans {

    private final int k;
    private final int maxIter;
    private final long seed;

    private double[][] centroids;
    private int[] assignments;

    public KMeans(int k, int maxIter, long seed) {
        this.k = k;
        this.maxIter = maxIter;
        this.seed = seed;
    }

    public void fit(double[][] X) {
        Random rng = new Random(seed);
        centroids = initPlusPlus(X, rng);
        assignments = new int[X.length];

        for (int iter = 0; iter < maxIter; iter++) {
            boolean changed = assign(X);
            updateCentroids(X);
            if (!changed) break;
        }
    }

    /** Returns the cluster index (0-based) for a new sample. */
    public int predict(double[] x) {
        int best = 0;
        double bestDist = Double.MAX_VALUE;
        for (int c = 0; c < k; c++) {
            double d = euclidean(x, centroids[c]);
            if (d < bestDist) { bestDist = d; best = c; }
        }
        return best;
    }

    /** Returns cluster index for each sample in X. */
    public int[] predict(double[][] X) {
        int[] out = new int[X.length];
        for (int i = 0; i < X.length; i++) out[i] = predict(X[i]);
        return out;
    }

    /** Within-cluster sum of squared distances (WCSS). Lower = tighter clusters. */
    public double wcss(double[][] X) {
        double sum = 0;
        for (int i = 0; i < X.length; i++) {
            double d = euclidean(X[i], centroids[assignments[i]]);
            sum += d * d;
        }
        return sum;
    }

    public double[][] centroids() { return centroids; }
    public int[] assignments() { return assignments.clone(); }

    // -----------------------------------------------------------------
    // K-Means++ initialisation
    // -----------------------------------------------------------------

    private double[][] initPlusPlus(double[][] X, Random rng) {
        double[][] c = new double[k][];
        c[0] = X[rng.nextInt(X.length)].clone();

        for (int ci = 1; ci < k; ci++) {
            double[] dist2 = new double[X.length];
            double total = 0;
            for (int i = 0; i < X.length; i++) {
                double minD = Double.MAX_VALUE;
                for (int j = 0; j < ci; j++) {
                    double d = euclidean(X[i], c[j]);
                    if (d < minD) minD = d;
                }
                dist2[i] = minD * minD;
                total += dist2[i];
            }
            double r = rng.nextDouble() * total;
            double cumulative = 0;
            for (int i = 0; i < X.length; i++) {
                cumulative += dist2[i];
                if (r <= cumulative) { c[ci] = X[i].clone(); break; }
            }
            if (c[ci] == null) c[ci] = X[X.length - 1].clone();
        }
        return c;
    }

    // -----------------------------------------------------------------

    private boolean assign(double[][] X) {
        boolean changed = false;
        for (int i = 0; i < X.length; i++) {
            int best = 0; double bestDist = Double.MAX_VALUE;
            for (int c = 0; c < k; c++) {
                double d = euclidean(X[i], centroids[c]);
                if (d < bestDist) { bestDist = d; best = c; }
            }
            if (assignments[i] != best) { assignments[i] = best; changed = true; }
        }
        return changed;
    }

    private void updateCentroids(double[][] X) {
        int dims = X[0].length;
        double[][] sums = new double[k][dims];
        int[] counts = new int[k];
        for (int i = 0; i < X.length; i++) {
            int c = assignments[i];
            counts[c]++;
            for (int d = 0; d < dims; d++) sums[c][d] += X[i][d];
        }
        for (int c = 0; c < k; c++) {
            if (counts[c] > 0) {
                for (int d = 0; d < dims; d++) centroids[c][d] = sums[c][d] / counts[c];
            }
        }
    }

    static double euclidean(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) sum += (a[i] - b[i]) * (a[i] - b[i]);
        return Math.sqrt(sum);
    }
}

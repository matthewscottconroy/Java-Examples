package com.ml.knn;

import java.util.*;

/**
 * K-Nearest Neighbours classifier and regressor.
 *
 * <p>KNN is a non-parametric, instance-based learning algorithm. It stores
 * the entire training set and defers all computation to prediction time.
 * For a query point q, it finds the k training samples closest by Euclidean
 * distance and returns the majority class (classification) or mean value
 * (regression).
 *
 * <p>Time complexity: O(m) per prediction (brute force search).
 * Space complexity:  O(m) to store training set.
 *
 * <p>Key hyperparameter: k
 * <ul>
 *   <li>Small k (e.g. 1) — low bias, high variance; sensitive to noise</li>
 *   <li>Large k — high bias, low variance; over-smoothed boundaries</li>
 * </ul>
 */
public class KNN {

    private final int k;
    private double[][] trainX;
    private int[]    trainY;

    public KNN(int k) {
        this.k = k;
    }

    public void fit(double[][] X, int[] y) {
        this.trainX = X;
        this.trainY = y;
    }

    /** Returns predicted class label for x. */
    public int predict(double[] x) {
        int[] neighbours = kNearestIndices(x);
        return majorityVote(neighbours);
    }

    /** Returns predicted class labels for each row of X. */
    public int[] predict(double[][] X) {
        int[] preds = new int[X.length];
        for (int i = 0; i < X.length; i++) preds[i] = predict(X[i]);
        return preds;
    }

    public double accuracy(double[][] X, int[] y) {
        int[] preds = predict(X);
        int correct = 0;
        for (int i = 0; i < y.length; i++) if (preds[i] == y[i]) correct++;
        return (double) correct / y.length;
    }

    /** Returns distances to each training point, sorted ascending (for inspection). */
    public double[] distances(double[] x) {
        double[] dists = new double[trainX.length];
        for (int i = 0; i < trainX.length; i++) dists[i] = euclidean(x, trainX[i]);
        Arrays.sort(dists);
        return dists;
    }

    // ---------------------------------------------------------------

    int[] kNearestIndices(double[] x) {
        // Priority queue: max-heap of size k (furthest at top — evict if closer found)
        PriorityQueue<int[]> heap = new PriorityQueue<>(
            k, (a, b) -> Double.compare(b[1], a[1]));   // b[1] vs a[1] = max-heap on distance

        for (int i = 0; i < trainX.length; i++) {
            double d = euclidean(x, trainX[i]);
            int dist100 = (int)(d * 1e9);   // encode as int for heap (won't lose ordering)

            if (heap.size() < k) {
                heap.offer(new int[]{ i, dist100 });
            } else if (dist100 < heap.peek()[1]) {
                heap.poll();
                heap.offer(new int[]{ i, dist100 });
            }
        }

        return heap.stream().mapToInt(e -> e[0]).toArray();
    }

    private int majorityVote(int[] indices) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (int idx : indices) counts.merge(trainY[idx], 1, Integer::sum);
        return counts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElseThrow();
    }

    static double euclidean(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) sum += (a[i] - b[i]) * (a[i] - b[i]);
        return Math.sqrt(sum);
    }
}

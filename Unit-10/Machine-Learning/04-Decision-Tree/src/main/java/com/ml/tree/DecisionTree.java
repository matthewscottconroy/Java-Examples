package com.ml.tree;

import java.util.*;

/**
 * Decision tree classifier using recursive binary splitting on continuous features.
 *
 * <p>At each node the algorithm exhaustively searches every (feature, threshold)
 * split and picks the one that minimises the weighted Gini impurity of the
 * resulting child nodes:
 * <pre>
 *   Gini(S) = 1 - Σ pₖ²
 *   Gain    = Gini(parent) - [|Sₗ|/|S| × Gini(Sₗ) + |Sᵣ|/|S| × Gini(Sᵣ)]
 * </pre>
 *
 * <p>Recursion stops when:
 * <ul>
 *   <li>The node is pure (all samples same class)</li>
 *   <li>Max tree depth has been reached</li>
 *   <li>A split would produce a child with fewer than minSamplesSplit samples</li>
 * </ul>
 */
public class DecisionTree {

    private Node root;
    private final int maxDepth;
    private final int minSamplesSplit;

    public DecisionTree(int maxDepth, int minSamplesSplit) {
        this.maxDepth = maxDepth;
        this.minSamplesSplit = minSamplesSplit;
    }

    // -----------------------------------------------------------------
    // Tree nodes
    // -----------------------------------------------------------------

    sealed interface Node permits DecisionTree.Leaf, DecisionTree.Split {}

    record Leaf(int label) implements Node {}

    record Split(int feature, double threshold, Node left, Node right) implements Node {}

    // -----------------------------------------------------------------
    // Training
    // -----------------------------------------------------------------

    public void fit(double[][] X, int[] y) {
        root = buildTree(X, y, 0);
    }

    private Node buildTree(double[][] X, int[] y, int depth) {
        if (isPure(y) || depth >= maxDepth || X.length < minSamplesSplit) {
            return new Leaf(majorityClass(y));
        }

        BestSplit best = findBestSplit(X, y);
        if (best == null) return new Leaf(majorityClass(y));

        int[][] partition = partition(X, y, best.feature(), best.threshold());
        double[][] leftX  = subsetX(X, partition[0]);
        int[]      leftY  = subsetY(y, partition[0]);
        double[][] rightX = subsetX(X, partition[1]);
        int[]      rightY = subsetY(y, partition[1]);

        if (leftX.length == 0 || rightX.length == 0) return new Leaf(majorityClass(y));

        return new Split(best.feature(), best.threshold(),
                         buildTree(leftX, leftY, depth + 1),
                         buildTree(rightX, rightY, depth + 1));
    }

    // -----------------------------------------------------------------
    // Prediction
    // -----------------------------------------------------------------

    public int predict(double[] x) {
        Node node = root;
        while (node instanceof Split s) {
            node = x[s.feature()] <= s.threshold() ? s.left() : s.right();
        }
        return ((Leaf) node).label();
    }

    public int[] predict(double[][] X) {
        int[] out = new int[X.length];
        for (int i = 0; i < X.length; i++) out[i] = predict(X[i]);
        return out;
    }

    public double accuracy(double[][] X, int[] y) {
        int[] p = predict(X); int c = 0;
        for (int i = 0; i < y.length; i++) if (p[i] == y[i]) c++;
        return (double) c / y.length;
    }

    // -----------------------------------------------------------------
    // Split search
    // -----------------------------------------------------------------

    private record BestSplit(int feature, double threshold, double gain) {}

    private BestSplit findBestSplit(double[][] X, int[] y) {
        double parentGini = gini(y);
        int m = X.length, n = X[0].length;
        BestSplit best = null;

        for (int f = 0; f < n; f++) {
            // Collect unique thresholds (midpoints between sorted values)
            double[] vals = new double[m];
            for (int i = 0; i < m; i++) vals[i] = X[i][f];
            Arrays.sort(vals);

            for (int t = 0; t < m - 1; t++) {
                if (vals[t] == vals[t + 1]) continue;
                double threshold = (vals[t] + vals[t + 1]) / 2.0;
                int[][] partition = partition(X, y, f, threshold);
                if (partition[0].length == 0 || partition[1].length == 0) continue;

                double gain = parentGini
                    - ((double) partition[0].length / m) * gini(subsetY(y, partition[0]))
                    - ((double) partition[1].length / m) * gini(subsetY(y, partition[1]));

                if (best == null || gain > best.gain()) {
                    best = new BestSplit(f, threshold, gain);
                }
            }
        }
        return best;
    }

    // -----------------------------------------------------------------
    // Gini impurity
    // -----------------------------------------------------------------

    static double gini(int[] y) {
        if (y.length == 0) return 0;
        Map<Integer, Integer> counts = new HashMap<>();
        for (int v : y) counts.merge(v, 1, Integer::sum);
        double impurity = 1.0;
        for (int c : counts.values()) {
            double p = (double) c / y.length;
            impurity -= p * p;
        }
        return impurity;
    }

    // -----------------------------------------------------------------
    // Utilities
    // -----------------------------------------------------------------

    private static int[][] partition(double[][] X, int[] y, int feature, double threshold) {
        List<Integer> left = new ArrayList<>(), right = new ArrayList<>();
        for (int i = 0; i < X.length; i++) {
            (X[i][feature] <= threshold ? left : right).add(i);
        }
        return new int[][]{ left.stream().mapToInt(Integer::intValue).toArray(),
                            right.stream().mapToInt(Integer::intValue).toArray() };
    }

    private static double[][] subsetX(double[][] X, int[] indices) {
        double[][] out = new double[indices.length][];
        for (int i = 0; i < indices.length; i++) out[i] = X[indices[i]];
        return out;
    }

    private static int[] subsetY(int[] y, int[] indices) {
        int[] out = new int[indices.length];
        for (int i = 0; i < indices.length; i++) out[i] = y[indices[i]];
        return out;
    }

    private static boolean isPure(int[] y) {
        if (y.length == 0) return true;
        int first = y[0];
        for (int v : y) if (v != first) return false;
        return true;
    }

    static int majorityClass(int[] y) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (int v : y) counts.merge(v, 1, Integer::sum);
        return counts.entrySet().stream().max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse(0);
    }
}

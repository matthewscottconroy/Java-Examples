package com.ml.forest;

import java.util.*;

/**
 * A single decision tree within a Random Forest.
 *
 * <p>Like a standard decision tree, but at each split only {@code maxFeatures}
 * randomly chosen features are considered. This decorrelates the trees in the
 * ensemble so their errors do not all point the same direction.
 */
class RandomTree {

    private Node root;
    private final int maxDepth;
    private final int minSamplesSplit;
    private final int maxFeatures;
    private final Random rng;

    sealed interface Node permits RandomTree.Leaf, RandomTree.Split {}
    record Leaf(int label) implements Node {}
    record Split(int feature, double threshold, double gainContrib,
                 Node left, Node right) implements Node {}

    RandomTree(int maxDepth, int minSamplesSplit, int maxFeatures, Random rng) {
        this.maxDepth = maxDepth;
        this.minSamplesSplit = minSamplesSplit;
        this.maxFeatures = maxFeatures;
        this.rng = rng;
    }

    void fit(double[][] X, int[] y) {
        root = buildTree(X, y, 0);
    }

    int predict(double[] x) {
        Node node = root;
        while (node instanceof Split s) {
            node = x[s.feature()] <= s.threshold() ? s.left() : s.right();
        }
        return ((Leaf) node).label();
    }

    void accumulateImportance(double[] importance) {
        accumulateNode(root, importance);
    }

    private void accumulateNode(Node node, double[] importance) {
        if (node instanceof Split s) {
            importance[s.feature()] += s.gainContrib();
            accumulateNode(s.left(), importance);
            accumulateNode(s.right(), importance);
        }
    }

    private Node buildTree(double[][] X, int[] y, int depth) {
        if (isPure(y) || depth >= maxDepth || X.length < minSamplesSplit) {
            return new Leaf(majorityClass(y));
        }

        BestSplit best = findBestSplit(X, y);
        if (best == null) return new Leaf(majorityClass(y));

        int[][] part = partition(X, y, best.feature, best.threshold);
        if (part[0].length == 0 || part[1].length == 0) return new Leaf(majorityClass(y));

        return new Split(
            best.feature, best.threshold, best.gain,
            buildTree(subsetX(X, part[0]), subsetY(y, part[0]), depth + 1),
            buildTree(subsetX(X, part[1]), subsetY(y, part[1]), depth + 1)
        );
    }

    private record BestSplit(int feature, double threshold, double gain) {}

    private BestSplit findBestSplit(double[][] X, int[] y) {
        int n = X[0].length;
        int m = X.length;
        double parentGini = gini(y);

        // Randomly subsample features
        int[] features = randomSubset(n);
        BestSplit best = null;

        for (int f : features) {
            double[] vals = new double[m];
            for (int i = 0; i < m; i++) vals[i] = X[i][f];
            Arrays.sort(vals);

            for (int t = 0; t < m - 1; t++) {
                if (vals[t] == vals[t + 1]) continue;
                double threshold = (vals[t] + vals[t + 1]) / 2.0;
                int[][] part = partition(X, y, f, threshold);
                if (part[0].length == 0 || part[1].length == 0) continue;

                double gain = parentGini
                    - ((double) part[0].length / m) * gini(subsetY(y, part[0]))
                    - ((double) part[1].length / m) * gini(subsetY(y, part[1]));

                if (best == null || gain > best.gain) best = new BestSplit(f, threshold, gain);
            }
        }
        return best;
    }

    private int[] randomSubset(int n) {
        int[] all = new int[n];
        for (int i = 0; i < n; i++) all[i] = i;
        // Fisher-Yates partial shuffle
        for (int i = n - 1; i > n - maxFeatures - 1 && i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = all[i]; all[i] = all[j]; all[j] = tmp;
        }
        return Arrays.copyOfRange(all, n - maxFeatures, n);
    }

    private static double gini(int[] y) {
        if (y.length == 0) return 0;
        Map<Integer, Integer> counts = new HashMap<>();
        for (int v : y) counts.merge(v, 1, Integer::sum);
        double impurity = 1.0;
        for (int c : counts.values()) { double p = (double) c / y.length; impurity -= p * p; }
        return impurity;
    }

    private static int majorityClass(int[] y) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (int v : y) counts.merge(v, 1, Integer::sum);
        return counts.entrySet().stream().max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse(0);
    }

    private static boolean isPure(int[] y) {
        int first = y[0];
        for (int v : y) if (v != first) return false;
        return true;
    }

    private static int[][] partition(double[][] X, int[] y, int f, double threshold) {
        List<Integer> left = new ArrayList<>(), right = new ArrayList<>();
        for (int i = 0; i < X.length; i++) (X[i][f] <= threshold ? left : right).add(i);
        return new int[][]{ left.stream().mapToInt(Integer::intValue).toArray(),
                            right.stream().mapToInt(Integer::intValue).toArray() };
    }

    private static double[][] subsetX(double[][] X, int[] idx) {
        double[][] out = new double[idx.length][];
        for (int i = 0; i < idx.length; i++) out[i] = X[idx[i]];
        return out;
    }

    private static int[] subsetY(int[] y, int[] idx) {
        int[] out = new int[idx.length];
        for (int i = 0; i < idx.length; i++) out[i] = y[idx[i]];
        return out;
    }
}

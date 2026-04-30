package com.ml.forest;

import java.util.*;

/**
 * Random Forest classifier — an ensemble of decision trees trained with
 * bootstrap aggregation (bagging) and random feature subsampling.
 *
 * <p>Two sources of randomness prevent the trees from being identical:
 * <ol>
 *   <li><b>Bootstrap sampling</b> — each tree trains on a random sample of
 *       the training set drawn with replacement (≈63% unique samples)</li>
 *   <li><b>Feature subsampling</b> — at each split, only √n features are
 *       considered as split candidates rather than all n features</li>
 * </ol>
 *
 * <p>Prediction is by majority vote across all trees. Because each tree is
 * trained on a different bootstrap sample, the ensemble is less prone to
 * overfitting than a single deep decision tree.
 *
 * <p>Feature importance is estimated as the total Gini gain produced by
 * each feature across all splits in all trees, normalised to sum to 1.
 */
public class RandomForest {

    private final int numTrees;
    private final int maxDepth;
    private final int minSamplesSplit;
    private final long seed;

    private List<RandomTree> trees;
    private int numFeatures;

    public RandomForest(int numTrees, int maxDepth, int minSamplesSplit, long seed) {
        this.numTrees = numTrees;
        this.maxDepth = maxDepth;
        this.minSamplesSplit = minSamplesSplit;
        this.seed = seed;
    }

    public void fit(double[][] X, int[] y) {
        numFeatures = X[0].length;
        trees = new ArrayList<>(numTrees);
        Random rng = new Random(seed);

        for (int t = 0; t < numTrees; t++) {
            // Bootstrap sample
            int m = X.length;
            double[][] bootX = new double[m][];
            int[]      bootY = new int[m];
            for (int i = 0; i < m; i++) {
                int idx = rng.nextInt(m);
                bootX[i] = X[idx];
                bootY[i] = y[idx];
            }
            RandomTree tree = new RandomTree(maxDepth, minSamplesSplit,
                                             (int) Math.max(1, Math.sqrt(numFeatures)),
                                             new Random(rng.nextLong()));
            tree.fit(bootX, bootY);
            trees.add(tree);
        }
    }

    public int predict(double[] x) {
        Map<Integer, Integer> votes = new HashMap<>();
        for (RandomTree tree : trees) votes.merge(tree.predict(x), 1, Integer::sum);
        return votes.entrySet().stream().max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElseThrow();
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

    /**
     * Returns feature importances (Gini-based), normalised to sum to 1.
     * Higher = more important.
     */
    public double[] featureImportances() {
        double[] importance = new double[numFeatures];
        for (RandomTree tree : trees) tree.accumulateImportance(importance);
        double total = Arrays.stream(importance).sum();
        if (total > 0) for (int f = 0; f < numFeatures; f++) importance[f] /= total;
        return importance;
    }

    int treeCount() { return trees.size(); }
}

package com.ml.forest;

/**
 * Credit risk classifier.
 *
 * Classifies loan applicants as:
 *   0 = Low risk
 *   1 = Medium risk
 *   2 = High risk
 *
 * Features (all normalised to [0,1]):
 *   [0] credit_score_norm      — higher = better
 *   [1] income_norm            — higher = better
 *   [2] debt_to_income_norm    — higher = more debt (worse)
 *   [3] employment_years_norm  — higher = more stable
 *   [4] missed_payments_norm   — higher = more missed (worse)
 *
 * A single decision tree on this 5-feature problem would be prone to
 * overfitting. A Random Forest of 20 trees, each trained on a bootstrap
 * sample and using √5 ≈ 2 features per split, generalises much better.
 */
public class Main {

    static final String[] CLASS_NAMES = { "Low Risk", "Medium Risk", "High Risk" };
    static final String[] FEATURE_NAMES = { "Credit Score", "Income", "Debt Ratio", "Employment Yrs", "Missed Payments" };

    // [credit, income, debt_ratio, employment, missed_payments]
    static final double[][] X = {
        // Low risk (0)
        { 0.9, 0.8, 0.1, 0.9, 0.0 }, { 0.85, 0.9, 0.1, 0.8, 0.0 }, { 0.9, 0.7, 0.2, 0.9, 0.0 },
        { 0.8, 0.8, 0.1, 0.7, 0.0 }, { 0.9, 0.9, 0.1, 0.9, 0.0 }, { 0.85, 0.8, 0.2, 0.8, 0.0 },
        { 0.8, 0.7, 0.1, 0.9, 0.0 }, { 0.9, 0.8, 0.2, 0.8, 0.0 },
        // Medium risk (1)
        { 0.6, 0.5, 0.4, 0.5, 0.2 }, { 0.55, 0.6, 0.5, 0.4, 0.3 }, { 0.6, 0.5, 0.4, 0.6, 0.2 },
        { 0.5, 0.5, 0.5, 0.5, 0.3 }, { 0.6, 0.6, 0.4, 0.5, 0.2 }, { 0.55, 0.5, 0.5, 0.4, 0.2 },
        { 0.5, 0.6, 0.4, 0.6, 0.3 }, { 0.6, 0.5, 0.5, 0.5, 0.2 },
        // High risk (2)
        { 0.2, 0.2, 0.8, 0.1, 0.8 }, { 0.15, 0.3, 0.9, 0.2, 0.9 }, { 0.2, 0.2, 0.8, 0.1, 0.8 },
        { 0.3, 0.2, 0.9, 0.1, 0.7 }, { 0.2, 0.3, 0.8, 0.2, 0.8 }, { 0.1, 0.2, 0.9, 0.1, 0.9 },
        { 0.2, 0.1, 0.8, 0.2, 0.8 }, { 0.3, 0.2, 0.9, 0.1, 0.7 },
    };

    static final int[] Y = {
        0,0,0,0,0,0,0,0,
        1,1,1,1,1,1,1,1,
        2,2,2,2,2,2,2,2
    };

    public static void main(String[] args) {
        System.out.println("=== Random Forest — Credit Risk Classifier ===\n");

        // Compare single tree vs forest
        for (int numTrees : new int[]{ 1, 5, 20 }) {
            RandomForest rf = new RandomForest(numTrees, 5, 2, 42L);
            rf.fit(X, Y);
            System.out.printf("Trees=%2d  training accuracy: %.1f%%%n",
                numTrees, rf.accuracy(X, Y) * 100);
        }

        RandomForest rf = new RandomForest(20, 5, 2, 42L);
        rf.fit(X, Y);

        System.out.println("\nFeature importances:");
        double[] importance = rf.featureImportances();
        for (int f = 0; f < FEATURE_NAMES.length; f++) {
            System.out.printf("  %-18s  %.3f%n", FEATURE_NAMES[f], importance[f]);
        }

        System.out.println("\nSample predictions:");
        System.out.printf("  %-12s  %-14s%n", "Actual", "Predicted");
        System.out.println("  " + "-".repeat(30));
        int[] idxs = { 0, 8, 16 };
        for (int idx : idxs) {
            System.out.printf("  %-12s  %-14s%n",
                CLASS_NAMES[Y[idx]], CLASS_NAMES[rf.predict(X[idx])]);
        }

        System.out.println("\nNew applicant (credit=0.7, income=0.6, debt=0.3, employment=0.6, missed=0.1):");
        int pred = rf.predict(new double[]{ 0.7, 0.6, 0.3, 0.6, 0.1 });
        System.out.printf("  Risk category: %s%n", CLASS_NAMES[pred]);
    }
}

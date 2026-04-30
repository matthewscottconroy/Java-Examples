package com.ml.tree;

/**
 * Loan application approver.
 *
 * Features: [credit_score_norm, income_norm, debt_ratio, employment_years_norm]
 * Labels:   0 = Denied, 1 = Approved
 *
 * A decision tree learns interpretable rules about which applications
 * to approve. Unlike a linear model, it can capture thresholds and
 * interactions without feature engineering.
 */
public class Main {

    static final String[] CLASS_NAMES = { "Denied", "Approved" };

    // [credit_score_norm, income_norm, debt_ratio, employment_years_norm]
    static final double[][] X = {
        // Approved (1) — good credit, reasonable income/debt
        { 0.8, 0.7, 0.2, 0.6 }, { 0.9, 0.8, 0.1, 0.8 }, { 0.7, 0.6, 0.3, 0.5 },
        { 0.8, 0.9, 0.2, 0.7 }, { 0.9, 0.7, 0.1, 0.9 }, { 0.7, 0.8, 0.3, 0.6 },
        { 0.6, 0.9, 0.2, 0.8 }, { 0.8, 0.6, 0.2, 0.7 }, { 0.9, 0.9, 0.1, 0.8 },
        { 0.7, 0.7, 0.3, 0.7 },
        // Denied (0) — poor credit or high debt
        { 0.3, 0.4, 0.7, 0.2 }, { 0.2, 0.3, 0.8, 0.1 }, { 0.4, 0.3, 0.9, 0.3 },
        { 0.3, 0.5, 0.8, 0.2 }, { 0.2, 0.4, 0.7, 0.1 }, { 0.4, 0.2, 0.9, 0.2 },
        { 0.3, 0.3, 0.8, 0.3 }, { 0.2, 0.2, 0.9, 0.1 }, { 0.4, 0.4, 0.7, 0.2 },
        { 0.3, 0.3, 0.8, 0.3 },
    };

    static final int[] Y = {
        1,1,1,1,1,1,1,1,1,1,
        0,0,0,0,0,0,0,0,0,0
    };

    public static void main(String[] args) {
        System.out.println("=== Decision Tree — Loan Approver ===\n");

        for (int depth : new int[]{ 1, 2, 4 }) {
            DecisionTree dt = new DecisionTree(depth, 2);
            dt.fit(X, Y);
            System.out.printf("Max depth=%d  training accuracy: %.1f%%%n",
                depth, dt.accuracy(X, Y) * 100);
        }

        DecisionTree dt = new DecisionTree(4, 2);
        dt.fit(X, Y);

        System.out.println("\nSample predictions:");
        System.out.printf("  %-45s  %-9s  %-9s%n", "Applicant", "Actual", "Predicted");
        System.out.println("  " + "-".repeat(66));
        int[] idxs = { 0, 5, 10, 15 };
        for (int idx : idxs) {
            System.out.printf("  credit=%.1f inc=%.1f debt=%.1f emp=%.1f       %-9s  %-9s%n",
                X[idx][0], X[idx][1], X[idx][2], X[idx][3],
                CLASS_NAMES[Y[idx]], CLASS_NAMES[dt.predict(X[idx])]);
        }

        System.out.println("\nNew applicant (credit=0.75, income=0.60, debt=0.25, employment=0.5):");
        int pred = dt.predict(new double[]{ 0.75, 0.60, 0.25, 0.5 });
        System.out.printf("  Decision: %s%n", CLASS_NAMES[pred]);
    }
}

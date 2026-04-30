package com.ml.tree;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DecisionTreeTest {

    // XOR-like dataset — needs depth ≥ 2 to classify perfectly
    static final double[][] X_XOR = {
        {0.1, 0.1}, {0.9, 0.9},   // class 0
        {0.1, 0.9}, {0.9, 0.1},   // class 1
    };
    static final int[] Y_XOR = { 0, 0, 1, 1 };

    // Linearly separable dataset
    static final double[][] X_SEP = {
        {0.1}, {0.2}, {0.3},   // class 0
        {0.7}, {0.8}, {0.9},   // class 1
    };
    static final int[] Y_SEP = { 0, 0, 0, 1, 1, 1 };

    @Test
    @DisplayName("Gini impurity: pure node = 0")
    void gini_pure() {
        assertEquals(0.0, DecisionTree.gini(new int[]{1, 1, 1}), 1e-9);
        assertEquals(0.0, DecisionTree.gini(new int[]{0, 0, 0}), 1e-9);
    }

    @Test
    @DisplayName("Gini impurity: perfectly mixed 2-class = 0.5")
    void gini_maxImpurity() {
        assertEquals(0.5, DecisionTree.gini(new int[]{0, 1, 0, 1}), 1e-9);
    }

    @Test
    @DisplayName("Gini impurity: empty array = 0")
    void gini_empty() {
        assertEquals(0.0, DecisionTree.gini(new int[]{}), 1e-9);
    }

    @Test
    @DisplayName("majorityClass returns most frequent label")
    void majorityClass() {
        assertEquals(1, DecisionTree.majorityClass(new int[]{1, 1, 0}));
        assertEquals(0, DecisionTree.majorityClass(new int[]{0, 0, 0}));
    }

    @Test
    @DisplayName("Depth-1 tree (stump) splits on best single feature")
    void depth1_stump() {
        DecisionTree dt = new DecisionTree(1, 1);
        dt.fit(X_SEP, Y_SEP);
        // Should separate class 0 (≤0.5) from class 1 (>0.5) perfectly
        assertEquals(1.0, dt.accuracy(X_SEP, Y_SEP));
    }

    @Test
    @DisplayName("Deep tree fits training data perfectly")
    void deepTree_perfectFit() {
        DecisionTree dt = new DecisionTree(10, 1);
        dt.fit(X_XOR, Y_XOR);
        assertEquals(1.0, dt.accuracy(X_XOR, Y_XOR));
    }

    @Test
    @DisplayName("Depth-0 tree predicts majority class for all inputs")
    void depth0_alwaysMajority() {
        DecisionTree dt = new DecisionTree(0, 1);
        dt.fit(X_SEP, Y_SEP);
        // With 3 class-0 and 3 class-1, either can be the majority; just check consistency
        int pred = dt.predict(new double[]{0.5});
        assertTrue(pred == 0 || pred == 1);
        // All predictions must be the same (always majority)
        for (double[] x : X_SEP) assertEquals(pred, dt.predict(x));
    }

    @Test
    @DisplayName("Batch predict returns one label per sample")
    void batchPredict_length() {
        DecisionTree dt = new DecisionTree(3, 1);
        dt.fit(X_SEP, Y_SEP);
        int[] preds = dt.predict(X_SEP);
        assertEquals(X_SEP.length, preds.length);
    }

    @Test
    @DisplayName("Loan dataset: depth-4 tree achieves 100% training accuracy")
    void loanDataset_perfectFit() {
        DecisionTree dt = new DecisionTree(4, 2);
        dt.fit(Main.X, Main.Y);
        assertEquals(1.0, dt.accuracy(Main.X, Main.Y));
    }

    @Test
    @DisplayName("High-credit applicant is approved")
    void highCredit_approved() {
        DecisionTree dt = new DecisionTree(4, 2);
        dt.fit(Main.X, Main.Y);
        // Strong applicant: high credit, good income, low debt
        assertEquals(1, dt.predict(new double[]{ 0.85, 0.75, 0.15, 0.7 }));
    }

    @Test
    @DisplayName("Poor-credit applicant is denied")
    void poorCredit_denied() {
        DecisionTree dt = new DecisionTree(4, 2);
        dt.fit(Main.X, Main.Y);
        // Weak applicant: low credit, low income, high debt
        assertEquals(0, dt.predict(new double[]{ 0.25, 0.30, 0.85, 0.1 }));
    }

    @Test
    @DisplayName("3-class dataset: tree learns all three classes")
    void threeClass_allClassesLearned() {
        double[][] X = {
            {0.1}, {0.2},   // class 0
            {0.5}, {0.6},   // class 1
            {0.8}, {0.9},   // class 2
        };
        int[] y = { 0, 0, 1, 1, 2, 2 };
        DecisionTree dt = new DecisionTree(4, 1);
        dt.fit(X, y);
        assertEquals(0, dt.predict(new double[]{0.15}));
        assertEquals(1, dt.predict(new double[]{0.55}));
        assertEquals(2, dt.predict(new double[]{0.85}));
    }
}

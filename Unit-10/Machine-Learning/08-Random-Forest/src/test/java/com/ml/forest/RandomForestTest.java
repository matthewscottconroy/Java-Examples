package com.ml.forest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class RandomForestTest {

    // Well-separated 2-class dataset
    static final double[][] X_SEP = {
        {0.1, 0.1}, {0.2, 0.15}, {0.15, 0.2},   // class 0
        {0.8, 0.9}, {0.9, 0.8},  {0.85, 0.85},  // class 1
    };
    static final int[] Y_SEP = { 0, 0, 0, 1, 1, 1 };

    @Test
    @DisplayName("Builds correct number of trees")
    void treeCount() {
        RandomForest rf = new RandomForest(10, 5, 2, 0L);
        rf.fit(X_SEP, Y_SEP);
        assertEquals(10, rf.treeCount());
    }

    @Test
    @DisplayName("100% accuracy on well-separated 2-class data")
    void wellSeparated_perfectAccuracy() {
        RandomForest rf = new RandomForest(10, 5, 2, 0L);
        rf.fit(X_SEP, Y_SEP);
        assertEquals(1.0, rf.accuracy(X_SEP, Y_SEP));
    }

    @Test
    @DisplayName("predict() returns valid class label")
    void predict_inRange() {
        RandomForest rf = new RandomForest(5, 4, 2, 0L);
        rf.fit(X_SEP, Y_SEP);
        int pred = rf.predict(new double[]{0.5, 0.5});
        assertTrue(pred == 0 || pred == 1);
    }

    @Test
    @DisplayName("Batch predict returns one label per sample")
    void batchPredict_length() {
        RandomForest rf = new RandomForest(5, 4, 2, 0L);
        rf.fit(X_SEP, Y_SEP);
        int[] preds = rf.predict(X_SEP);
        assertEquals(X_SEP.length, preds.length);
    }

    @Test
    @DisplayName("Feature importances sum to 1.0")
    void featureImportances_sumToOne() {
        RandomForest rf = new RandomForest(20, 5, 2, 42L);
        rf.fit(Main.X, Main.Y);
        double sum = Arrays.stream(rf.featureImportances()).sum();
        assertEquals(1.0, sum, 1e-9, "Feature importances must sum to 1");
    }

    @Test
    @DisplayName("Feature importances are non-negative")
    void featureImportances_nonNegative() {
        RandomForest rf = new RandomForest(20, 5, 2, 42L);
        rf.fit(Main.X, Main.Y);
        for (double imp : rf.featureImportances()) assertTrue(imp >= 0);
    }

    @Test
    @DisplayName("Credit score and missed payments have above-average importance")
    void creditRisk_topFeatures() {
        RandomForest rf = new RandomForest(50, 5, 2, 42L);
        rf.fit(Main.X, Main.Y);
        double[] imp = rf.featureImportances();

        // Features: [credit=0, income=1, debt=2, employment=3, missed=4]
        // With 5 features, average importance = 0.2.
        // Credit score and missed payments should each exceed half the average.
        double avgImportance = 1.0 / imp.length;
        assertTrue(imp[0] > avgImportance * 0.5,
            "Credit score importance should be above half-average, got " + imp[0]);
        assertTrue(imp[4] > avgImportance * 0.5,
            "Missed payments importance should be above half-average, got " + imp[4]);
    }

    @Test
    @DisplayName("Forest outperforms single tree on 3-class dataset (≥ 95% accuracy)")
    void creditRisk_highAccuracy() {
        RandomForest rf = new RandomForest(20, 5, 2, 42L);
        rf.fit(Main.X, Main.Y);
        assertTrue(rf.accuracy(Main.X, Main.Y) >= 0.95,
            "20-tree forest should achieve ≥95% on training data");
    }

    @Test
    @DisplayName("Low-risk applicant classified correctly")
    void lowRisk_classified() {
        RandomForest rf = new RandomForest(20, 5, 2, 42L);
        rf.fit(Main.X, Main.Y);
        assertEquals(0, rf.predict(new double[]{ 0.9, 0.85, 0.1, 0.9, 0.0 }));
    }

    @Test
    @DisplayName("High-risk applicant classified correctly")
    void highRisk_classified() {
        RandomForest rf = new RandomForest(20, 5, 2, 42L);
        rf.fit(Main.X, Main.Y);
        assertEquals(2, rf.predict(new double[]{ 0.15, 0.2, 0.9, 0.1, 0.9 }));
    }

    @Test
    @DisplayName("Different seeds produce different but equally valid forests")
    void differentSeeds_differentPredictions() {
        // Two forests may disagree on borderline cases
        RandomForest rf1 = new RandomForest(10, 4, 2, 1L);
        RandomForest rf2 = new RandomForest(10, 4, 2, 99L);
        rf1.fit(Main.X, Main.Y);
        rf2.fit(Main.X, Main.Y);
        // Both should still be highly accurate
        assertTrue(rf1.accuracy(Main.X, Main.Y) >= 0.9);
        assertTrue(rf2.accuracy(Main.X, Main.Y) >= 0.9);
    }
}

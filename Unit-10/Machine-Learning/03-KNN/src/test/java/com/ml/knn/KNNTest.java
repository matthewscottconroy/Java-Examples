package com.ml.knn;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class KNNTest {

    // 2-class, 1D training set: class 0 near 0, class 1 near 1
    static final double[][] X2 = { {0.1}, {0.2}, {0.8}, {0.9} };
    static final int[]      Y2 = {   0,    0,    1,    1   };

    @Test
    @DisplayName("Euclidean distance: origin to (3,4) = 5")
    void euclidean_pythagorean() {
        assertEquals(5.0, KNN.euclidean(new double[]{0,0}, new double[]{3,4}), 1e-9);
    }

    @Test
    @DisplayName("Euclidean distance: same point = 0")
    void euclidean_samePoint() {
        assertEquals(0.0, KNN.euclidean(new double[]{1,2,3}, new double[]{1,2,3}), 1e-9);
    }

    @Test
    @DisplayName("k=1 correctly classifies points identical to training data")
    void k1_exactTrainingPoints() {
        KNN knn = new KNN(1);
        knn.fit(X2, Y2);
        assertEquals(0, knn.predict(new double[]{0.1}));
        assertEquals(1, knn.predict(new double[]{0.9}));
    }

    @Test
    @DisplayName("k=1 classifies midpoint by nearest neighbour")
    void k1_midpoint() {
        KNN knn = new KNN(1);
        knn.fit(X2, Y2);
        // 0.35 is closer to 0.2 (class 0) than to 0.8 (class 1)
        assertEquals(0, knn.predict(new double[]{0.35}));
    }

    @Test
    @DisplayName("k=3 uses majority vote to resolve ties")
    void k3_majorityVote() {
        double[][] X = { {0.1}, {0.2}, {0.3}, {0.8}, {0.9} };
        int[]      y = {   0,    0,    1,    1,    1   };
        KNN knn = new KNN(3);
        knn.fit(X, y);
        // Query 0.25: neighbours are 0.2(0), 0.3(1), 0.1(0) → majority class 0
        assertEquals(0, knn.predict(new double[]{0.25}));
    }

    @Test
    @DisplayName("Batch predict returns one label per sample")
    void batchPredict_correctLength() {
        KNN knn = new KNN(1);
        knn.fit(X2, Y2);
        int[] preds = knn.predict(X2);
        assertEquals(X2.length, preds.length);
    }

    @Test
    @DisplayName("100% training accuracy on well-separated 2D data")
    void wellSeparated_2D() {
        double[][] X = {
            {0.1, 0.1}, {0.2, 0.1},   // class 0
            {0.8, 0.9}, {0.9, 0.8},   // class 1
        };
        int[] y = { 0, 0, 1, 1 };
        KNN knn = new KNN(1);
        knn.fit(X, y);
        assertEquals(1.0, knn.accuracy(X, y));
    }

    @Test
    @DisplayName("distances() returns sorted ascending array")
    void distances_sorted() {
        KNN knn = new KNN(3);
        knn.fit(Main.X, Main.Y);
        double[] dists = knn.distances(new double[]{ 0.5, 0.5, 0.5, 0.5 });
        for (int i = 1; i < dists.length; i++) {
            assertTrue(dists[i] >= dists[i-1], "distances must be sorted ascending");
        }
    }

    @Test
    @DisplayName("Medical dataset: k=3 achieves ≥ 90% accuracy")
    void medicalDataset_accuracy() {
        KNN knn = new KNN(3);
        knn.fit(Main.X, Main.Y);
        assertTrue(knn.accuracy(Main.X, Main.Y) >= 0.9);
    }

    @Test
    @DisplayName("Healthy patient cluster predicts class 0")
    void healthyCluster_predictsHealthy() {
        KNN knn = new KNN(3);
        knn.fit(Main.X, Main.Y);
        assertEquals(0, knn.predict(new double[]{ 0.25, 0.25, 0.25, 0.15 }));
    }

    @Test
    @DisplayName("Diabetic patient cluster predicts class 2")
    void diabeticCluster_predictsDiabetic() {
        KNN knn = new KNN(3);
        knn.fit(Main.X, Main.Y);
        assertEquals(2, knn.predict(new double[]{ 0.85, 0.85, 0.85, 0.85 }));
    }
}

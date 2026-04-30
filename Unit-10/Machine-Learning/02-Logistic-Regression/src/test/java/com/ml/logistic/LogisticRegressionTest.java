package com.ml.logistic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LogisticRegressionTest {

    @Test
    @DisplayName("Sigmoid: σ(0) = 0.5")
    void sigmoid_zero() {
        assertEquals(0.5, LogisticRegression.sigmoid(0.0), 1e-9);
    }

    @Test
    @DisplayName("Sigmoid: large positive input → near 1")
    void sigmoid_largePositive() {
        assertTrue(LogisticRegression.sigmoid(10.0) > 0.99);
    }

    @Test
    @DisplayName("Sigmoid: large negative input → near 0")
    void sigmoid_largeNegative() {
        assertTrue(LogisticRegression.sigmoid(-10.0) < 0.01);
    }

    @Test
    @DisplayName("Linearly separable data: 100% accuracy")
    void linearlySeparable_perfectAccuracy() {
        // Class 1 at x > 0.5, class 0 at x < 0.5
        double[][] X = { {0.1}, {0.2}, {0.3}, {0.7}, {0.8}, {0.9} };
        int[]      y = {   0,    0,    0,    1,    1,    1   };
        LogisticRegression lr = new LogisticRegression(1.0, 2000);
        lr.fit(X, y);
        assertEquals(1.0, lr.accuracy(X, y), "Should achieve 100% on linearly separable data");
    }

    @Test
    @DisplayName("Predict returns 0 or 1 only")
    void predict_binaryOutput() {
        double[][] X = { {0.1}, {0.9} };
        int[]      y = {   0,    1   };
        LogisticRegression lr = new LogisticRegression(1.0, 500);
        lr.fit(X, y);
        int p = lr.predict(new double[]{0.5});
        assertTrue(p == 0 || p == 1, "predict() must return 0 or 1");
    }

    @Test
    @DisplayName("predictProba returns value in (0, 1)")
    void predictProba_inRange() {
        double[][] X = { {0.0}, {1.0} };
        int[]      y = {   0,    1   };
        LogisticRegression lr = new LogisticRegression(1.0, 500);
        lr.fit(X, y);
        double p = lr.predictProba(new double[]{0.5});
        assertTrue(p > 0 && p < 1, "predictProba must be in (0,1)");
    }

    @Test
    @DisplayName("High-signal spam features yield P(spam) > 0.8")
    void spamHighSignal() {
        LogisticRegression lr = new LogisticRegression(0.5, 1000);
        lr.fit(Main.X, Main.Y);
        double prob = lr.predictProba(new double[]{ 0.9, 0.9, 0.9, 0.1, 1.0 });
        assertTrue(prob > 0.8, "High-signal spam should have P(spam) > 0.8, got " + prob);
    }

    @Test
    @DisplayName("Low-signal ham features yield P(spam) < 0.2")
    void hamLowSignal() {
        LogisticRegression lr = new LogisticRegression(0.5, 1000);
        lr.fit(Main.X, Main.Y);
        double prob = lr.predictProba(new double[]{ 0.0, 0.1, 0.0, 0.9, 0.0 });
        assertTrue(prob < 0.2, "Low-signal ham should have P(spam) < 0.2, got " + prob);
    }

    @Test
    @DisplayName("Spam dataset: training accuracy ≥ 95%")
    void spamDataset_accuracy() {
        LogisticRegression lr = new LogisticRegression(0.5, 1000);
        lr.fit(Main.X, Main.Y);
        assertTrue(lr.accuracy(Main.X, Main.Y) >= 0.95,
            "Should achieve ≥95% on training data");
    }

    @Test
    @DisplayName("Weights vector length equals features + 1 (bias)")
    void weightsLength() {
        double[][] X = { {1, 2} };
        int[]      y = { 1 };
        LogisticRegression lr = new LogisticRegression(0.1, 1);
        lr.fit(X, y);
        assertEquals(3, lr.weights().length);
    }
}

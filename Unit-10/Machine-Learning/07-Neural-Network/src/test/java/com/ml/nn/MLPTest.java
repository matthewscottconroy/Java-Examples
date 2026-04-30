package com.ml.nn;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MLPTest {

    @Test
    @DisplayName("ReLU: negative input → 0, positive input → unchanged")
    void relu_basic() {
        double[] out = MLP.relu(new double[]{ -2, 0, 3, -0.5 });
        assertEquals(0.0, out[0], 1e-9);
        assertEquals(0.0, out[1], 1e-9);
        assertEquals(3.0, out[2], 1e-9);
        assertEquals(0.0, out[3], 1e-9);
    }

    @Test
    @DisplayName("Softmax: output sums to 1")
    void softmax_sumsToOne() {
        double[] z = { 1.0, 2.0, 3.0 };
        double[] out = MLP.softmax(z);
        double sum = 0;
        for (double v : out) sum += v;
        assertEquals(1.0, sum, 1e-9);
    }

    @Test
    @DisplayName("Softmax: largest input gets highest probability")
    void softmax_maxInput_maxProb() {
        double[] z = { 0.5, 2.0, 0.1 };
        double[] out = MLP.softmax(z);
        assertTrue(out[1] > out[0] && out[1] > out[2]);
    }

    @Test
    @DisplayName("Softmax: numerically stable for large inputs")
    void softmax_numericalStability() {
        double[] z = { 1000.0, 1001.0, 999.0 };
        double[] out = MLP.softmax(z);
        assertFalse(Double.isNaN(out[0]), "Softmax should not produce NaN for large inputs");
        double sum = 0;
        for (double v : out) sum += v;
        assertEquals(1.0, sum, 1e-9);
    }

    @Test
    @DisplayName("Forward pass: output has correct number of classes")
    void forwardPass_outputSize() {
        MLP mlp = new MLP(new int[]{4, 6, 3}, 0.01, 1, 1);
        mlp.fit(new double[][]{{0,0,0,0}}, new int[]{0}, 0L);
        double[][] activations = mlp.forwardPass(new double[]{0.1, 0.2, 0.3, 0.4});
        // Last layer should have 3 outputs
        assertEquals(3, activations[activations.length - 1].length);
    }

    @Test
    @DisplayName("predict() returns value in [0, numClasses)")
    void predict_inRange() {
        MLP mlp = new MLP(new int[]{4, 6, 3}, 0.01, 100, 4);
        mlp.fit(new double[][]{{0,0,0,0},{1,1,1,1},{0,1,0,1}}, new int[]{0,1,2}, 0L);
        int pred = mlp.predict(new double[]{0.5, 0.5, 0.5, 0.5});
        assertTrue(pred >= 0 && pred < 3);
    }

    @Test
    @DisplayName("predictProba() sums to 1.0")
    void predictProba_sumsToOne() {
        MLP mlp = new MLP(new int[]{3, 5, 2}, 0.01, 50, 2);
        mlp.fit(new double[][]{{0,0,0},{1,1,1}}, new int[]{0,1}, 0L);
        double[] probs = mlp.predictProba(new double[]{0.5, 0.5, 0.5});
        double sum = 0;
        for (double p : probs) sum += p;
        assertEquals(1.0, sum, 1e-9);
    }

    @Test
    @DisplayName("XOR: MLP learns XOR with hidden layer")
    void learnXOR() {
        // XOR cannot be solved without a hidden layer
        double[][] X = { {0,0}, {0,1}, {1,0}, {1,1} };
        int[]      y = {   0,    1,    1,    0   };
        MLP mlp = new MLP(new int[]{2, 4, 2}, 0.1, 2000, 4);
        mlp.fit(X, y, 7L);
        assertTrue(mlp.accuracy(X, y) >= 0.75,
            "MLP should solve XOR with ≥75% accuracy");
    }

    @Test
    @DisplayName("Linearly separable: 2-layer MLP achieves 100% accuracy")
    void linearSeparable_perfectAccuracy() {
        double[][] X = {
            {0.1, 0.1}, {0.2, 0.1},   // class 0
            {0.8, 0.9}, {0.9, 0.8},   // class 1
        };
        int[] y = { 0, 0, 1, 1 };
        MLP mlp = new MLP(new int[]{2, 4, 2}, 0.05, 500, 4);
        mlp.fit(X, y, 0L);
        assertEquals(1.0, mlp.accuracy(X, y));
    }

    @Test
    @DisplayName("Digit pattern dataset: MLP achieves ≥ 90% training accuracy")
    void digitDataset_accuracy() {
        MLP mlp = new MLP(new int[]{16, 12, 5}, 0.05, 500, 8);
        mlp.fit(Main.X, Main.Y, 42L);
        assertTrue(mlp.accuracy(Main.X, Main.Y) >= 0.9,
            "MLP should achieve ≥90% on digit pattern training data");
    }
}

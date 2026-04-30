package com.ml.nn;

import java.util.Arrays;

/**
 * Handwritten digit pattern recogniser (simplified 4×4 binary grid).
 *
 * Each "digit" is encoded as 16 binary pixel values (4×4 grid flattened).
 * The network learns to distinguish between patterns representing digits 0–4.
 *
 * Architecture: 16 inputs → 12 hidden (ReLU) → 5 outputs (softmax)
 */
public class Main {

    static final String[] DIGIT_NAMES = { "Zero", "One", "Two", "Three", "Four" };

    // 4×4 binary pixel grids for digits 0–4, with slight variants per class
    // Each row = one training example (16 pixels, flattened)
    static final double[][] X;
    static final int[]      Y;

    static {
        // Digit patterns (4×4, each row is one sample)
        // 0: outer ring lit
        double[][] zero = {
            {1,1,1,1, 1,0,0,1, 1,0,0,1, 1,1,1,1},
            {1,1,1,1, 1,0,0,1, 1,0,0,1, 1,1,1,0},
            {1,1,1,1, 1,0,0,1, 1,0,0,1, 0,1,1,1},
            {0,1,1,1, 1,0,0,1, 1,0,0,1, 1,1,1,1},
        };
        // 1: vertical bar
        double[][] one = {
            {0,0,1,0, 0,0,1,0, 0,0,1,0, 0,0,1,0},
            {0,1,1,0, 0,0,1,0, 0,0,1,0, 0,0,1,0},
            {0,0,1,0, 0,0,1,0, 0,0,1,0, 0,1,1,0},
            {0,0,1,1, 0,0,1,0, 0,0,1,0, 0,0,1,0},
        };
        // 2: top, middle, bottom bars + diagonal
        double[][] two = {
            {1,1,1,1, 0,0,0,1, 1,1,1,1, 1,0,0,0},
            {1,1,1,1, 0,0,1,1, 1,1,1,1, 1,0,0,1},
            {1,1,1,0, 0,0,0,1, 1,1,1,0, 1,0,0,0},
            {1,1,1,1, 0,0,0,1, 0,1,1,1, 1,0,0,0},
        };
        // 3: right side + horizontals
        double[][] three = {
            {1,1,1,1, 0,0,0,1, 1,1,1,1, 0,0,0,1},
            {1,1,1,1, 0,0,0,1, 0,1,1,1, 0,0,0,1},
            {1,1,1,0, 0,0,0,1, 1,1,1,1, 0,0,0,1},
            {1,1,1,1, 0,0,0,1, 1,1,1,1, 1,0,0,1},
        };
        // 4: left-top + middle + right bar
        double[][] four = {
            {1,0,0,1, 1,0,0,1, 1,1,1,1, 0,0,0,1},
            {1,0,0,1, 1,0,0,1, 0,1,1,1, 0,0,0,1},
            {1,0,1,1, 1,0,0,1, 1,1,1,1, 0,0,0,1},
            {1,0,0,1, 1,0,1,1, 1,1,1,1, 0,0,0,1},
        };

        double[][][] digits = { zero, one, two, three, four };
        int total = 0;
        for (double[][] d : digits) total += d.length;
        X = new double[total][];
        Y = new int[total];
        int idx = 0;
        for (int cls = 0; cls < digits.length; cls++) {
            for (double[] sample : digits[cls]) {
                X[idx] = sample;
                Y[idx] = cls;
                idx++;
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Neural Network (MLP) — Digit Pattern Recogniser ===\n");
        System.out.printf("Training samples: %d  Classes: %d  Input features: %d%n",
            X.length, 5, 16);

        // Architecture: 16 → 12 → 5
        MLP mlp = new MLP(new int[]{ 16, 12, 5 }, 0.05, 500, 8);
        mlp.fit(X, Y, 42L);

        System.out.printf("Training accuracy: %.1f%%%n%n", mlp.accuracy(X, Y) * 100);

        System.out.println("Predictions:");
        System.out.printf("  %-8s  %-8s  %s%n", "Actual", "Predicted", "Confidence");
        System.out.println("  " + "-".repeat(40));
        for (int i = 0; i < X.length; i++) {
            int pred = mlp.predict(X[i]);
            double[] probs = mlp.predictProba(X[i]);
            System.out.printf("  %-8s  %-8s  %.1f%%%n",
                DIGIT_NAMES[Y[i]], DIGIT_NAMES[pred], probs[pred] * 100);
        }
    }
}

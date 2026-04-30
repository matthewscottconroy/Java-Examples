package com.ml.linear;

import java.util.Arrays;

/**
 * Ordinary least-squares linear regression trained with batch gradient descent.
 *
 * <p>Models the relationship: ŷ = w₀ + w₁x₁ + w₂x₂ + ... + wₙxₙ
 * where w₀ is the bias (intercept) and w₁..wₙ are the feature weights.
 *
 * <p>Parameters are updated each epoch by computing the gradient of mean
 * squared error (MSE) over the entire training set:
 * <pre>
 *   MSE  = (1/m) Σ (ŷᵢ - yᵢ)²
 *   ∂MSE/∂wⱼ = (2/m) Σ (ŷᵢ - yᵢ) xᵢⱼ      (xᵢ₀ = 1 for bias)
 * </pre>
 */
public class LinearRegression {

    private double[] weights;   // weights[0] = bias
    private final double learningRate;
    private final int epochs;

    public LinearRegression(double learningRate, int epochs) {
        this.learningRate = learningRate;
        this.epochs = epochs;
    }

    /** Trains on features matrix X (m×n) and target vector y (m). */
    public void fit(double[][] X, double[] y) {
        int m = X.length;
        int n = X[0].length + 1;           // +1 for bias column
        weights = new double[n];           // initialised to zero

        double[][] Xb = addBiasColumn(X);  // m × n  (first column = 1)

        for (int epoch = 0; epoch < epochs; epoch++) {
            double[] predictions = predict(Xb, weights);
            double[] errors = new double[m];
            for (int i = 0; i < m; i++) errors[i] = predictions[i] - y[i];

            // Gradient update for each weight
            for (int j = 0; j < n; j++) {
                double gradient = 0;
                for (int i = 0; i < m; i++) gradient += errors[i] * Xb[i][j];
                weights[j] -= learningRate * (2.0 / m) * gradient;
            }
        }
    }

    /** Predicts for a single sample (without bias column). */
    public double predict(double[] x) {
        double sum = weights[0];    // bias
        for (int j = 0; j < x.length; j++) sum += weights[j + 1] * x[j];
        return sum;
    }

    public double mse(double[][] X, double[] y) {
        double sum = 0;
        for (int i = 0; i < X.length; i++) {
            double err = predict(X[i]) - y[i];
            sum += err * err;
        }
        return sum / X.length;
    }

    public double r2(double[][] X, double[] y) {
        double mean = Arrays.stream(y).average().orElse(0);
        double ssTot = 0, ssRes = 0;
        for (int i = 0; i < X.length; i++) {
            ssTot += (y[i] - mean) * (y[i] - mean);
            ssRes += (predict(X[i]) - y[i]) * (predict(X[i]) - y[i]);
        }
        return ssTot == 0 ? 1.0 : 1.0 - ssRes / ssTot;
    }

    public double[] weights() { return weights.clone(); }

    // ---------------------------------------------------------------

    private static double[][] addBiasColumn(double[][] X) {
        double[][] Xb = new double[X.length][X[0].length + 1];
        for (int i = 0; i < X.length; i++) {
            Xb[i][0] = 1.0;
            System.arraycopy(X[i], 0, Xb[i], 1, X[i].length);
        }
        return Xb;
    }

    private static double[] predict(double[][] Xb, double[] w) {
        double[] out = new double[Xb.length];
        for (int i = 0; i < Xb.length; i++) {
            double sum = 0;
            for (int j = 0; j < w.length; j++) sum += Xb[i][j] * w[j];
            out[i] = sum;
        }
        return out;
    }
}

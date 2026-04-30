package com.ml.logistic;

/**
 * Binary logistic regression trained with batch gradient descent.
 *
 * <p>Models P(y=1 | x) = σ(w·x) where σ is the sigmoid function.
 * Parameters are updated by minimising binary cross-entropy loss:
 * <pre>
 *   L = -(1/m) Σ [ yᵢ log(ŷᵢ) + (1-yᵢ) log(1-ŷᵢ) ]
 *   ∂L/∂wⱼ = (1/m) Σ (ŷᵢ - yᵢ) xᵢⱼ
 * </pre>
 * The gradient of cross-entropy with respect to the weights has the same
 * form as MSE gradient — a useful coincidence of the sigmoid + log-loss pair.
 */
public class LogisticRegression {

    private double[] weights;    // weights[0] = bias
    private final double learningRate;
    private final int epochs;

    public LogisticRegression(double learningRate, int epochs) {
        this.learningRate = learningRate;
        this.epochs = epochs;
    }

    public void fit(double[][] X, int[] y) {
        int m = X.length;
        int n = X[0].length + 1;
        weights = new double[n];

        double[][] Xb = addBiasColumn(X);

        for (int epoch = 0; epoch < epochs; epoch++) {
            double[] errors = new double[m];
            for (int i = 0; i < m; i++) {
                errors[i] = sigmoid(dot(Xb[i], weights)) - y[i];
            }
            for (int j = 0; j < n; j++) {
                double grad = 0;
                for (int i = 0; i < m; i++) grad += errors[i] * Xb[i][j];
                weights[j] -= learningRate * grad / m;
            }
        }
    }

    /** Returns probability P(y=1 | x). */
    public double predictProba(double[] x) {
        double z = weights[0];
        for (int j = 0; j < x.length; j++) z += weights[j + 1] * x[j];
        return sigmoid(z);
    }

    /** Returns class label 0 or 1 using threshold 0.5. */
    public int predict(double[] x) {
        return predictProba(x) >= 0.5 ? 1 : 0;
    }

    public double accuracy(double[][] X, int[] y) {
        int correct = 0;
        for (int i = 0; i < X.length; i++) if (predict(X[i]) == y[i]) correct++;
        return (double) correct / X.length;
    }

    public double[] weights() { return weights.clone(); }

    // ---------------------------------------------------------------

    static double sigmoid(double z) { return 1.0 / (1.0 + Math.exp(-z)); }

    private static double dot(double[] a, double[] b) {
        double s = 0;
        for (int i = 0; i < a.length; i++) s += a[i] * b[i];
        return s;
    }

    private static double[][] addBiasColumn(double[][] X) {
        double[][] Xb = new double[X.length][X[0].length + 1];
        for (int i = 0; i < X.length; i++) {
            Xb[i][0] = 1.0;
            System.arraycopy(X[i], 0, Xb[i], 1, X[i].length);
        }
        return Xb;
    }
}

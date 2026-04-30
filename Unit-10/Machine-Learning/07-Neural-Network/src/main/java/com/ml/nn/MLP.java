package com.ml.nn;

import java.util.Random;

/**
 * Multi-Layer Perceptron (fully connected feedforward neural network).
 *
 * <p>Architecture: input layer → one or more hidden layers → output layer.
 * Hidden layers use ReLU activation; the output layer uses softmax for
 * multi-class classification.
 *
 * <p>Training uses mini-batch stochastic gradient descent with backpropagation:
 * <ol>
 *   <li><b>Forward pass</b> — compute activations layer by layer</li>
 *   <li><b>Loss</b> — categorical cross-entropy on softmax outputs</li>
 *   <li><b>Backward pass</b> — compute gradients via the chain rule</li>
 *   <li><b>Update</b> — subtract learning_rate × gradient from each weight</li>
 * </ol>
 *
 * <p>Weight initialisation uses He initialisation (variance = 2/fan_in) which
 * works well with ReLU activations by preserving signal variance across layers.
 */
public class MLP {

    private final int[] layerSizes;       // e.g. [4, 8, 3] = 4 inputs, 8 hidden, 3 outputs
    private final double learningRate;
    private final int epochs;
    private final int batchSize;

    private double[][][] W;   // W[l][j][i] = weight from neuron i in layer l to j in l+1
    private double[][]  b;   // b[l][j] = bias of neuron j in layer l+1

    public MLP(int[] layerSizes, double learningRate, int epochs, int batchSize) {
        this.layerSizes   = layerSizes;
        this.learningRate = learningRate;
        this.epochs       = epochs;
        this.batchSize    = batchSize;
    }

    // -----------------------------------------------------------------
    // Training
    // -----------------------------------------------------------------

    public void fit(double[][] X, int[] y, long seed) {
        initWeights(seed);
        int m = X.length;
        Random rng = new Random(seed);
        int numClasses = layerSizes[layerSizes.length - 1];

        for (int epoch = 0; epoch < epochs; epoch++) {
            // Shuffle indices for mini-batch SGD
            int[] idx = shuffled(m, rng);

            for (int start = 0; start < m; start += batchSize) {
                int end = Math.min(start + batchSize, m);
                int bsz = end - start;

                // Accumulate gradients
                double[][][] dW = new double[W.length][][];
                double[][]   db = new double[b.length][];
                for (int l = 0; l < W.length; l++) {
                    dW[l] = new double[W[l].length][W[l][0].length];
                    db[l] = new double[b[l].length];
                }

                for (int bi = start; bi < end; bi++) {
                    double[] x = X[idx[bi]];
                    double[] yOH = oneHot(y[idx[bi]], numClasses);
                    backward(x, yOH, dW, db);
                }

                // Apply average gradient
                for (int l = 0; l < W.length; l++) {
                    for (int j = 0; j < W[l].length; j++) {
                        for (int i = 0; i < W[l][j].length; i++) {
                            W[l][j][i] -= learningRate * dW[l][j][i] / bsz;
                        }
                        b[l][j] -= learningRate * db[l][j] / bsz;
                    }
                }
            }
        }
    }

    // -----------------------------------------------------------------
    // Prediction
    // -----------------------------------------------------------------

    public int predict(double[] x) {
        double[] probs = forwardPass(x)[layerSizes.length - 1];
        int best = 0;
        for (int i = 1; i < probs.length; i++) if (probs[i] > probs[best]) best = i;
        return best;
    }

    public double[] predictProba(double[] x) {
        return forwardPass(x)[layerSizes.length - 1];
    }

    public double accuracy(double[][] X, int[] y) {
        int correct = 0;
        for (int i = 0; i < X.length; i++) if (predict(X[i]) == y[i]) correct++;
        return (double) correct / X.length;
    }

    // -----------------------------------------------------------------
    // Forward pass — returns activations for every layer
    // -----------------------------------------------------------------

    double[][] forwardPass(double[] x) {
        int L = layerSizes.length;
        double[][] a = new double[L][];
        a[0] = x.clone();

        for (int l = 0; l < W.length; l++) {
            int nextSize = layerSizes[l + 1];
            double[] z = new double[nextSize];
            for (int j = 0; j < nextSize; j++) {
                z[j] = b[l][j];
                for (int i = 0; i < a[l].length; i++) z[j] += W[l][j][i] * a[l][i];
            }
            a[l + 1] = (l == W.length - 1) ? softmax(z) : relu(z);
        }
        return a;
    }

    // -----------------------------------------------------------------
    // Backward pass — accumulates gradients into dW and db
    // -----------------------------------------------------------------

    private void backward(double[] x, double[] yOH, double[][][] dW, double[][] db) {
        double[][] a = forwardPass(x);
        int L = layerSizes.length;

        // δ[l] = error signal for layer l+1
        double[][] delta = new double[W.length][];

        // Output layer: softmax + cross-entropy gradient = a - y
        int lastL = W.length - 1;
        delta[lastL] = new double[layerSizes[L - 1]];
        for (int j = 0; j < delta[lastL].length; j++) {
            delta[lastL][j] = a[L - 1][j] - yOH[j];
        }

        // Hidden layers (backprop through ReLU)
        for (int l = lastL - 1; l >= 0; l--) {
            delta[l] = new double[layerSizes[l + 1]];
            for (int i = 0; i < layerSizes[l + 1]; i++) {
                double err = 0;
                for (int j = 0; j < layerSizes[l + 2]; j++) {
                    err += delta[l + 1][j] * W[l + 1][j][i];
                }
                delta[l][i] = err * reluDerivative(a[l + 1][i]);
            }
        }

        // Accumulate gradients
        for (int l = 0; l < W.length; l++) {
            for (int j = 0; j < W[l].length; j++) {
                for (int i = 0; i < W[l][j].length; i++) {
                    dW[l][j][i] += delta[l][j] * a[l][i];
                }
                db[l][j] += delta[l][j];
            }
        }
    }

    // -----------------------------------------------------------------
    // Activations
    // -----------------------------------------------------------------

    static double[] relu(double[] z) {
        double[] out = new double[z.length];
        for (int i = 0; i < z.length; i++) out[i] = Math.max(0, z[i]);
        return out;
    }

    static double reluDerivative(double a) { return a > 0 ? 1.0 : 0.0; }

    static double[] softmax(double[] z) {
        double max = z[0];
        for (double v : z) if (v > max) max = v;
        double sum = 0;
        double[] out = new double[z.length];
        for (int i = 0; i < z.length; i++) { out[i] = Math.exp(z[i] - max); sum += out[i]; }
        for (int i = 0; i < out.length; i++) out[i] /= sum;
        return out;
    }

    // -----------------------------------------------------------------
    // Utilities
    // -----------------------------------------------------------------

    private void initWeights(long seed) {
        Random rng = new Random(seed);
        int L = layerSizes.length;
        W = new double[L - 1][][];
        b = new double[L - 1][];
        for (int l = 0; l < L - 1; l++) {
            int fan_in = layerSizes[l], fan_out = layerSizes[l + 1];
            W[l] = new double[fan_out][fan_in];
            b[l] = new double[fan_out];
            double std = Math.sqrt(2.0 / fan_in);   // He initialisation
            for (int j = 0; j < fan_out; j++)
                for (int i = 0; i < fan_in; i++)
                    W[l][j][i] = rng.nextGaussian() * std;
        }
    }

    private static double[] oneHot(int label, int numClasses) {
        double[] v = new double[numClasses];
        v[label] = 1.0;
        return v;
    }

    private static int[] shuffled(int n, Random rng) {
        int[] idx = new int[n];
        for (int i = 0; i < n; i++) idx[i] = i;
        for (int i = n - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = idx[i]; idx[i] = idx[j]; idx[j] = tmp;
        }
        return idx;
    }
}

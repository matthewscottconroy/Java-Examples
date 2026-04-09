package com.markovmonopoly.core;

import java.util.Arrays;

/**
 * An immutable n×n row-stochastic matrix representing the transition probabilities
 * of a Markov chain.
 *
 * <p>Entry {@code [i][j]} is the probability of transitioning from state {@code i}
 * to state {@code j} in one step. Every row must sum to exactly 1.0 (within a
 * small tolerance) for the matrix to be a valid probability distribution.
 *
 * <p>All mutating operations return new {@code TransitionMatrix} instances.
 */
public final class TransitionMatrix {

    /** Tolerance used when checking whether row sums equal 1. */
    public static final double ROW_SUM_TOLERANCE = 1e-9;

    private final double[][] data;
    private final int n;

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    private TransitionMatrix(double[][] data) {
        this.n    = data.length;
        this.data = deepCopy(data);
    }

    /**
     * Creates a TransitionMatrix from the given 2-D array.
     * The array must be square; no validation of row sums is performed here
     * (call {@link #validate()} explicitly when needed).
     */
    public static TransitionMatrix of(double[][] data) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Matrix data must be non-null and non-empty.");
        }
        int n = data.length;
        for (double[] row : data) {
            if (row.length != n) {
                throw new IllegalArgumentException("Matrix must be square.");
            }
        }
        return new TransitionMatrix(data);
    }

    /** Creates an n×n identity matrix. */
    public static TransitionMatrix identity(int n) {
        double[][] d = new double[n][n];
        for (int i = 0; i < n; i++) d[i][i] = 1.0;
        return new TransitionMatrix(d);
    }

    /** Creates an n×n matrix where every entry is 1/n (uniform transitions). */
    public static TransitionMatrix uniform(int n) {
        double[][] d = new double[n][n];
        double p = 1.0 / n;
        for (double[] row : d) Arrays.fill(row, p);
        return new TransitionMatrix(d);
    }

    /** Creates an n×n zero matrix (not stochastic — used as a builder starting point). */
    public static TransitionMatrix zeros(int n) {
        return new TransitionMatrix(new double[n][n]);
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    /** Returns the dimension (number of states). */
    public int size() { return n; }

    /** Returns entry [row][col]. */
    public double get(int row, int col) { return data[row][col]; }

    /** Returns a defensive copy of the entire matrix. */
    public double[][] toArray() { return deepCopy(data); }

    /** Returns a copy of row {@code i}. */
    public double[] getRow(int i) { return Arrays.copyOf(data[i], n); }

    /** Returns a copy of column {@code j}. */
    public double[] getColumn(int j) {
        double[] col = new double[n];
        for (int i = 0; i < n; i++) col[i] = data[i][j];
        return col;
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    /**
     * Checks that every row sums to 1 and that no entry is negative.
     *
     * @throws IllegalStateException with a descriptive message if invalid.
     */
    public void validate() {
        for (int i = 0; i < n; i++) {
            double sum = 0;
            for (int j = 0; j < n; j++) {
                if (data[i][j] < 0) {
                    throw new IllegalStateException(
                        "Negative probability at [" + i + "][" + j + "]: " + data[i][j]);
                }
                sum += data[i][j];
            }
            if (Math.abs(sum - 1.0) > ROW_SUM_TOLERANCE) {
                throw new IllegalStateException(
                    "Row " + i + " sums to " + sum + ", expected 1.0 (difference: " +
                    Math.abs(sum - 1.0) + ")");
            }
        }
    }

    /** Returns {@code true} if the matrix is a valid row-stochastic matrix. */
    public boolean isValid() {
        try { validate(); return true; } catch (IllegalStateException e) { return false; }
    }

    /** Returns {@code true} if there exists at least one state with P[i][i] == 1. */
    public boolean hasAbsorbingState() {
        for (int i = 0; i < n; i++) {
            if (Math.abs(data[i][i] - 1.0) < ROW_SUM_TOLERANCE) return true;
        }
        return false;
    }

    /** Returns {@code true} if columns also sum to 1 (doubly stochastic). */
    public boolean isDoublyStochastic() {
        for (int j = 0; j < n; j++) {
            double sum = 0;
            for (int i = 0; i < n; i++) sum += data[i][j];
            if (Math.abs(sum - 1.0) > ROW_SUM_TOLERANCE) return false;
        }
        return isValid();
    }

    // -------------------------------------------------------------------------
    // Linear algebra
    // -------------------------------------------------------------------------

    /** Returns the matrix product {@code this × other}. */
    public TransitionMatrix multiply(TransitionMatrix other) {
        if (other.n != n) {
            throw new IllegalArgumentException("Matrix dimensions do not match.");
        }
        double[][] result = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int k = 0; k < n; k++) {
                if (data[i][k] == 0.0) continue;
                for (int j = 0; j < n; j++) {
                    result[i][j] += data[i][k] * other.data[k][j];
                }
            }
        }
        return new TransitionMatrix(result);
    }

    /**
     * Raises this matrix to the {@code k}-th power using repeated squaring.
     * O(n³ log k) — much faster than multiplying {@code k} times for large k.
     *
     * @param k non-negative integer
     */
    public TransitionMatrix power(int k) {
        if (k < 0) throw new IllegalArgumentException("Exponent must be non-negative.");
        if (k == 0) return identity(n);
        if (k == 1) return new TransitionMatrix(data);

        TransitionMatrix result = identity(n);
        TransitionMatrix base   = new TransitionMatrix(data);
        while (k > 0) {
            if ((k & 1) == 1) result = result.multiply(base);
            base = base.multiply(base);
            k >>= 1;
        }
        return result;
    }

    /**
     * Left-multiplies the row vector {@code v} by this matrix: returns {@code v * P}.
     * This is one step of the probability vector update when {@code v} is the current
     * probability distribution over states.
     */
    public double[] leftMultiply(double[] v) {
        if (v.length != n) throw new IllegalArgumentException("Vector length must equal matrix size.");
        double[] result = new double[n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                result[j] += v[i] * data[i][j];
            }
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Stationary distribution
    // -------------------------------------------------------------------------

    /**
     * Computes the stationary distribution {@code π} satisfying {@code π = π * P}
     * using power iteration.
     *
     * <p>Starts from the uniform distribution and repeatedly applies the transition
     * matrix until convergence: {@code max|π_new[i] - π[i]| < tolerance}.
     *
     * <p><b>Convergence guarantee:</b> This method converges for irreducible,
     * aperiodic (ergodic) Markov chains. For chains that are not ergodic,
     * the result depends on the starting distribution.
     *
     * @param maxIterations maximum number of iterations before giving up
     * @param tolerance     convergence threshold (recommended: 1e-10)
     * @return the stationary distribution as a probability vector
     */
    public double[] stationaryDistribution(int maxIterations, double tolerance) {
        double[] pi = new double[n];
        Arrays.fill(pi, 1.0 / n);  // start from uniform distribution

        for (int iter = 0; iter < maxIterations; iter++) {
            double[] piNew = leftMultiply(pi);
            double maxDiff = 0;
            for (int i = 0; i < n; i++) {
                maxDiff = Math.max(maxDiff, Math.abs(piNew[i] - pi[i]));
            }
            pi = piNew;
            if (maxDiff < tolerance) break;
        }
        return pi;
    }

    /** Computes the stationary distribution using default convergence parameters. */
    public double[] stationaryDistribution() {
        return stationaryDistribution(100_000, 1e-12);
    }

    // -------------------------------------------------------------------------
    // Immutable update
    // -------------------------------------------------------------------------

    /** Returns a new matrix identical to this one except entry [row][col] = value. */
    public TransitionMatrix withEntry(int row, int col, double value) {
        double[][] copy = deepCopy(data);
        copy[row][col] = value;
        return new TransitionMatrix(copy);
    }

    /** Returns a new matrix with each row normalized to sum to 1. */
    public TransitionMatrix normalized() {
        double[][] copy = deepCopy(data);
        for (int i = 0; i < n; i++) {
            double sum = 0;
            for (double v : copy[i]) sum += v;
            if (sum > 0) for (int j = 0; j < n; j++) copy[i][j] /= sum;
            else Arrays.fill(copy[i], 1.0 / n);  // default to uniform if row is all zeros
        }
        return new TransitionMatrix(copy);
    }

    // -------------------------------------------------------------------------
    // Display
    // -------------------------------------------------------------------------

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TransitionMatrix(").append(n).append("x").append(n).append("):\n");
        for (int i = 0; i < n; i++) {
            sb.append("  [");
            for (int j = 0; j < n; j++) {
                if (j > 0) sb.append(", ");
                sb.append(String.format("%.4f", data[i][j]));
            }
            sb.append("]\n");
        }
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static double[][] deepCopy(double[][] src) {
        double[][] copy = new double[src.length][];
        for (int i = 0; i < src.length; i++) {
            copy[i] = Arrays.copyOf(src[i], src[i].length);
        }
        return copy;
    }
}

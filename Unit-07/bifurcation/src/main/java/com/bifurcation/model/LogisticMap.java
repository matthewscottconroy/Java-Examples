package com.bifurcation.model;

/**
 * The logistic map: {@code x_{n+1} = r · x_n · (1 − x_n)}.
 *
 * <h2>Overview</h2>
 * <p>The logistic map is a simple one-dimensional discrete-time dynamical system
 * that exhibits the full route to chaos through period doubling. Despite its
 * algebraic simplicity it captures the universal behaviour seen in many real-world
 * systems: fixed points, period doubling, chaos, and windows of periodicity.</p>
 *
 * <h2>Parameter ranges</h2>
 * <ul>
 *   <li>{@code r < 1}: population collapses to extinction (x → 0).</li>
 *   <li>{@code 1 < r < 3}: single stable fixed point at {@code x* = (r−1)/r}.</li>
 *   <li>{@code r = 3}: first period-doubling bifurcation; period-2 orbit begins.</li>
 *   <li>{@code r ≈ 3.449}: period-4 bifurcation.</li>
 *   <li>{@code r ≈ 3.544}: period-8 bifurcation.</li>
 *   <li>{@code r ≈ 3.569}: onset of chaos.</li>
 *   <li>{@code r = 4}: fully chaotic; x is uniformly distributed on (0,1).</li>
 * </ul>
 *
 * <h2>Feigenbaum constant</h2>
 * <p>The ratio of successive bifurcation interval widths converges to the universal
 * Feigenbaum constant {@code δ ≈ 4.6692016...}, independent of the particular map.</p>
 */
public class LogisticMap {

    /**
     * The universal Feigenbaum constant δ, the limiting ratio of successive
     * bifurcation interval widths in the period-doubling route to chaos.
     *
     * <p>This constant is universal: it appears in every smooth one-dimensional
     * map with a single quadratic maximum, regardless of the specific functional form.
     */
    public static final double FEIGENBAUM_DELTA = 4.669201609102990;

    private double r;

    /**
     * Construct a logistic map with the given parameter value.
     *
     * @param r the growth-rate parameter; must be in [0, 4] for x to remain in [0, 1]
     */
    public LogisticMap(double r) {
        this.r = r;
    }

    /**
     * Apply one iteration of the logistic map: {@code x_{n+1} = r · x · (1 − x)}.
     *
     * @param x the current population value; should be in [0, 1]
     * @return  the next population value, always in [0, 1] when r ∈ [0, 4]
     */
    public double iterate(double x) {
        return r * x * (1.0 - x);
    }

    /**
     * Compute the attractor set for a sweep of r values.
     *
     * <p>For each r value, the map is run for {@code transientSteps} iterations
     * (which are discarded to allow transients to die out), then {@code attractorSteps}
     * iterations are recorded and appended to the returned array.
     *
     * @param rMin          minimum r value (inclusive)
     * @param rMax          maximum r value (inclusive)
     * @param rSteps        number of r values to sample (one per pixel column)
     * @param transientSteps number of warm-up iterations to discard
     * @param attractorSteps number of attractor iterations to record per r value
     * @return a {@code double[]} of length {@code rSteps * attractorSteps} containing
     *         the attractor x values; the first {@code attractorSteps} entries correspond
     *         to {@code rMin}, the next to the second r value, and so on
     */
    public double[] bifurcationPoints(double rMin, double rMax,
                                      int rSteps, int transientSteps, int attractorSteps) {
        double[] result = new double[rSteps * attractorSteps];
        double rStep = (rSteps > 1) ? (rMax - rMin) / (rSteps - 1) : 0.0;
        int idx = 0;

        for (int i = 0; i < rSteps; i++) {
            double rVal = (rSteps > 1) ? rMin + i * rStep : rMin;
            double x = 0.5;

            // discard transient
            for (int t = 0; t < transientSteps; t++) {
                x = rVal * x * (1.0 - x);
            }

            // record attractor
            for (int a = 0; a < attractorSteps; a++) {
                x = rVal * x * (1.0 - x);
                result[idx++] = x;
            }
        }

        return result;
    }

    /**
     * Compute the stable fixed point of the logistic map for the given parameter.
     *
     * <p>For {@code 1 < r < 3} the non-trivial fixed point is {@code x* = (r−1)/r}.
     * For {@code r ≤ 1} the only fixed point is 0 (extinction).
     * For {@code r ≥ 3} the fixed point is unstable and period doubling has occurred;
     * this method still returns the formula value {@code (r−1)/r} as a reference.
     *
     * @param r the growth-rate parameter
     * @return  the non-trivial fixed point {@code (r−1)/r}, or 0.0 if {@code r ≤ 1}
     */
    public double fixedPoint(double r) {
        if (r <= 1.0) return 0.0;
        return (r - 1.0) / r;
    }

    /**
     * Estimate whether the map is in a chaotic regime for the current {@code r} value.
     *
     * <p>Chaos is detected by computing the Lyapunov exponent: the time-average of
     * {@code log|r(1 − 2x)|} (the log of the absolute value of the map's derivative).
     * A positive Lyapunov exponent indicates exponential divergence of nearby
     * trajectories, i.e., chaos.
     *
     * @param r       the growth-rate parameter to test
     * @param samples number of iterations to use for the Lyapunov estimate
     * @return {@code true} if the estimated Lyapunov exponent is positive (chaotic)
     */
    public boolean isChaotic(double r, int samples) {
        double x = 0.5;
        double lyapunov = 0.0;

        // warm up
        for (int i = 0; i < 200; i++) {
            x = r * x * (1.0 - x);
        }

        for (int i = 0; i < samples; i++) {
            x = r * x * (1.0 - x);
            double deriv = Math.abs(r * (1.0 - 2.0 * x));
            if (deriv > 0) {
                lyapunov += Math.log(deriv);
            }
        }

        return (lyapunov / samples) > 0.0;
    }

    // -------------------------------------------------------------------------
    // Getters / setters
    // -------------------------------------------------------------------------

    /** Returns the current growth-rate parameter r. */
    public double getR() { return r; }

    /** Sets the growth-rate parameter r. */
    public void setR(double r) { this.r = r; }
}

package com.epidemic.model;

/**
 * Aggregate SIR epidemic model integrated with the classic fourth-order
 * Runge-Kutta (RK4) method.
 *
 * <h2>Equations of motion</h2>
 * <pre>
 *   dS/dt = −β · S · I / N
 *   dI/dt =  β · S · I / N − γ · I
 *   dR/dt =  γ · I
 * </pre>
 * where {@code N = S + I + R} is the (conserved) total population.
 *
 * <h2>Conservation law</h2>
 * <p>The sum S + I + R is an invariant of the ODE; numerical integration
 * with RK4 preserves this to within floating-point rounding error.
 *
 * <h2>Basic reproduction number</h2>
 * <p>R₀ = β / γ.  When R₀ &gt; 1 the epidemic grows initially;
 * when R₀ &lt; 1 it decays from the start.
 *
 * <h2>Default parameters</h2>
 * <ul>
 *   <li>N = 1000 (total population)</li>
 *   <li>Initial infected = 10</li>
 * </ul>
 */
public class SIROde {

    /** Default total population. */
    public static final double DEFAULT_N  = 1000.0;

    /** Default number of initially infected individuals. */
    public static final double DEFAULT_I0 = 10.0;

    private final double n;
    private double s;
    private double i;
    private double r;

    private double initialI0;

    /**
     * Construct an ODE SIR model with default population (N=1000) and
     * default initial infected count (I₀=10).
     */
    public SIROde() {
        this(DEFAULT_N, DEFAULT_I0);
    }

    /**
     * Construct an ODE SIR model.
     *
     * @param n   total population (fixed throughout simulation)
     * @param i0  number of initially infected individuals (clamped to [1, n-1])
     */
    public SIROde(double n, double i0) {
        this.n = n;
        this.initialI0 = Math.max(1, Math.min(i0, n - 1));
        reset(this.initialI0);
    }

    // -------------------------------------------------------------------------
    // Simulation control
    // -------------------------------------------------------------------------

    /**
     * Advance the model by {@code dt} using RK4 integration.
     *
     * @param dt    time step (days or arbitrary time units); recommend 0.1
     * @param beta  transmission rate ∈ (0, ∞)
     * @param gamma recovery rate ∈ (0, ∞)
     */
    public void step(double dt, double beta, double gamma) {
        double[] state = {s, i, r};
        double[] k1 = derivatives(state, beta, gamma);
        double[] k2 = derivatives(add(state, scale(k1, dt / 2)), beta, gamma);
        double[] k3 = derivatives(add(state, scale(k2, dt / 2)), beta, gamma);
        double[] k4 = derivatives(add(state, scale(k3, dt)),     beta, gamma);

        s = state[0] + (dt / 6.0) * (k1[0] + 2*k2[0] + 2*k3[0] + k4[0]);
        i = state[1] + (dt / 6.0) * (k1[1] + 2*k2[1] + 2*k3[1] + k4[1]);
        r = state[2] + (dt / 6.0) * (k1[2] + 2*k2[2] + 2*k3[2] + k4[2]);

        // Clamp to prevent floating-point negatives
        s = Math.max(0, s);
        i = Math.max(0, i);
        r = Math.max(0, r);
    }

    /**
     * Reset the model to its initial conditions.
     *
     * @param i0 number of initially infected individuals
     */
    public void reset(double i0) {
        this.initialI0 = Math.max(1, Math.min(i0, n - 1));
        this.i = this.initialI0;
        this.s = n - this.i;
        this.r = 0.0;
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    /** Returns the current susceptible count. */
    public double getS() { return s; }

    /** Returns the current infected count. */
    public double getI() { return i; }

    /** Returns the current recovered count. */
    public double getR() { return r; }

    /** Returns the total population N (constant). */
    public double getN() { return n; }

    /**
     * Returns the conserved quantity S + I + R, which should equal N at all times.
     *
     * <p>In exact arithmetic {@code conserved() == N}; in floating-point the
     * deviation is on the order of machine epsilon times the magnitudes involved.
     *
     * @return S + I + R
     */
    public double conserved() {
        return s + i + r;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private double[] derivatives(double[] state, double beta, double gamma) {
        double sv = state[0];
        double iv = state[1];
        double infection = beta * sv * iv / n;
        double recovery  = gamma * iv;
        return new double[]{-infection, infection - recovery, recovery};
    }

    private static double[] scale(double[] v, double factor) {
        return new double[]{v[0] * factor, v[1] * factor, v[2] * factor};
    }

    private static double[] add(double[] a, double[] b) {
        return new double[]{a[0] + b[0], a[1] + b[1], a[2] + b[2]};
    }
}

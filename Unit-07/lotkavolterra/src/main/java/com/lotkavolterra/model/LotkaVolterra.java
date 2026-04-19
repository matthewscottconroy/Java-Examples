package com.lotkavolterra.model;

/**
 * The classical Lotka-Volterra predator-prey model.
 *
 * <h2>Equations of motion</h2>
 * <p>Let x be the prey population and y the predator population.
 * The coupled system of ODEs is:
 * <pre>
 *   dx/dt = α·x − β·x·y       (prey growth minus predation losses)
 *   dy/dt = δ·x·y − γ·y       (predator gains from prey minus natural death)
 * </pre>
 *
 * <h2>Parameters</h2>
 * <ul>
 *   <li>α (alpha) — intrinsic prey growth rate (per time unit)</li>
 *   <li>β (beta)  — predation rate coefficient (per predator per time unit)</li>
 *   <li>δ (delta) — predator reproduction rate per prey consumed</li>
 *   <li>γ (gamma) — intrinsic predator death rate (per time unit)</li>
 * </ul>
 *
 * <h2>Equilibrium</h2>
 * <p>The non-trivial fixed point is
 * {@code x* = γ/δ}, {@code y* = α/β}.
 * At this point both derivatives are zero and neither population changes.
 *
 * <h2>Conserved quantity</h2>
 * <p>The system possesses a conserved (Lyapunov) function that is constant
 * along every closed orbit:
 * <pre>  V = δ·x − γ·ln(x) + β·y − α·ln(y)</pre>
 * <p>Numerical drift in V indicates integration error.
 *
 * <h2>Harvesting extension</h2>
 * <p>An optional harvesting coefficient H subtracts H·x from prey growth,
 * reducing the effective prey birth rate to (α − H)·x.  Sufficient harvesting
 * drives the equilibrium prey population to zero, collapsing both species.
 *
 * <h2>Integration</h2>
 * <p>Uses the classical fourth-order Runge-Kutta (RK4) method with a fixed
 * time step of {@value #DT} per sub-step.  The caller should invoke
 * {@link #step()} once per animation frame, which internally runs
 * {@value #SUBSTEPS} sub-steps for accuracy.
 */
public class LotkaVolterra {

    /** Fixed RK4 time step (seconds per sub-step). */
    public static final double DT = 0.005;

    /** Number of RK4 sub-steps executed by each call to {@link #step()}. */
    public static final int SUBSTEPS = 8;

    /** Minimum population value; populations are clamped to this to avoid log(0). */
    private static final double MIN_POP = 0.001;

    // -------------------------------------------------------------------------
    // Parameters
    // -------------------------------------------------------------------------

    /** Prey intrinsic growth rate α (0.1 – 3.0). */
    private double alpha;

    /** Predation rate coefficient β (0.01 – 1.0). */
    private double beta;

    /** Predator reproduction rate per prey eaten δ (0.01 – 1.0). */
    private double delta;

    /** Predator natural death rate γ (0.1 – 3.0). */
    private double gamma;

    /** Harvesting coefficient H (0.0 – 0.5); subtracted from effective prey growth. */
    private double harvesting;

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    /** Current prey population x. */
    private double x;

    /** Current predator population y. */
    private double y;

    /** Prey population at reset (used by {@link #reset(double, double)}). */
    private double x0;

    /** Predator population at reset. */
    private double y0;

    /**
     * Construct the model with default parameters and given initial populations.
     *
     * <p>Default parameters: α = 1.0, β = 0.1, δ = 0.075, γ = 1.5.
     * These values produce a clearly visible cycle with the populations
     * remaining well-bounded over hundreds of time units.
     *
     * @param x0 initial prey population (must be &gt; 0)
     * @param y0 initial predator population (must be &gt; 0)
     */
    public LotkaVolterra(double x0, double y0) {
        this.alpha      = 1.0;
        this.beta       = 0.1;
        this.delta      = 0.075;
        this.gamma      = 1.5;
        this.harvesting = 0.0;
        reset(x0, y0);
    }

    // -------------------------------------------------------------------------
    // Integration
    // -------------------------------------------------------------------------

    /**
     * Advance the simulation by one animation frame ({@value #SUBSTEPS} RK4 sub-steps).
     *
     * <p>After each sub-step both populations are clamped to {@value #MIN_POP}
     * to guard against numerical underflow or negative values.
     */
    public void step() {
        for (int i = 0; i < SUBSTEPS; i++) {
            double[] next = rk4(new double[]{x, y}, DT);
            x = Math.max(MIN_POP, next[0]);
            y = Math.max(MIN_POP, next[1]);
        }
    }

    /**
     * Compute one RK4 step for the 2-component state {@code [x, y]}.
     *
     * @param state current state {@code [prey, predator]}
     * @param dt    time step (seconds)
     * @return new state after one RK4 step (input array is not mutated)
     */
    private double[] rk4(double[] state, double dt) {
        double[] k1 = derivatives(state);

        double[] s2 = {state[0] + 0.5 * dt * k1[0],
                       state[1] + 0.5 * dt * k1[1]};
        double[] k2 = derivatives(s2);

        double[] s3 = {state[0] + 0.5 * dt * k2[0],
                       state[1] + 0.5 * dt * k2[1]};
        double[] k3 = derivatives(s3);

        double[] s4 = {state[0] + dt * k3[0],
                       state[1] + dt * k3[1]};
        double[] k4 = derivatives(s4);

        return new double[]{
            state[0] + (dt / 6.0) * (k1[0] + 2.0 * k2[0] + 2.0 * k3[0] + k4[0]),
            state[1] + (dt / 6.0) * (k1[1] + 2.0 * k2[1] + 2.0 * k3[1] + k4[1])
        };
    }

    /**
     * Evaluate the Lotka-Volterra derivative function at the given state.
     *
     * <p>The returned array is {@code [dx/dt, dy/dt]}.  Populations are
     * clamped to {@value #MIN_POP} before evaluation to avoid arithmetic on
     * negative values produced by large step sizes.
     *
     * @param state {@code [x, y]} — prey and predator populations
     * @return derivative vector {@code [dx/dt, dy/dt]}
     */
    double[] derivatives(double[] state) {
        double px = Math.max(MIN_POP, state[0]);
        double py = Math.max(MIN_POP, state[1]);
        double dxdt = (alpha - harvesting) * px - beta * px * py;
        double dydt = delta * px * py - gamma * py;
        return new double[]{dxdt, dydt};
    }

    // -------------------------------------------------------------------------
    // Equilibrium and conserved quantity
    // -------------------------------------------------------------------------

    /**
     * Non-trivial equilibrium prey population: {@code x* = γ/δ}.
     *
     * @return equilibrium prey count
     */
    public double equilibriumX() {
        return gamma / delta;
    }

    /**
     * Non-trivial equilibrium predator population: {@code y* = α/β}.
     *
     * @return equilibrium predator count
     */
    public double equilibriumY() {
        return alpha / beta;
    }

    /**
     * Evaluate the conserved Lyapunov function at the current state.
     *
     * <p>The value {@code V = δ·x − γ·ln(x) + β·y − α·ln(y)} is constant
     * along any exact trajectory of the system.  Drift in V over time is a
     * measure of numerical integration error.
     *
     * @return current value of V
     */
    public double conservedQuantity() {
        return conservedQuantityAt(x, y);
    }

    /**
     * Evaluate the conserved Lyapunov function at an arbitrary point.
     *
     * @param px prey population (must be &gt; 0)
     * @param py predator population (must be &gt; 0)
     * @return value of V at (px, py)
     */
    public double conservedQuantityAt(double px, double py) {
        return delta * px - gamma * Math.log(px)
             + beta  * py - alpha * Math.log(py);
    }

    // -------------------------------------------------------------------------
    // Reset
    // -------------------------------------------------------------------------

    /**
     * Reset populations to the given initial values and restart the simulation.
     *
     * @param x0 initial prey population (clamped to {@value #MIN_POP} if &le; 0)
     * @param y0 initial predator population (clamped to {@value #MIN_POP} if &le; 0)
     */
    public void reset(double x0, double y0) {
        this.x0 = Math.max(MIN_POP, x0);
        this.y0 = Math.max(MIN_POP, y0);
        this.x  = this.x0;
        this.y  = this.y0;
    }

    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    /** Returns the current prey population x. */
    public double getX() { return x; }

    /** Returns the current predator population y. */
    public double getY() { return y; }

    /** Returns the initial prey population x0. */
    public double getX0() { return x0; }

    /** Returns the initial predator population y0. */
    public double getY0() { return y0; }

    /** Returns α, the prey intrinsic growth rate. */
    public double getAlpha() { return alpha; }

    /** Returns β, the predation rate coefficient. */
    public double getBeta() { return beta; }

    /** Returns δ, the predator reproduction rate per prey eaten. */
    public double getDelta() { return delta; }

    /** Returns γ, the predator natural death rate. */
    public double getGamma() { return gamma; }

    /** Returns H, the harvesting coefficient. */
    public double getHarvesting() { return harvesting; }

    /**
     * Set α, the prey intrinsic growth rate.
     *
     * @param alpha prey growth rate (typically 0.1 – 3.0)
     */
    public void setAlpha(double alpha) { this.alpha = alpha; }

    /**
     * Set β, the predation rate coefficient.
     *
     * @param beta predation coefficient (typically 0.01 – 1.0)
     */
    public void setBeta(double beta) { this.beta = beta; }

    /**
     * Set δ, the predator reproduction rate per prey consumed.
     *
     * @param delta predator growth per prey eaten (typically 0.01 – 1.0)
     */
    public void setDelta(double delta) { this.delta = delta; }

    /**
     * Set γ, the predator natural death rate.
     *
     * @param gamma predator death rate (typically 0.1 – 3.0)
     */
    public void setGamma(double gamma) { this.gamma = gamma; }

    /**
     * Set the harvesting coefficient H.
     *
     * <p>H is subtracted from the effective prey birth rate.  When
     * {@code H >= alpha} no prey can grow and both populations collapse.
     *
     * @param harvesting harvesting rate (typically 0.0 – 0.5)
     */
    public void setHarvesting(double harvesting) { this.harvesting = harvesting; }
}

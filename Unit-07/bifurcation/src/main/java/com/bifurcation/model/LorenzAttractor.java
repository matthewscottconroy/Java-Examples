package com.bifurcation.model;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * The Lorenz attractor: a three-dimensional continuous-time dynamical system
 * that exhibits the hallmark of deterministic chaos — the "butterfly" strange attractor.
 *
 * <h2>ODE System</h2>
 * <pre>
 *   dx/dt = σ(y − x)
 *   dy/dt = x(ρ − z) − y
 *   dz/dt = xy − βz
 * </pre>
 *
 * <h2>Default Parameters</h2>
 * <p>The classic chaotic regime uses σ=10, ρ=28, β=8/3. With these values the
 * trajectory never settles into a periodic orbit but instead traces an infinite,
 * non-repeating path on the two-winged strange attractor.</p>
 *
 * <h2>Sensitive Dependence</h2>
 * <p>This class maintains a second "shadow" trajectory starting from an initial
 * condition offset by 10⁻⁵. The two trajectories diverge exponentially,
 * demonstrating the sensitive dependence on initial conditions that is the
 * defining hallmark of chaos and the reason long-term weather prediction
 * is fundamentally limited.</p>
 *
 * <h2>Integration</h2>
 * <p>Uses the fourth-order Runge-Kutta (RK4) method, which provides much better
 * accuracy and energy conservation than the Euler method for the same step size.</p>
 */
public class LorenzAttractor {

    /** Maximum number of trail points retained for each trajectory. */
    public static final int MAX_TRAIL = 2500;

    /** Initial condition offset for the shadow trajectory (demonstrates sensitive dependence). */
    public static final double SHADOW_OFFSET = 1e-5;

    private double sigma;
    private double rho;
    private double beta;

    // Primary trajectory state
    private double x, y, z;

    // Shadow trajectory state (offset initial condition)
    private double sx, sy, sz;

    // Initial conditions (for reset)
    private final double x0, y0, z0;

    // Trails
    private final Deque<double[]> trail       = new ArrayDeque<>(MAX_TRAIL);
    private final Deque<double[]> shadowTrail = new ArrayDeque<>(MAX_TRAIL);

    /**
     * Construct a Lorenz attractor with the given parameters and initial state.
     *
     * @param x0    initial x coordinate
     * @param y0    initial y coordinate
     * @param z0    initial z coordinate
     * @param sigma σ parameter (rate of convection; classic = 10)
     * @param rho   ρ parameter (temperature difference; classic = 28)
     * @param beta  β parameter (geometric factor; classic = 8/3)
     */
    public LorenzAttractor(double x0, double y0, double z0,
                           double sigma, double rho, double beta) {
        this.x0    = x0;
        this.y0    = y0;
        this.z0    = z0;
        this.sigma = sigma;
        this.rho   = rho;
        this.beta  = beta;

        this.x  = x0;
        this.y  = y0;
        this.z  = z0;
        this.sx = x0 + SHADOW_OFFSET;
        this.sy = y0;
        this.sz = z0;
    }

    /**
     * Advance both trajectories by one time step using RK4.
     *
     * @param dt time step in seconds; recommend 0.005 for visual quality
     */
    public void step(double dt) {
        double[] state  = {x,  y,  z};
        double[] sstate = {sx, sy, sz};

        double[] next  = rk4(state,  dt);
        double[] snext = rk4(sstate, dt);

        x  = next[0];  y  = next[1];  z  = next[2];
        sx = snext[0]; sy = snext[1]; sz = snext[2];
    }

    /**
     * Record the current positions of both trajectories into their respective trails.
     *
     * <p>When a trail reaches {@link #MAX_TRAIL} entries the oldest entry is removed
     * to keep memory bounded.
     */
    public void recordTrail() {
        if (trail.size() >= MAX_TRAIL) trail.pollFirst();
        trail.addLast(new double[]{x, y, z});

        if (shadowTrail.size() >= MAX_TRAIL) shadowTrail.pollFirst();
        shadowTrail.addLast(new double[]{sx, sy, sz});
    }

    /**
     * Reset both trajectories to the original initial conditions.
     *
     * <p>Clears all trail history and restores the primary trajectory to
     * {@code (x0, y0, z0)} and the shadow trajectory to
     * {@code (x0 + SHADOW_OFFSET, y0, z0)}.
     */
    public void reset() {
        x  = x0;  y  = y0;  z  = z0;
        sx = x0 + SHADOW_OFFSET; sy = y0; sz = z0;
        trail.clear();
        shadowTrail.clear();
    }

    /**
     * Reset both trajectories to the specified initial conditions.
     *
     * @param x0 new initial x
     * @param y0 new initial y
     * @param z0 new initial z
     */
    public void reset(double x0, double y0, double z0) {
        this.x  = x0;  this.y  = y0;  this.z  = z0;
        this.sx = x0 + SHADOW_OFFSET; this.sy = y0; this.sz = z0;
        trail.clear();
        shadowTrail.clear();
    }

    /**
     * Return the absolute divergence between the two trajectories: {@code |x − sx|}.
     *
     * @return Euclidean distance between the primary and shadow x coordinates
     */
    public double divergence() {
        double dx = x - sx;
        double dy = y - sy;
        double dz = z - sz;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    // -------------------------------------------------------------------------
    // RK4 integration
    // -------------------------------------------------------------------------

    /**
     * Single RK4 step for the Lorenz system.
     *
     * @param state current [x, y, z]
     * @param dt    time step
     * @return      updated [x, y, z] after one step
     */
    private double[] rk4(double[] state, double dt) {
        double[] k1 = deriv(state);
        double[] k2 = deriv(add(state, scale(k1, dt / 2.0)));
        double[] k3 = deriv(add(state, scale(k2, dt / 2.0)));
        double[] k4 = deriv(add(state, scale(k3, dt)));

        return new double[]{
            state[0] + dt / 6.0 * (k1[0] + 2*k2[0] + 2*k3[0] + k4[0]),
            state[1] + dt / 6.0 * (k1[1] + 2*k2[1] + 2*k3[1] + k4[1]),
            state[2] + dt / 6.0 * (k1[2] + 2*k2[2] + 2*k3[2] + k4[2])
        };
    }

    /**
     * Lorenz system derivative: returns {@code [dx/dt, dy/dt, dz/dt]}.
     *
     * @param s state array [x, y, z]
     * @return  derivatives [σ(y−x), x(ρ−z)−y, xy−βz]
     */
    private double[] deriv(double[] s) {
        double px = s[0], py = s[1], pz = s[2];
        return new double[]{
            sigma * (py - px),
            px * (rho - pz) - py,
            px * py - beta * pz
        };
    }

    private double[] add(double[] a, double[] b) {
        return new double[]{a[0] + b[0], a[1] + b[1], a[2] + b[2]};
    }

    private double[] scale(double[] a, double s) {
        return new double[]{a[0] * s, a[1] * s, a[2] * s};
    }

    // -------------------------------------------------------------------------
    // Getters / setters
    // -------------------------------------------------------------------------

    /** Current x coordinate of the primary trajectory. */
    public double getX()  { return x; }

    /** Current y coordinate of the primary trajectory. */
    public double getY()  { return y; }

    /** Current z coordinate of the primary trajectory. */
    public double getZ()  { return z; }

    /** Current x coordinate of the shadow trajectory. */
    public double getShadowX() { return sx; }

    /** Current y coordinate of the shadow trajectory. */
    public double getShadowY() { return sy; }

    /** Current z coordinate of the shadow trajectory. */
    public double getShadowZ() { return sz; }

    /** Returns the σ (sigma) parameter (convection rate). */
    public double getSigma() { return sigma; }

    /** Returns the ρ (rho) parameter (temperature difference). */
    public double getRho()   { return rho; }

    /** Returns the β (beta) parameter (geometric factor). */
    public double getBeta()  { return beta; }

    /** Sets the σ (sigma) parameter. */
    public void setSigma(double sigma) { this.sigma = sigma; }

    /** Sets the ρ (rho) parameter. */
    public void setRho(double rho)     { this.rho   = rho; }

    /** Sets the β (beta) parameter. */
    public void setBeta(double beta)   { this.beta  = beta; }

    /** Returns the trail of the primary trajectory as a deque of [x, y, z] arrays. */
    public Deque<double[]> getTrail()       { return trail; }

    /** Returns the trail of the shadow trajectory as a deque of [x, y, z] arrays. */
    public Deque<double[]> getShadowTrail() { return shadowTrail; }
}

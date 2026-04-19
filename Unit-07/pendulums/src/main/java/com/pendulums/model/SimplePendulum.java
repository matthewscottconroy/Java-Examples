package com.pendulums.model;

import com.pendulums.physics.Integrator;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * A simple (single-bob) pendulum on a massless, rigid rod.
 *
 * <h2>Equation of motion</h2>
 * <p>The exact non-linear equation is
 * <pre>  d²θ/dt² = −(g/L) sin θ − c · dθ/dt</pre>
 * where θ is the angle from the downward vertical, L is the rod length,
 * g is gravitational acceleration, and c is an optional damping coefficient.
 *
 * <p>For small angles (θ ≪ 1 rad), sin θ ≈ θ, reducing the equation to
 * simple harmonic motion with period T = 2π√(L/g).  This class integrates
 * the <em>exact</em> equation so you can observe the deviation from SHM at
 * large amplitudes.</p>
 *
 * <h2>State</h2>
 * <ul>
 *   <li>θ — angle from the downward vertical (radians); positive = right</li>
 *   <li>ω — angular velocity (radians/second)</li>
 * </ul>
 *
 * <h2>Units</h2>
 * <p>All lengths are in pixels; time is in seconds.  A typical setup uses
 * {@code length ≈ 200 px} and {@code gravity ≈ 980 px/s²}, which is analogous
 * to a ~2-metre physical pendulum with standard Earth gravity.</p>
 */
public class SimplePendulum {

    /** Maximum number of (x, y) trail points retained. */
    public static final int MAX_TRAIL = 600;

    private double theta;
    private double omega;
    private double length;
    private double gravity;
    private double damping;
    private Integrator.Method method;

    private final Deque<double[]> trail = new ArrayDeque<>(MAX_TRAIL);

    /**
     * Construct a simple pendulum at the given release angle with no initial velocity.
     *
     * @param theta0  initial angle from vertical (radians)
     * @param length  rod length (pixels)
     * @param gravity gravitational acceleration (pixels/s²)
     * @param damping angular velocity damping coefficient (1/s; 0 = no damping)
     * @param method  numerical integration method
     */
    public SimplePendulum(double theta0, double length, double gravity,
                          double damping, Integrator.Method method) {
        this.theta   = theta0;
        this.omega   = 0.0;
        this.length  = length;
        this.gravity = gravity;
        this.damping = damping;
        this.method  = method;
    }

    /**
     * Advance the simulation by {@code dt} seconds using the configured integrator.
     *
     * @param dt time step (seconds); should be ≤ 0.005 for stable Euler, ≤ 0.02 for RK4
     */
    public void step(double dt) {
        Integrator.ODE ode = state -> new double[]{
            state[1],
            -(gravity / length) * Math.sin(state[0]) - damping * state[1]
        };
        double[] next = Integrator.step(new double[]{theta, omega}, ode, dt, method);
        theta = next[0];
        omega = next[1];
    }

    /**
     * Record the current bob position (relative to pivot) as a trail point.
     *
     * @param pivotX pivot x-coordinate on screen (pixels)
     * @param pivotY pivot y-coordinate on screen (pixels)
     */
    public void recordTrail(double pivotX, double pivotY) {
        if (trail.size() >= MAX_TRAIL) trail.pollFirst();
        trail.addLast(new double[]{pivotX + bobRelX(), pivotY + bobRelY()});
    }

    /** Clear the trail history and reset angle/velocity. */
    public void reset(double theta0) {
        this.theta = theta0;
        this.omega = 0.0;
        trail.clear();
    }

    // -------------------------------------------------------------------------
    // Kinematics
    // -------------------------------------------------------------------------

    /** X offset of the bob from the pivot (pixels). */
    public double bobRelX() { return length * Math.sin(theta); }

    /** Y offset of the bob below the pivot (pixels; positive = downward). */
    public double bobRelY() { return length * Math.cos(theta); }

    // -------------------------------------------------------------------------
    // Energy
    // -------------------------------------------------------------------------

    /**
     * Kinetic energy per unit mass: {@code ½ L² ω²} (px²/s²).
     *
     * <p>Kinetic energy will grow slowly under Euler integration (energy drift)
     * but should remain nearly constant under RK4.
     */
    public double kineticEnergy() {
        return 0.5 * length * length * omega * omega;
    }

    /**
     * Potential energy per unit mass relative to the equilibrium: {@code gL(1 − cos θ)} (px²/s²).
     */
    public double potentialEnergy() {
        return gravity * length * (1.0 - Math.cos(theta));
    }

    /** Total mechanical energy per unit mass (px²/s²). */
    public double totalEnergy() {
        return kineticEnergy() + potentialEnergy();
    }

    /**
     * Small-angle period approximation: {@code T = 2π√(L/g)} (seconds).
     *
     * <p>The true period is longer than this estimate when |θ₀| is large.
     * At 90° the true period is about 18 % longer; at 170° about 3× longer.
     */
    public double smallAnglePeriod() {
        return 2.0 * Math.PI * Math.sqrt(length / gravity);
    }

    // -------------------------------------------------------------------------
    // Getters / setters
    // -------------------------------------------------------------------------

    public double getTheta()                  { return theta; }
    public double getOmega()                  { return omega; }
    public double getLength()                 { return length; }
    public double getGravity()                { return gravity; }
    public double getDamping()                { return damping; }
    public Integrator.Method getMethod()      { return method; }
    public Deque<double[]> getTrail()         { return trail; }

    public void setLength(double length)      { this.length  = length; }
    public void setGravity(double gravity)    { this.gravity = gravity; }
    public void setDamping(double damping)    { this.damping = damping; }
    public void setMethod(Integrator.Method m) { this.method = m; }
    public void setTheta(double theta)        { this.theta   = theta; }
    public void setOmega(double omega)        { this.omega   = omega; }
}

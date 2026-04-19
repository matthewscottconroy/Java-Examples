package com.pendulums.model;

import com.pendulums.physics.Integrator;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * A double pendulum: two rods connected end-to-end, each carrying a point mass.
 *
 * <h2>Derivation</h2>
 * <p>The equations of motion are derived from the Lagrangian
 * {@code L = T − V} where T is total kinetic energy and V is total potential energy.
 * The coupled, non-linear result for the angular accelerations α₁ and α₂ is:</p>
 * <pre>
 *   let δ = θ₁ − θ₂,  M = m₁ + m₂
 *
 *   α₁ = [−g(2m₁+m₂)sinθ₁ − m₂g sin(θ₁−2θ₂) − 2sinδ·m₂(ω₂²L₂ + ω₁²L₁cosδ)]
 *          / [L₁(2m₁+m₂ − m₂cos2δ)]
 *
 *   α₂ = [2sinδ·(ω₁²L₁(m₁+m₂) + g(m₁+m₂)cosθ₁ + ω₂²L₂m₂cosδ)]
 *          / [L₂(2m₁+m₂ − m₂cos2δ)]
 * </pre>
 *
 * <h2>Chaos</h2>
 * <p>For large initial angles the system is chaotic: two trajectories starting
 * with angles differing by as little as 10⁻⁶ radians will diverge exponentially.
 * The Lyapunov exponent is positive for most high-energy initial conditions.
 * The double pendulum is a classic demonstration that deterministic systems can
 * produce behaviour that is, in practice, unpredictable.</p>
 *
 * <h2>State</h2>
 * <ul>
 *   <li>θ₁, θ₂ — angles from the downward vertical (radians)</li>
 *   <li>ω₁, ω₂ — angular velocities (radians/second)</li>
 * </ul>
 */
public class DoublePendulum {

    /** Maximum trail points for the second (end) bob. */
    public static final int MAX_TRAIL = 1200;

    private double theta1, omega1;
    private double theta2, omega2;
    private double length1, length2;
    private double mass1, mass2;
    private double gravity;

    private final Deque<double[]> trail = new ArrayDeque<>(MAX_TRAIL);

    /**
     * Construct a double pendulum at the given release angles with zero initial velocity.
     *
     * @param theta1  angle of first rod from vertical (radians)
     * @param theta2  angle of second rod from vertical (radians)
     * @param length1 first rod length (pixels)
     * @param length2 second rod length (pixels)
     * @param mass1   first bob mass (arbitrary units; only ratios matter)
     * @param mass2   second bob mass
     * @param gravity gravitational acceleration (pixels/s²)
     */
    public DoublePendulum(double theta1, double theta2,
                          double length1, double length2,
                          double mass1, double mass2,
                          double gravity) {
        this.theta1  = theta1;
        this.theta2  = theta2;
        this.omega1  = 0.0;
        this.omega2  = 0.0;
        this.length1 = length1;
        this.length2 = length2;
        this.mass1   = mass1;
        this.mass2   = mass2;
        this.gravity = gravity;
    }

    /**
     * Advance the simulation by {@code dt} seconds using RK4.
     *
     * <p>Always uses RK4 — Euler is numerically unstable for the double pendulum
     * at any practically useful step size.
     *
     * @param dt time step (seconds); recommend ≤ 0.005 for accurate trails
     */
    public void step(double dt) {
        double[] state = {theta1, omega1, theta2, omega2};
        double[] next  = Integrator.rk4(state, this::derivatives, dt);
        theta1 = next[0];
        omega1 = next[1];
        theta2 = next[2];
        omega2 = next[3];
    }

    /**
     * Derivative function for the 4-component state [θ₁, ω₁, θ₂, ω₂].
     *
     * <p>Implements the Lagrangian equations of motion described in the class doc.
     */
    private double[] derivatives(double[] s) {
        double t1 = s[0], o1 = s[1], t2 = s[2], o2 = s[3];
        double delta    = t1 - t2;
        double sinD     = Math.sin(delta);
        double cosD     = Math.cos(delta);
        double denom    = 2.0 * mass1 + mass2 - mass2 * Math.cos(2.0 * delta);

        double alpha1 = (-gravity * (2.0 * mass1 + mass2) * Math.sin(t1)
                        - mass2 * gravity * Math.sin(t1 - 2.0 * t2)
                        - 2.0 * sinD * mass2 * (o2 * o2 * length2 + o1 * o1 * length1 * cosD))
                       / (length1 * denom);

        double alpha2 = (2.0 * sinD * (o1 * o1 * length1 * (mass1 + mass2)
                        + gravity * (mass1 + mass2) * Math.cos(t1)
                        + mass2 * o2 * o2 * length2 * cosD))
                       / (length2 * denom);

        return new double[]{o1, alpha1, o2, alpha2};
    }

    /**
     * Record the end-bob position as a trail point.
     *
     * @param pivotX pivot x on screen (pixels)
     * @param pivotY pivot y on screen (pixels)
     */
    public void recordTrail(double pivotX, double pivotY) {
        if (trail.size() >= MAX_TRAIL) trail.pollFirst();
        trail.addLast(new double[]{
            pivotX + bob1RelX() + bob2RelX(),
            pivotY + bob1RelY() + bob2RelY()
        });
    }

    /** Reset to given angles with zero velocity. */
    public void reset(double theta1, double theta2) {
        this.theta1 = theta1;
        this.theta2 = theta2;
        this.omega1 = 0.0;
        this.omega2 = 0.0;
        trail.clear();
    }

    // -------------------------------------------------------------------------
    // Kinematics
    // -------------------------------------------------------------------------

    /** X offset of bob 1 from the pivot (pixels). */
    public double bob1RelX() { return length1 * Math.sin(theta1); }

    /** Y offset of bob 1 below the pivot (pixels). */
    public double bob1RelY() { return length1 * Math.cos(theta1); }

    /** X offset of bob 2 from bob 1 (pixels). */
    public double bob2RelX() { return length2 * Math.sin(theta2); }

    /** Y offset of bob 2 below bob 1 (pixels). */
    public double bob2RelY() { return length2 * Math.cos(theta2); }

    // -------------------------------------------------------------------------
    // Energy
    // -------------------------------------------------------------------------

    /**
     * Total kinetic energy of both bobs (per unit of mass1, in px²/s²).
     *
     * <p>KE = ½m₁L₁²ω₁² + ½m₂[L₁²ω₁² + L₂²ω₂² + 2L₁L₂ω₁ω₂cos(θ₁−θ₂)]
     */
    public double kineticEnergy() {
        double cosD = Math.cos(theta1 - theta2);
        return 0.5 * mass1 * length1 * length1 * omega1 * omega1
             + 0.5 * mass2 * (length1 * length1 * omega1 * omega1
                              + length2 * length2 * omega2 * omega2
                              + 2.0 * length1 * length2 * omega1 * omega2 * cosD);
    }

    /**
     * Total potential energy of both bobs relative to the pivot (px²/s²).
     *
     * <p>V = −(m₁+m₂)gL₁cosθ₁ − m₂gL₂cosθ₂
     */
    public double potentialEnergy() {
        return -(mass1 + mass2) * gravity * length1 * Math.cos(theta1)
               - mass2 * gravity * length2 * Math.cos(theta2);
    }

    /** Total mechanical energy (px²/s²). */
    public double totalEnergy() {
        return kineticEnergy() + potentialEnergy();
    }

    // -------------------------------------------------------------------------
    // Getters / setters
    // -------------------------------------------------------------------------

    public double getTheta1()              { return theta1; }
    public double getTheta2()              { return theta2; }
    public double getOmega1()              { return omega1; }
    public double getOmega2()              { return omega2; }
    public double getLength1()             { return length1; }
    public double getLength2()             { return length2; }
    public double getMass1()               { return mass1; }
    public double getMass2()               { return mass2; }
    public double getGravity()             { return gravity; }
    public Deque<double[]> getTrail()      { return trail; }

    public void setLength1(double v)       { this.length1 = v; }
    public void setLength2(double v)       { this.length2 = v; }
    public void setMass1(double v)         { this.mass1   = v; }
    public void setMass2(double v)         { this.mass2   = v; }
    public void setGravity(double v)       { this.gravity = v; }
    public void setTheta1(double v)        { this.theta1  = v; }
    public void setTheta2(double v)        { this.theta2  = v; }
}

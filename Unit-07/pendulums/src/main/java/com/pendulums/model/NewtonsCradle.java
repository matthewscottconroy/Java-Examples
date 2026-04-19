package com.pendulums.model;

import com.pendulums.physics.Integrator;

/**
 * Newton's Cradle: a row of identical pendulums whose bobs hang touching at rest.
 *
 * <h2>Physics</h2>
 * <p>Each ball is modelled as a point mass on a massless rod (simple pendulum).
 * When adjacent bobs collide the impulse is resolved using the 1-D elastic
 * collision formula for equal masses with a configurable coefficient of
 * restitution {@code e}:</p>
 * <pre>
 *   J = (1 + e) · v_rel / 2        (impulse per unit mass)
 *   v₁' = v₁ − J · n̂              (ball moving into collision)
 *   v₂' = v₂ + J · n̂              (ball receiving impulse)
 * </pre>
 * <p>where {@code v_rel = (v₁ − v₂) · n̂} is the relative approach speed along
 * the contact normal {@code n̂}.  The impulse is then projected onto each
 * pendulum's tangential direction to obtain the change in angular velocity.</p>
 *
 * <h2>Why does one ball out → one ball out?</h2>
 * <p>With {@code e = 1} and equal masses, the elastic collision solution
 * uniquely satisfies both conservation of momentum <em>and</em> conservation
 * of kinetic energy: the moving ball stops and the stationary ball inherits
 * the full velocity.  This propagates through the row as a chain of
 * pairwise collisions, producing the familiar cradle behaviour.  When
 * {@code e < 1} energy is lost at each collision and the motion damps out.
 *
 * <h2>Geometry</h2>
 * <p>Pivot centres are spaced exactly {@code 2 × ballRadius} apart so that
 * balls touch when all angles are zero.  The model stores angles and angular
 * velocities; the UI layer maps these to screen coordinates.
 */
public class NewtonsCradle {

    /** Maximum ball count supported. */
    public static final int MAX_BALLS = 9;

    /** Minimum ball count. */
    public static final int MIN_BALLS = 3;

    /** Spacing between adjacent pivot centres (pixels) = ball diameter. */
    public final double pivotSpacing;

    private final double length;
    private final double ballRadius;
    private double gravity;
    private double restitution;

    private int ballCount;
    private double[] theta;
    private double[] omega;

    /**
     * Construct a cradle with the given geometry and default 5 balls.
     *
     * @param length      rod length (pixels)
     * @param ballRadius  ball radius (pixels); pivot spacing = 2 × ballRadius
     * @param gravity     gravitational acceleration (pixels/s²)
     * @param restitution coefficient of restitution (0 = perfectly inelastic, 1 = elastic)
     */
    public NewtonsCradle(double length, double ballRadius, double gravity, double restitution) {
        this.length       = length;
        this.ballRadius   = ballRadius;
        this.pivotSpacing = 2.0 * ballRadius;
        this.gravity      = gravity;
        this.restitution  = restitution;
        setBallCount(5);
    }

    /**
     * Change the number of balls, resetting all angles to zero.
     *
     * @param count new ball count, clamped to [{@value #MIN_BALLS}, {@value #MAX_BALLS}]
     */
    public void setBallCount(int count) {
        ballCount = Math.max(MIN_BALLS, Math.min(MAX_BALLS, count));
        theta = new double[ballCount];
        omega = new double[ballCount];
    }

    /**
     * Lift the leftmost {@code liftCount} balls to {@code liftAngle} radians and release.
     *
     * @param liftCount number of balls to lift (from the left); clamped to [1, ballCount-1]
     * @param liftAngle release angle (radians; positive = pulled to the left = negative theta)
     */
    public void reset(int liftCount, double liftAngle) {
        int n = Math.max(1, Math.min(liftCount, ballCount - 1));
        for (int i = 0; i < ballCount; i++) {
            theta[i] = (i < n) ? -Math.abs(liftAngle) : 0.0;
            omega[i] = 0.0;
        }
    }

    /** Reset all balls to the vertical (at rest). */
    public void resetAll() {
        for (int i = 0; i < ballCount; i++) {
            theta[i] = 0.0;
            omega[i] = 0.0;
        }
    }

    /**
     * Advance the simulation by {@code dt} seconds.
     *
     * <p>Integrates each pendulum with RK4, then resolves collisions between
     * all adjacent pairs (repeated until no new collisions remain, up to
     * {@code ballCount} passes to handle cascade chains).
     *
     * @param dt time step (seconds); recommend ≤ 0.002 for accurate collision timing
     */
    public void step(double dt) {
        for (int i = 0; i < ballCount; i++) {
            final double t = theta[i];
            final double o = omega[i];
            Integrator.ODE ode = state -> new double[]{
                state[1],
                -(gravity / length) * Math.sin(state[0])
            };
            double[] next = Integrator.rk4(new double[]{t, o}, ode, dt);
            theta[i] = next[0];
            omega[i] = next[1];
        }

        // Multiple passes so a single swing can propagate through the whole row.
        for (int pass = 0; pass < ballCount; pass++) {
            boolean any = false;
            for (int i = 0; i < ballCount - 1; i++) {
                if (resolveCollision(i)) any = true;
            }
            if (!any) break;
        }
    }

    /**
     * Check whether balls {@code i} and {@code i+1} overlap and, if so, apply
     * an impulse to separate them.
     *
     * @return {@code true} if a collision was resolved
     */
    private boolean resolveCollision(int i) {
        // Positions relative to pivot[0] = (0, 0)
        double x1 = i * pivotSpacing       + length * Math.sin(theta[i]);
        double y1 =                           length * Math.cos(theta[i]);
        double x2 = (i + 1) * pivotSpacing + length * Math.sin(theta[i + 1]);
        double y2 =                           length * Math.cos(theta[i + 1]);

        double dx   = x2 - x1;
        double dy   = y2 - y1;
        double dist = Math.hypot(dx, dy);

        if (dist >= 2.0 * ballRadius - 1e-9) return false;

        // Contact normal from ball i toward ball i+1
        double nx = (dist < 1e-12) ? 1.0 : dx / dist;
        double ny = (dist < 1e-12) ? 0.0 : dy / dist;

        // Tangential linear velocities of each bob
        double v1x = length * omega[i]       * Math.cos(theta[i]);
        double v1y = -length * omega[i]      * Math.sin(theta[i]);
        double v2x = length * omega[i + 1]   * Math.cos(theta[i + 1]);
        double v2y = -length * omega[i + 1]  * Math.sin(theta[i + 1]);

        // Relative velocity along contact normal; positive = approaching
        double vRel = (v1x - v2x) * nx + (v1y - v2y) * ny;
        if (vRel <= 0.0) return false;

        // Impulse per unit mass for equal masses with restitution e
        double j = (1.0 + restitution) * vRel / 2.0;

        // Apply impulse to linear velocities, then project onto tangent to get Δω
        // Tangent for pendulum i: direction of increasing θ = (cosθ, −sinθ)
        double cos1 = Math.cos(theta[i]),     sin1 = Math.sin(theta[i]);
        double cos2 = Math.cos(theta[i + 1]), sin2 = Math.sin(theta[i + 1]);

        omega[i]     += (-j * nx * cos1 + j * ny * sin1) / length;
        omega[i + 1] += ( j * nx * cos2 - j * ny * sin2) / length;

        // Positional correction to prevent penetration from accumulating
        double overlap = 2.0 * ballRadius - dist;
        if (overlap > 0.0) {
            theta[i]     += (-nx * cos1 + ny * sin1) * (overlap * 0.5) / length;
            theta[i + 1] += ( nx * cos2 - ny * sin2) * (overlap * 0.5) / length;
        }

        return true;
    }

    // -------------------------------------------------------------------------
    // Kinematics helpers for the UI
    // -------------------------------------------------------------------------

    /**
     * X position of ball {@code i} on screen.
     *
     * @param pivotX0 x-coordinate of pivot[0] on screen (pixels)
     */
    public double ballScreenX(int i, double pivotX0) {
        return pivotX0 + i * pivotSpacing + length * Math.sin(theta[i]);
    }

    /**
     * Y position of ball {@code i} on screen.
     *
     * @param pivotY y-coordinate of all pivots on screen (pixels)
     */
    public double ballScreenY(int i, double pivotY) {
        return pivotY + length * Math.cos(theta[i]);
    }

    /**
     * X coordinate of pivot {@code i} on screen.
     *
     * @param pivotX0 x-coordinate of pivot[0] on screen (pixels)
     */
    public double pivotScreenX(int i, double pivotX0) {
        return pivotX0 + i * pivotSpacing;
    }

    // -------------------------------------------------------------------------
    // Energy
    // -------------------------------------------------------------------------

    /** Total kinetic energy of all balls (per unit mass, px²/s²). */
    public double kineticEnergy() {
        double ke = 0;
        for (int i = 0; i < ballCount; i++) {
            ke += 0.5 * length * length * omega[i] * omega[i];
        }
        return ke;
    }

    /** Total potential energy of all balls relative to equilibrium (per unit mass, px²/s²). */
    public double potentialEnergy() {
        double pe = 0;
        for (int i = 0; i < ballCount; i++) {
            pe += gravity * length * (1.0 - Math.cos(theta[i]));
        }
        return pe;
    }

    /** Total mechanical energy (per unit mass, px²/s²). */
    public double totalEnergy() { return kineticEnergy() + potentialEnergy(); }

    // -------------------------------------------------------------------------
    // Getters / setters
    // -------------------------------------------------------------------------

    public int    getBallCount()              { return ballCount; }
    public double getTheta(int i)             { return theta[i]; }
    public double getOmega(int i)             { return omega[i]; }
    public double getLength()                 { return length; }
    public double getBallRadius()             { return ballRadius; }
    public double getGravity()                { return gravity; }
    public double getRestitution()            { return restitution; }

    public void setGravity(double g)          { this.gravity     = g; }
    public void setRestitution(double e)      { this.restitution = Math.max(0, Math.min(1, e)); }
}

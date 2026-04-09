package com.bouncingballs.physics;

import com.bouncingballs.model.Ball;
import java.util.List;

/**
 * Discrete-time physics engine for the glass-jar simulator.
 *
 * <h2>Each time-step</h2>
 * <ol>
 *   <li>Apply gravity and the window-inertia impulse to every ball's velocity.</li>
 *   <li>Integrate positions with a simple Euler step.</li>
 *   <li>Resolve ball–wall collisions (reflect + restitution + floor friction).</li>
 *   <li>Resolve ball–ball collisions iteratively (impulse + positional correction).</li>
 * </ol>
 *
 * <h2>Window inertia</h2>
 * <p>When the jar (window) moves, every ball receives a velocity kick equal to
 * {@code −windowVelocity × INERTIA_FACTOR}.  Physically this models the inertia
 * of the ball resisting the jar's sudden change in reference frame.
 */
public final class PhysicsEngine {

    /** Fraction of window velocity transferred to balls (0 = no effect, 1 = rigid). */
    public static final double INERTIA_FACTOR  = 0.65;

    private static final double WALL_RESTITUTION = 0.70;
    private static final double BALL_RESTITUTION = 0.78;
    /** Rolling friction applied to vx whenever a ball rests on the floor. */
    private static final double FLOOR_FRICTION   = 0.980;
    /** Max iterations of the collision solver per step (improves stability). */
    private static final int    SOLVE_ITERS      = 5;
    /** Hard cap on ball speed (px/s) to prevent tunnelling on large impulses. */
    private static final double MAX_SPEED        = 4000.0;

    private PhysicsEngine() {}

    /**
     * Advance the simulation by {@code dt} seconds.
     *
     * @param balls       mutable list of all balls (modified in-place)
     * @param gravity     downward acceleration in px/s²
     * @param dt          time step in seconds
     * @param leftWall    inner-left boundary (pixels)
     * @param rightWall   inner-right boundary (pixels)
     * @param topWall     inner-top boundary (pixels)
     * @param bottomWall  inner-bottom boundary (pixels)
     * @param windowDvx   window velocity x (px/s) — used for inertia kick
     * @param windowDvy   window velocity y (px/s) — used for inertia kick
     */
    public static void step(List<Ball> balls, double gravity, double dt,
                            double leftWall,  double rightWall,
                            double topWall,   double bottomWall,
                            double windowDvx, double windowDvy) {
        // 1. Forces → velocity
        for (Ball b : balls) {
            b.vy += gravity * dt;
            // Inertia: jar accelerating right → balls appear to lag left
            b.vx -= windowDvx * INERTIA_FACTOR;
            b.vy -= windowDvy * INERTIA_FACTOR;

            double spd = Math.sqrt(b.vx * b.vx + b.vy * b.vy);
            if (spd > MAX_SPEED) {
                double scale = MAX_SPEED / spd;
                b.vx *= scale;
                b.vy *= scale;
            }
        }

        // 2. Integrate positions
        for (Ball b : balls) {
            b.x += b.vx * dt;
            b.y += b.vy * dt;
        }

        // 3. Wall collisions
        for (Ball b : balls) {
            if (b.x - b.radius < leftWall) {
                b.x = leftWall + b.radius;
                if (b.vx < 0) b.vx = -b.vx * WALL_RESTITUTION;
            }
            if (b.x + b.radius > rightWall) {
                b.x = rightWall - b.radius;
                if (b.vx > 0) b.vx = -b.vx * WALL_RESTITUTION;
            }
            if (b.y - b.radius < topWall) {
                b.y = topWall + b.radius;
                if (b.vy < 0) b.vy = -b.vy * WALL_RESTITUTION;
            }
            if (b.y + b.radius > bottomWall) {
                b.y = bottomWall - b.radius;
                if (b.vy > 0) b.vy = -b.vy * WALL_RESTITUTION;
                b.vx *= FLOOR_FRICTION;
            }
        }

        // 4. Ball–ball collisions (multiple iterations for stack stability)
        int n = balls.size();
        for (int iter = 0; iter < SOLVE_ITERS; iter++) {
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    resolveCollision(balls.get(i), balls.get(j));
                }
            }
        }
    }

    // ── Pairwise collision ────────────────────────────────────────────────────

    /**
     * Resolves a pairwise elastic collision between two balls using impulse
     * resolution and positional correction.
     *
     * <p>Algorithm:
     * <ol>
     *   <li>Compute the collision normal from centre-to-centre vector.</li>
     *   <li>Push the balls apart along that normal, weighted by inverse mass,
     *       so the heavier ball moves less.</li>
     *   <li>Compute the relative velocity along the collision normal.</li>
     *   <li>If the balls are already separating ({@code dvn ≤ 0}), do nothing.</li>
     *   <li>Apply an impulse {@code j = −(1+e)·dvn / (1/m_a + 1/m_b)} scaled by
     *       {@link #BALL_RESTITUTION}.</li>
     * </ol>
     */
    private static void resolveCollision(Ball a, Ball b) {
        double dx     = b.x - a.x;
        double dy     = b.y - a.y;
        double distSq = dx * dx + dy * dy;
        double minDist = a.radius + b.radius;

        if (distSq >= minDist * minDist || distSq < 1e-9) return;

        double dist = Math.sqrt(distSq);
        double nx   = dx / dist;
        double ny   = dy / dist;

        // Positional correction: push balls apart weighted by inverse mass
        double overlap    = minDist - dist;
        double totalMass  = a.mass + b.mass;
        a.x -= nx * overlap * (b.mass / totalMass);
        a.y -= ny * overlap * (b.mass / totalMass);
        b.x += nx * overlap * (a.mass / totalMass);
        b.y += ny * overlap * (a.mass / totalMass);

        // Velocity resolution along collision normal
        double dvx = a.vx - b.vx;
        double dvy = a.vy - b.vy;
        double dvn = dvx * nx + dvy * ny;
        if (dvn <= 0) return;  // already separating

        double e = BALL_RESTITUTION;
        double j = -(1.0 + e) * dvn / (1.0 / a.mass + 1.0 / b.mass);

        a.vx += j * nx / a.mass;
        a.vy += j * ny / a.mass;
        b.vx -= j * nx / b.mass;
        b.vy -= j * ny / b.mass;
    }
}

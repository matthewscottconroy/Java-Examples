package com.boids.model;

/**
 * A single agent in the Boids flocking simulation.
 *
 * <h2>Representation</h2>
 * <p>Each boid is a point mass moving in 2-D Euclidean space.  Its state
 * consists of a position {@code (x, y)} and a velocity {@code (vx, vy)}.
 * All coordinates are in pixels; velocity is in pixels per second.</p>
 *
 * <h2>Steering</h2>
 * <p>The caller computes a steering force {@code (fx, fy)} by combining the
 * three Reynolds rules (separation, alignment, cohesion) and passes it to
 * {@link #applyForce}.  The force is added to the velocity, which is then
 * clamped to {@code maxSpeed}.  Position is updated with the new velocity
 * scaled by the time step {@code dt}.</p>
 *
 * <h2>Boundary</h2>
 * <p>The world uses toroidal (wrap-around) boundaries.  Calling
 * {@link #wrap(double, double)} after each step keeps the boid inside
 * the canvas rectangle.</p>
 */
public class Boid {

    /** X position in world space (pixels). */
    double x;

    /** Y position in world space (pixels). */
    double y;

    /** X component of velocity (pixels/second). */
    double vx;

    /** Y component of velocity (pixels/second). */
    double vy;

    /**
     * Construct a boid with the given initial state.
     *
     * @param x  initial x position (pixels)
     * @param y  initial y position (pixels)
     * @param vx initial x velocity (pixels/second)
     * @param vy initial y velocity (pixels/second)
     */
    public Boid(double x, double y, double vx, double vy) {
        this.x  = x;
        this.y  = y;
        this.vx = vx;
        this.vy = vy;
    }

    /**
     * Apply a steering force to the boid, updating velocity and position.
     *
     * <p>The steering force is first clamped so its magnitude does not exceed
     * {@code maxForce}.  It is then added to the current velocity, which is
     * subsequently clamped to {@code maxSpeed}.  Finally, the position is
     * advanced by {@code velocity × dt}.</p>
     *
     * @param fx       x component of the steering force (pixels/s²)
     * @param fy       y component of the steering force (pixels/s²)
     * @param maxForce maximum magnitude of the steering force (pixels/s²)
     * @param maxSpeed maximum speed the boid may reach (pixels/second)
     * @param dt       time step (seconds)
     */
    public void applyForce(double fx, double fy, double maxForce, double maxSpeed, double dt) {
        // Clamp steering force
        double fMag = Math.sqrt(fx * fx + fy * fy);
        if (fMag > maxForce && fMag > 0) {
            fx = fx / fMag * maxForce;
            fy = fy / fMag * maxForce;
        }

        // Integrate velocity
        vx += fx * dt;
        vy += fy * dt;

        // Clamp speed
        double speed = Math.sqrt(vx * vx + vy * vy);
        if (speed > maxSpeed && speed > 0) {
            vx = vx / speed * maxSpeed;
            vy = vy / speed * maxSpeed;
        }

        // Update position
        x += vx * dt;
        y += vy * dt;
    }

    /**
     * Wrap the boid's position to the toroidal world boundaries.
     *
     * <p>If the boid exits the left edge it reappears at the right edge,
     * and vice versa.  The same applies to the top and bottom edges.</p>
     *
     * @param width  world width (pixels)
     * @param height world height (pixels)
     */
    public void wrap(double width, double height) {
        if (x < 0)      x += width;
        if (x > width)  x -= width;
        if (y < 0)      y += height;
        if (y > height) y -= height;
    }

    /**
     * Return the heading angle of this boid in radians.
     *
     * <p>The angle is measured from the positive x-axis using
     * {@link Math#atan2}, so the result lies in {@code (−π, π]}.</p>
     *
     * @return heading angle (radians)
     */
    public double heading() {
        return Math.atan2(vy, vx);
    }

    /**
     * Return the current speed of this boid (pixels/second).
     *
     * @return speed (pixels/second; always ≥ 0)
     */
    public double speed() {
        return Math.sqrt(vx * vx + vy * vy);
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /** @return x position (pixels) */
    public double getX() { return x; }

    /** @return y position (pixels) */
    public double getY() { return y; }

    /** @return x velocity component (pixels/second) */
    public double getVx() { return vx; }

    /** @return y velocity component (pixels/second) */
    public double getVy() { return vy; }
}

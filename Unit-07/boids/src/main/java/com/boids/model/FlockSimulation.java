package com.boids.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Reynolds' Boids flocking simulation (1987).
 *
 * <h2>Model</h2>
 * <p>The flock is a collection of {@link Boid} agents navigating a toroidal
 * 2-D world of dimensions {@code width × height}.  At each time step every
 * boid evaluates three local steering rules against its neighbours:
 *
 * <ol>
 *   <li><b>Separation</b> — steer away from neighbours within
 *       {@link #separationRadius}.  The force is the normalised sum of
 *       displacement vectors from each neighbour, weighted by the inverse of
 *       their distance.</li>
 *   <li><b>Alignment</b> — steer toward the average velocity of neighbours
 *       within {@link #perceptionRadius}.  The force is the difference between
 *       that average and the boid's own velocity.</li>
 *   <li><b>Cohesion</b> — steer toward the average position of neighbours
 *       within {@link #perceptionRadius}.  The force is the vector from the
 *       boid's position to that average.</li>
 * </ol>
 *
 * <p>Each rule produces a force that is normalised and scaled by its weight.
 * The three weighted forces are summed to form a steering acceleration, which
 * is clamped to {@link #maxForce} and applied via
 * {@link Boid#applyForce(double, double, double, double, double)}.
 *
 * <h2>Predator</h2>
 * <p>An optional predator position may be set via {@link #setPredator}.  When
 * present, every boid within 150 px adds a strong separation-like repulsion
 * force directed away from the predator.  The weight of this force is
 * {@code 5 × separationWeight}.</p>
 *
 * <h2>Complexity</h2>
 * <p>The naive O(N²) neighbour search is used.  For N ≤ 300 boids this is
 * comfortably real-time on modern hardware.</p>
 */
public class FlockSimulation {

    // -------------------------------------------------------------------------
    // Default parameter constants
    // -------------------------------------------------------------------------

    /** Default number of boids on reset. */
    public static final int    DEFAULT_BOID_COUNT       = 80;

    /** Default perception radius for alignment and cohesion (pixels). */
    public static final double DEFAULT_PERCEPTION_RADIUS = 80.0;

    /** Default separation radius — must be smaller than perception radius (pixels). */
    public static final double DEFAULT_SEPARATION_RADIUS = 25.0;

    /** Default separation steering weight. */
    public static final double DEFAULT_SEPARATION_WEIGHT = 1.5;

    /** Default alignment steering weight. */
    public static final double DEFAULT_ALIGNMENT_WEIGHT  = 1.0;

    /** Default cohesion steering weight. */
    public static final double DEFAULT_COHESION_WEIGHT   = 1.0;

    /** Default maximum speed (pixels/second). */
    public static final double DEFAULT_MAX_SPEED         = 120.0;

    /** Default maximum steering force magnitude (pixels/s²). */
    public static final double DEFAULT_MAX_FORCE         = 200.0;

    /** Predator influence radius (pixels). */
    private static final double PREDATOR_RADIUS          = 150.0;

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    private final List<Boid> boids = new ArrayList<>();
    private final Random     rng   = new Random();

    private final double width;
    private final double height;

    // -------------------------------------------------------------------------
    // Simulation parameters (mutable via setters)
    // -------------------------------------------------------------------------

    private int    boidCount        = DEFAULT_BOID_COUNT;
    private double perceptionRadius = DEFAULT_PERCEPTION_RADIUS;
    private double separationRadius = DEFAULT_SEPARATION_RADIUS;
    private double separationWeight = DEFAULT_SEPARATION_WEIGHT;
    private double alignmentWeight  = DEFAULT_ALIGNMENT_WEIGHT;
    private double cohesionWeight   = DEFAULT_COHESION_WEIGHT;
    private double maxSpeed         = DEFAULT_MAX_SPEED;
    private double maxForce         = DEFAULT_MAX_FORCE;

    /** Predator position, or {@code null} when no predator is active. */
    private double[] predator = null;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Create a new flock simulation on a world of the given dimensions and
     * spawn the default number of boids at random positions.
     *
     * @param width  world width (pixels)
     * @param height world height (pixels)
     */
    public FlockSimulation(double width, double height) {
        this.width  = width;
        this.height = height;
        reset(boidCount);
    }

    // -------------------------------------------------------------------------
    // Simulation step
    // -------------------------------------------------------------------------

    /**
     * Advance the simulation by one time step.
     *
     * <p>For each boid, the three steering forces are computed by scanning all
     * other boids.  The weighted sum is applied as a steering acceleration.
     * Positions are then wrapped to the toroidal boundary.</p>
     *
     * @param dt time step in seconds (typically 0.016 / substeps)
     */
    public void step(double dt) {
        int n = boids.size();
        if (n == 0) return;

        // Pre-compute steering for each boid before updating any positions
        double[] steerX = new double[n];
        double[] steerY = new double[n];

        for (int i = 0; i < n; i++) {
            Boid b = boids.get(i);

            // Accumulators for alignment and cohesion
            double avgVx = 0, avgVy = 0;
            double avgPx = 0, avgPy = 0;
            int    percCount = 0;

            // Accumulators for separation
            double sepX = 0, sepY = 0;
            int    sepCount = 0;

            for (int j = 0; j < n; j++) {
                if (i == j) continue;
                Boid other = boids.get(j);

                double dx   = b.x - other.x;
                double dy   = b.y - other.y;
                double dist = Math.sqrt(dx * dx + dy * dy);

                // Separation — only within separationRadius
                if (dist < separationRadius && dist > 0) {
                    // Weight by inverse distance for tight avoidance
                    sepX += (dx / dist) / dist;
                    sepY += (dy / dist) / dist;
                    sepCount++;
                }

                // Alignment and cohesion — within perceptionRadius
                if (dist < perceptionRadius) {
                    avgVx += other.vx;
                    avgVy += other.vy;
                    avgPx += other.x;
                    avgPy += other.y;
                    percCount++;
                }
            }

            double fx = 0, fy = 0;

            // --- Separation ---
            if (sepCount > 0) {
                double mag = Math.sqrt(sepX * sepX + sepY * sepY);
                if (mag > 0) {
                    sepX /= mag;
                    sepY /= mag;
                }
                fx += sepX * separationWeight;
                fy += sepY * separationWeight;
            }

            // --- Alignment ---
            if (percCount > 0) {
                avgVx /= percCount;
                avgVy /= percCount;
                double alX  = avgVx - b.vx;
                double alY  = avgVy - b.vy;
                double alMag = Math.sqrt(alX * alX + alY * alY);
                if (alMag > 0) {
                    alX /= alMag;
                    alY /= alMag;
                }
                fx += alX * alignmentWeight;
                fy += alY * alignmentWeight;

                // --- Cohesion ---
                avgPx /= percCount;
                avgPy /= percCount;
                double coX   = avgPx - b.x;
                double coY   = avgPy - b.y;
                double coMag = Math.sqrt(coX * coX + coY * coY);
                if (coMag > 0) {
                    coX /= coMag;
                    coY /= coMag;
                }
                fx += coX * cohesionWeight;
                fy += coY * cohesionWeight;
            }

            // --- Predator repulsion ---
            if (predator != null) {
                double pdx  = b.x - predator[0];
                double pdy  = b.y - predator[1];
                double dist = Math.sqrt(pdx * pdx + pdy * pdy);
                if (dist < PREDATOR_RADIUS && dist > 0) {
                    double pForce = (5.0 * separationWeight);
                    fx += (pdx / dist) * pForce;
                    fy += (pdy / dist) * pForce;
                }
            }

            steerX[i] = fx;
            steerY[i] = fy;
        }

        // Apply steering and wrap
        for (int i = 0; i < n; i++) {
            boids.get(i).applyForce(steerX[i], steerY[i], maxForce, maxSpeed, dt);
            boids.get(i).wrap(width, height);
        }
    }

    // -------------------------------------------------------------------------
    // Flock management
    // -------------------------------------------------------------------------

    /**
     * Remove all existing boids and spawn {@code count} new boids at random
     * positions with random initial velocities.
     *
     * @param count number of boids to create (must be ≥ 0)
     */
    public void reset(int count) {
        boids.clear();
        this.boidCount = count;
        for (int i = 0; i < count; i++) {
            double x   = rng.nextDouble() * width;
            double y   = rng.nextDouble() * height;
            double ang = rng.nextDouble() * 2 * Math.PI;
            double spd = maxSpeed * (0.5 + rng.nextDouble() * 0.5);
            boids.add(new Boid(x, y, Math.cos(ang) * spd, Math.sin(ang) * spd));
        }
    }

    /**
     * Add a single boid at the specified position with a random initial velocity.
     *
     * @param x x position (pixels)
     * @param y y position (pixels)
     */
    public void addBoid(double x, double y) {
        double ang = rng.nextDouble() * 2 * Math.PI;
        double spd = maxSpeed * (0.5 + rng.nextDouble() * 0.5);
        boids.add(new Boid(x, y, Math.cos(ang) * spd, Math.sin(ang) * spd));
        this.boidCount = boids.size();
    }

    // -------------------------------------------------------------------------
    // Predator
    // -------------------------------------------------------------------------

    /**
     * Place the predator at the given world position.
     *
     * @param x x position (pixels)
     * @param y y position (pixels)
     */
    public void setPredator(double x, double y) {
        predator = new double[]{x, y};
    }

    /** Remove the predator from the simulation. */
    public void clearPredator() {
        predator = null;
    }

    /**
     * Return the current predator position, or {@code null} if none is active.
     *
     * @return {@code double[]{x, y}} or {@code null}
     */
    public double[] getPredator() {
        return predator;
    }

    // -------------------------------------------------------------------------
    // Getters / setters
    // -------------------------------------------------------------------------

    /** @return an unmodifiable view of the boid list */
    public List<Boid> getBoids() { return Collections.unmodifiableList(boids); }

    /** @return world width (pixels) */
    public double getWidth()  { return width; }

    /** @return world height (pixels) */
    public double getHeight() { return height; }

    /** @return current boid count (may differ from list size after manual adds) */
    public int getBoidCount() { return boidCount; }

    public double getPerceptionRadius() { return perceptionRadius; }
    public double getSeparationRadius() { return separationRadius; }
    public double getSeparationWeight() { return separationWeight; }
    public double getAlignmentWeight()  { return alignmentWeight; }
    public double getCohesionWeight()   { return cohesionWeight; }
    public double getMaxSpeed()         { return maxSpeed; }
    public double getMaxForce()         { return maxForce; }

    public void setPerceptionRadius(double r) { this.perceptionRadius = r; }
    public void setSeparationRadius(double r) { this.separationRadius = r; }
    public void setSeparationWeight(double w) { this.separationWeight = w; }
    public void setAlignmentWeight(double w)  { this.alignmentWeight  = w; }
    public void setCohesionWeight(double w)   { this.cohesionWeight   = w; }
    public void setMaxSpeed(double s)         { this.maxSpeed = s; }
    public void setMaxForce(double f)         { this.maxForce = f; }
    public void setBoidCount(int count)       { this.boidCount = count; }
}

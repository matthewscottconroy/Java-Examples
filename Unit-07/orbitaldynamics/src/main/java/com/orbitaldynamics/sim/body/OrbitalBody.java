package com.orbitaldynamics.sim.body;

import com.orbitaldynamics.math.Vector2D;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * A single celestial body in the N-body simulation.
 *
 * <h2>State</h2>
 * <ul>
 *   <li>Position and velocity in simulation units (pixels at default zoom)</li>
 *   <li>Orientation angle θ and angular velocity ω (radians, radians/second)</li>
 *   <li>Mass = density × π × radius² (2D area-based model)</li>
 * </ul>
 *
 * <h2>Angular Momentum</h2>
 * <p>Angular velocity is set at creation (derived from the mouse throw direction).
 * It is conserved: no external torques act on the body. The body spins at constant ω.
 */
public final class OrbitalBody {

    private static long nextId = 0;

    public static final int  MAX_TRAIL    = 500;
    private static final int TRAIL_PERIOD = 3;   // record trail every N physics steps

    private final long   id;
    private       String name;
    private Vector2D position;
    private Vector2D velocity;
    private double   radius;    // simulation units (= screen pixels at zoom=1)
    private double   density;   // arbitrary units; default 1.0
    private double   angle;     // radians
    private double   omega;     // radians/second (angular velocity)
    private boolean  selected;
    private boolean  pinned;    // if true, body stays fixed (mass effectively infinite for gravity)

    private final BodyTexture texture;

    // Orbit trail (world-space positions)
    private final Deque<Vector2D> trail = new ArrayDeque<>();
    private int trailCounter = 0;

    public OrbitalBody(Vector2D position, Vector2D velocity,
                       double radius, double density,
                       double angle, double omega) {
        this.id       = nextId++;
        this.name     = "Body " + id;
        this.position = position;
        this.velocity = velocity;
        this.radius   = radius;
        this.density  = density;
        this.angle    = angle;
        this.omega    = omega;
        this.texture  = BodyTexture.generate(id * 7919L + 42L);
    }

    // -------------------------------------------------------------------------
    // Physics properties
    // -------------------------------------------------------------------------

    /** Mass = density × π × r² */
    public double getMass() { return density * Math.PI * radius * radius; }

    public Vector2D getPosition()     { return position; }
    public Vector2D getVelocity()     { return velocity; }
    public double   getRadius()       { return radius; }
    public double   getDensity()      { return density; }
    public double   getAngle()        { return angle; }
    public double   getOmega()        { return omega; }

    public void setPosition(Vector2D p) { this.position = p; }
    public void setVelocity(Vector2D v) { this.velocity = v; }
    public void setRadius(double r)     { this.radius = r; }
    public void setDensity(double d)    { this.density = d; }
    public void setAngle(double a)      { this.angle = a % (2 * Math.PI); }
    public void setOmega(double w)      { this.omega = w; }

    // -------------------------------------------------------------------------
    // Identification
    // -------------------------------------------------------------------------

    public long   getId()          { return id; }
    public String getName()        { return name; }
    public void   setName(String n){ this.name = n; }

    public boolean isSelected()         { return selected; }
    public void    setSelected(boolean s){ this.selected = s; }
    public boolean isPinned()           { return pinned; }
    public void    setPinned(boolean p) { this.pinned = p; }

    // -------------------------------------------------------------------------
    // Trail
    // -------------------------------------------------------------------------

    /** Called each physics step; records trail point every TRAIL_PERIOD steps. */
    public void tickTrail() {
        if (++trailCounter >= TRAIL_PERIOD) {
            trailCounter = 0;
            trail.addLast(position);
            while (trail.size() > MAX_TRAIL) trail.pollFirst();
        }
    }

    /** Returns the trail positions in order from oldest to newest. */
    public Deque<Vector2D> getTrail() { return trail; }

    public void clearTrail() { trail.clear(); }

    // -------------------------------------------------------------------------
    // Texture / rendering
    // -------------------------------------------------------------------------

    public BodyTexture getTexture() { return texture; }

    public Color getColor() { return texture.getBaseColor(); }

    // -------------------------------------------------------------------------
    // Kinetic energy
    // -------------------------------------------------------------------------

    public double kineticEnergy() {
        double m = getMass();
        return 0.5 * m * velocity.magnitudeSq() + 0.5 * (0.5 * m * radius * radius) * omega * omega;
    }

    @Override
    public String toString() {
        return String.format("%s [r=%.0f ρ=%.1f m=%.0f pos=%s vel=%s ω=%.2f]",
            name, radius, density, getMass(), position, velocity, omega);
    }
}

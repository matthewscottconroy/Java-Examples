package com.bouncingballs.model;

import java.awt.Color;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A circular ball that lives inside the glass jar.
 *
 * <p>Mass uses a 2-D analogue: {@code mass = density × π × radius²}, so a
 * denser or larger ball is proportionally heavier and harder to deflect.
 *
 * <p>All position and velocity values are in screen pixels / pixels-per-second.
 */
public class Ball {

    private static final AtomicInteger ID_GEN = new AtomicInteger(1);

    public final int    id;
    public double       x, y;          // centre position (pixels)
    public double       vx, vy;        // velocity (pixels/s)
    public final double radius;        // pixels
    public final double density;       // relative (light ≈ 0.3, medium ≈ 1.0, heavy ≈ 3.5)
    public final double mass;          // density × π × radius²
    public final double restitution;   // coefficient of restitution [0, 1]
    public final Color  color;

    /** Density presets — expressed as human-readable categories. */
    public enum Density {
        LIGHT(0.30),
        MEDIUM(1.00),
        HEAVY(3.50);

        public final double value;
        Density(double v) { this.value = v; }
    }

    /** Size presets in pixels radius. */
    public enum Size {
        SMALL(11, 15),
        MEDIUM(17, 24),
        LARGE(27, 38);

        public final int min, max;
        Size(int min, int max) { this.min = min; this.max = max; }

        /** Pick a random radius within this size category. */
        public double randomRadius(java.util.Random rng) {
            return min + rng.nextInt(max - min + 1);
        }
    }

    public Ball(double x, double y, double radius, double density, Color color) {
        this.id          = ID_GEN.getAndIncrement();
        this.x           = x;
        this.y           = y;
        this.vx          = 0;
        this.vy          = 0;
        this.radius      = radius;
        this.density     = density;
        this.mass        = density * Math.PI * radius * radius;
        this.restitution = 0.76;
        this.color       = color;
    }

    /** True if the screen point (px, py) lies within this ball's circle. */
    public boolean contains(double px, double py) {
        double dx = px - x, dy = py - y;
        return dx * dx + dy * dy <= radius * radius;
    }
}

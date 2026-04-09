package com.projectiles;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A single pixel-sized debris particle ejected when terrain explodes.
 *
 * <h2>Spawn mix</h2>
 * <p>Particles are created in two groups by {@link #spawn}:
 * <ul>
 *   <li><b>Fountain (65 %)</b> — angle sampled in ±50° of straight up,
 *       speed 80–400 px/s.  Creates the upward geyser effect.</li>
 *   <li><b>Radial  (35 %)</b> — angle sampled over the full 360°,
 *       speed 40–240 px/s.  Scatters fragments outward.</li>
 * </ul>
 *
 * <h2>Lifecycle</h2>
 * <p>Each particle has a random lifetime of 0.6–1.8 s.  Alpha fades in during
 * the first half of the lifetime and the particle is removed when
 * {@code life &le; 0} or when it exits the canvas.
 *
 * <h2>Bounce</h2>
 * <p>Each particle bounces at most once off solid terrain with a 35 %
 * velocity-damping coefficient.  After the bounce it continues until its
 * lifetime expires.
 *
 * @see Terrain#explode(int, int, int)
 */
public final class Debris {

    private static final double DEBRIS_GRAVITY = 520.0;   // px/s²
    private static final double BOUNCE_DAMP    = 0.35;    // velocity retained on bounce
    private static final Random RNG            = new Random();

    // ── State ─────────────────────────────────────────────────────────────────
    private double x, y;
    private double vx, vy;
    private final Color  color;
    private final int    size;         // 1 or 2 px
    private double life;               // seconds remaining
    private final double maxLife;
    private boolean bounced = false;
    private boolean dead    = false;

    // -------------------------------------------------------------------------

    private Debris(double x, double y, double vx, double vy,
                   Color color, int size, double life) {
        this.x       = x;
        this.y       = y;
        this.vx      = vx;
        this.vy      = vy;
        this.color   = color;
        this.size    = size;
        this.life    = life;
        this.maxLife = life;
    }

    // -------------------------------------------------------------------------
    // Factory
    // -------------------------------------------------------------------------

    /**
     * Spawns debris from an explosion at (cx, cy) using the destroyed pixel
     * colour list returned by {@link Terrain#explode}.
     *
     * @param destroyedColors colours of removed pixels — each is used as a
     *                        particle colour; excess colours are sampled randomly
     * @param cx              explosion centre X
     * @param cy              explosion centre Y
     * @param count           total number of particles to create
     */
    public static List<Debris> spawn(List<Color> destroyedColors,
                                     int cx, int cy, int count) {
        List<Debris> list = new ArrayList<>(count);
        int fountainCount = (int) (count * 0.65);

        for (int i = 0; i < count; i++) {
            Color c = destroyedColors.isEmpty()
                ? new Color(120, 80, 40)
                : destroyedColors.get(RNG.nextInt(destroyedColors.size()));

            double vx, vy;
            if (i < fountainCount) {
                // Fountain: mostly upward, ±50° from vertical
                double angle = -Math.PI / 2.0 + (RNG.nextDouble() - 0.5) * Math.toRadians(100);
                double speed = 80 + RNG.nextDouble() * 320;
                vx = Math.cos(angle) * speed;
                vy = Math.sin(angle) * speed;
            } else {
                // Radial: full 360°
                double angle = RNG.nextDouble() * 2 * Math.PI;
                double speed = 40 + RNG.nextDouble() * 200;
                vx = Math.cos(angle) * speed;
                vy = Math.sin(angle) * speed;
            }

            // Slight offset from centre
            double ox = (RNG.nextDouble() - 0.5) * 20;
            double oy = (RNG.nextDouble() - 0.5) * 20;

            int    size = RNG.nextBoolean() ? 1 : 2;
            double life = 0.6 + RNG.nextDouble() * 1.2;

            list.add(new Debris(cx + ox, cy + oy, vx, vy, c, size, life));
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    /** @return {@code true} if this particle should be removed */
    public boolean update(double dt, Terrain terrain) {
        if (dead) return true;

        life -= dt;
        if (life <= 0) { dead = true; return true; }

        vy += DEBRIS_GRAVITY * dt;
        x  += vx * dt;
        y  += vy * dt;

        // Single bounce off terrain
        if (!bounced && terrain.isSolid((int) x, (int) y)) {
            vy = -vy * BOUNCE_DAMP;
            vx =  vx * BOUNCE_DAMP;
            bounced = true;
            // push above surface
            y -= 2;
        }

        // Remove when off screen or settled
        if (y > terrain.getHeight() + 20
                || x < -20 || x > terrain.getWidth() + 20) {
            dead = true;
        }

        return dead;
    }

    // -------------------------------------------------------------------------
    // Rendering
    // -------------------------------------------------------------------------

    public void draw(Graphics2D g) {
        if (dead) return;
        float alpha = (float) Math.min(1.0, life / (maxLife * 0.5));
        int   a     = Math.max(0, Math.min(255, (int) (alpha * 220)));
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), a));
        g.fillRect((int) x, (int) y, size, size);
    }
}

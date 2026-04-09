package com.projectiles;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * A slingshot-launched projectile with parabolic physics and a fade-out trail.
 *
 * <h2>Physics</h2>
 * <p>Uses first-order Euler integration each frame:
 * <pre>
 *   vy += gravity * dt;
 *   x  += vx * dt;
 *   y  += vy * dt;
 * </pre>
 * This is fast and sufficient for the small time steps used by the game loop
 * (typically dt ≈ 1/60 s).
 *
 * <h2>Collision detection</h2>
 * <p>Nine probe points are checked each frame: the centre plus eight points
 * evenly distributed on the circumference at radius {@value #RADIUS} px.
 * On any hit the projectile deactivates and {@link #update} returns {@code true},
 * signalling the game loop to trigger an explosion.
 *
 * <h2>Trajectory preview</h2>
 * <p>The static {@link #preview} method simulates forward without mutating any
 * state and without a live {@link Projectile} instance, so it is safe to call
 * every frame during the aiming phase.
 *
 * @see Terrain#isSolid(int, int)
 * @see Debris#spawn
 */
public final class Projectile {

    public static final int RADIUS = 10;

    private static final int   TRAIL_MAX    = 60;
    private static final Color STONE_OUTER  = new Color(90,  90, 100);
    private static final Color STONE_INNER  = new Color(140, 140, 155);
    private static final Color TRAIL_COLOR  = new Color(200, 200, 220, 120);

    // ── State ─────────────────────────────────────────────────────────────────
    private double x, y;
    private double vx, vy;
    private boolean active = true;

    private final Deque<Point> trail = new ArrayDeque<>();

    // ── Circumference sample offsets ──────────────────────────────────────────
    private static final int[][] PROBE;
    static {
        PROBE = new int[9][2];
        PROBE[0] = new int[]{0, 0};
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4.0;
            PROBE[i + 1] = new int[]{
                (int) Math.round(Math.cos(angle) * RADIUS),
                (int) Math.round(Math.sin(angle) * RADIUS)
            };
        }
    }

    // -------------------------------------------------------------------------

    public Projectile(double x, double y, double vx, double vy) {
        this.x  = x;
        this.y  = y;
        this.vx = vx;
        this.vy = vy;
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    /**
     * Advances the projectile by one frame.
     *
     * @param dt      seconds per frame
     * @param gravity pixels/s² (positive = downward)
     * @param terrain used for hit detection
     * @return {@code true} if the projectile hit terrain this frame
     */
    public boolean update(double dt, double gravity, Terrain terrain) {
        if (!active) return false;

        // Record trail position before moving
        trail.addLast(new Point((int) x, (int) y));
        if (trail.size() > TRAIL_MAX) trail.removeFirst();

        // Integrate
        vy += gravity * dt;
        x  += vx * dt;
        y  += vy * dt;

        // Out of bounds → deactivate silently
        if (x < -50 || x > terrain.getWidth() + 50
                || y > terrain.getHeight() + 50) {
            active = false;
            return false;
        }

        // Collision check
        for (int[] probe : PROBE) {
            if (terrain.isSolid((int) x + probe[0], (int) y + probe[1])) {
                active = false;
                return true;
            }
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Rendering
    // -------------------------------------------------------------------------

    public void draw(Graphics2D g) {
        // Trail
        int n = trail.size();
        int i = 0;
        for (Point p : trail) {
            float frac  = i / (float) Math.max(1, n - 1);
            int   alpha = (int) (frac * 110);
            int   sz    = Math.max(1, (int) (frac * 5));
            g.setColor(new Color(200, 200, 220, alpha));
            g.fillOval(p.x - sz / 2, p.y - sz / 2, sz, sz);
            i++;
        }

        if (!active) return;

        // Shadow
        g.setColor(new Color(0, 0, 0, 60));
        g.fill(new Ellipse2D.Double(x - RADIUS + 3, y - RADIUS + 3,
                                   RADIUS * 2, RADIUS * 2));

        // Body
        g.setColor(STONE_OUTER);
        g.fill(new Ellipse2D.Double(x - RADIUS, y - RADIUS, RADIUS * 2, RADIUS * 2));

        // Highlight
        g.setColor(STONE_INNER);
        int hl = RADIUS / 2;
        g.fill(new Ellipse2D.Double(x - hl + 1, y - hl - 1, hl + 2, hl + 2));
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public double getX()     { return x; }
    public double getY()     { return y; }
    public boolean isActive(){ return active; }

    /**
     * Preview trajectory points — simulates forward without side effects.
     *
     * @param steps  number of steps to simulate
     * @param dt     time step used per step
     * @param gravity gravity in px/s²
     * @return array of [x, y] pairs
     */
    public static double[][] preview(double sx, double sy,
                                     double vx0, double vy0,
                                     int steps, double dt, double gravity,
                                     Terrain terrain) {
        double[][] pts = new double[steps][2];
        double px = sx, py = sy, pvx = vx0, pvy = vy0;
        for (int i = 0; i < steps; i++) {
            pvy += gravity * dt;
            px  += pvx * dt;
            py  += pvy * dt;
            pts[i][0] = px;
            pts[i][1] = py;
            // Stop preview at terrain
            if (terrain.isSolid((int) px, (int) py)) {
                // fill rest with same point so line ends cleanly
                for (int j = i + 1; j < steps; j++) {
                    pts[j][0] = px;
                    pts[j][1] = py;
                }
                break;
            }
        }
        return pts;
    }
}

package com.projectiles;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Pixel-based destructible terrain stored as a {@link BufferedImage}.
 *
 * <h2>Dual-purpose image</h2>
 * <p>Every pixel is either fully transparent (air, alpha = 0) or fully opaque
 * (solid, alpha = 255).  This makes {@link #isSolid(int, int)} a single
 * alpha-channel read — O(1) and safe to call every frame from physics code.
 *
 * <h2>Layer colours (depth below surface)</h2>
 * <table border="1">
 *   <caption>Terrain layers</caption>
 *   <tr><th>Depth</th><th>Layer</th><th>Palette</th></tr>
 *   <tr><td>0–6 px</td><td>Grass</td><td>dark greens</td></tr>
 *   <tr><td>7–50 px</td><td>Dirt</td><td>earthy browns</td></tr>
 *   <tr><td>51+ px</td><td>Rock</td><td>greys</td></tr>
 * </table>
 *
 * <h2>Surface generation</h2>
 * <p>The skyline is built from three octaves of sine noise, clamped to the
 * range [80, H−20] to keep the terrain fully on-screen.
 *
 * <h2>Explosions</h2>
 * <p>{@link #explode(int, int, int)} carves a filled circle (sets pixels to
 * transparent), adds a scorched rim 4 px wide around the blast, and returns
 * the list of destroyed pixel colours so the caller can use them to tint
 * {@link Debris} particles.
 *
 * @see Projectile
 * @see Debris
 */
public final class Terrain {

    // ── Layer colours ─────────────────────────────────────────────────────────
    private static final Color[] GRASS = {
        new Color(34,  139, 34),
        new Color(50,  160, 50),
        new Color(28,  120, 28),
    };
    private static final Color[] DIRT = {
        new Color(139,  90,  43),
        new Color(160, 110,  55),
        new Color(120,  75,  35),
        new Color(100,  60,  25),
        new Color(145,  95,  50),
    };
    private static final Color[] ROCK = {
        new Color(100, 100, 110),
        new Color(115, 115, 125),
        new Color( 85,  85,  95),
        new Color(130, 128, 138),
    };
    private static final Color SCORCH = new Color(20, 15, 10, 200);

    private static final int GRASS_DEPTH = 6;
    private static final int DIRT_DEPTH  = 50;

    private final BufferedImage img;
    private final int           W, H;
    private final Random        rng = new Random();

    /** Y-coordinate of the terrain surface at each X column. */
    private final int[] surfaceY;

    // -------------------------------------------------------------------------

    /**
     * Creates and generates a new terrain.
     *
     * @param width  canvas width in pixels
     * @param height canvas height in pixels
     */
    public Terrain(int width, int height) {
        this.W   = width;
        this.H   = height;
        this.img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        this.surfaceY = new int[width];
        generate();
    }

    // -------------------------------------------------------------------------
    // Generation
    // -------------------------------------------------------------------------

    private void generate() {
        Graphics2D g = img.createGraphics();
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, W, H);
        g.setComposite(AlphaComposite.SrcOver);

        // Build a smooth hilly surface using two octaves of sine noise
        for (int x = 0; x < W; x++) {
            double nx  = x / (double) W;
            double h1  = 0.55 + 0.12 * Math.sin(nx * Math.PI * 2.5 + 0.4);
            double h2  = 0.04 * Math.sin(nx * Math.PI * 9.0  + 1.1);
            double h3  = 0.015 * Math.sin(nx * Math.PI * 22.0 + 2.7);
            int    surf = (int) ((h1 + h2 + h3) * H);
            surf = Math.max(80, Math.min(H - 20, surf));
            surfaceY[x] = surf;
        }

        // Paint pixels column by column
        for (int x = 0; x < W; x++) {
            int surf = surfaceY[x];
            for (int y = surf; y < H; y++) {
                int depth = y - surf;
                Color c;
                if (depth <= GRASS_DEPTH) {
                    c = GRASS[rng.nextInt(GRASS.length)];
                } else if (depth <= DIRT_DEPTH) {
                    c = DIRT[rng.nextInt(DIRT.length)];
                } else {
                    c = ROCK[rng.nextInt(ROCK.length)];
                }
                img.setRGB(x, y, c.getRGB() | 0xFF000000);
            }
        }

        g.dispose();
    }

    // -------------------------------------------------------------------------
    // Collision
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if the pixel at (x, y) is solid ground.
     *
     * <p>Out-of-bounds coordinates return {@code false} so callers do not need
     * to guard against edge cases during collision sweeps.
     *
     * @param x pixel column
     * @param y pixel row
     * @return true if the pixel is opaque (solid), false if transparent (air)
     *         or out of bounds
     */
    public boolean isSolid(int x, int y) {
        if (x < 0 || x >= W || y < 0 || y >= H) return false;
        return (img.getRGB(x, y) >>> 24) > 0;
    }

    // -------------------------------------------------------------------------
    // Explosion
    // -------------------------------------------------------------------------

    /**
     * Carves a circular hole of {@code radius} pixels centred at (cx, cy),
     * then burns a 4-pixel scorched rim around the edge.
     *
     * <p>Each solid pixel inside the circle is set to transparent (air).
     * Pixels in the annular rim [radius, radius+4) are blended toward a dark
     * scorch colour at 50% opacity to simulate heat damage.
     *
     * @param cx     explosion centre X in pixels
     * @param cy     explosion centre Y in pixels
     * @param radius blast radius in pixels
     * @return list of {@link Color} values of every destroyed pixel, suitable
     *         for passing to {@link Debris#spawn} as particle colours;
     *         empty if the explosion hit only air
     */
    public List<Color> explode(int cx, int cy, int radius) {
        List<Color> destroyed = new ArrayList<>();
        int r2 = radius * radius;

        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                if (dx * dx + dy * dy > r2) continue;
                int px = cx + dx, py = cy + dy;
                if (px < 0 || px >= W || py < 0 || py >= H) continue;
                int argb = img.getRGB(px, py);
                if ((argb >>> 24) == 0) continue;   // already air
                destroyed.add(new Color(argb, true));
                img.setRGB(px, py, 0x00000000);     // clear to air
            }
        }

        // Scorched rim (darkened halo, partial transparency)
        int rimOuter = radius + 4;
        int rimInner = radius;
        int rimOuter2 = rimOuter * rimOuter;
        int rimInner2 = rimInner * rimInner;
        for (int dy = -rimOuter; dy <= rimOuter; dy++) {
            for (int dx = -rimOuter; dx <= rimOuter; dx++) {
                int dist2 = dx * dx + dy * dy;
                if (dist2 < rimInner2 || dist2 > rimOuter2) continue;
                int px = cx + dx, py = cy + dy;
                if (px < 0 || px >= W || py < 0 || py >= H) continue;
                int argb = img.getRGB(px, py);
                if ((argb >>> 24) == 0) continue;
                // Blend pixel toward scorch colour
                Color orig  = new Color(argb, false);
                Color mixed = blend(orig, SCORCH, 0.5f);
                img.setRGB(px, py, mixed.getRGB() | 0xFF000000);
            }
        }

        return destroyed;
    }

    private static Color blend(Color base, Color over, float alpha) {
        int r = (int) (base.getRed()   * (1 - alpha) + over.getRed()   * alpha);
        int g = (int) (base.getGreen() * (1 - alpha) + over.getGreen() * alpha);
        int b = (int) (base.getBlue()  * (1 - alpha) + over.getBlue()  * alpha);
        return new Color(
            Math.max(0, Math.min(255, r)),
            Math.max(0, Math.min(255, g)),
            Math.max(0, Math.min(255, b)));
    }

    // -------------------------------------------------------------------------
    // Rendering
    // -------------------------------------------------------------------------

    /** Draws the terrain image onto {@code g}. */
    public void draw(Graphics2D g) {
        g.drawImage(img, 0, 0, null);
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public int getWidth()  { return W; }
    public int getHeight() { return H; }

    /** Y coordinate of the surface at column x (for slingshot placement). */
    public int getSurfaceY(int x) {
        int cx = Math.max(0, Math.min(W - 1, x));
        return surfaceY[cx];
    }
}

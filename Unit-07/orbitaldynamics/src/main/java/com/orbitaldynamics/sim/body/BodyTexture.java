package com.orbitaldynamics.sim.body;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Procedurally generated asymmetric body texture.
 *
 * <p>Each body gets a seed-based collection of "continent" blobs rendered in a
 * rotated coordinate frame so that rotation of the body is visually apparent.
 * One distinctive "marking" (like Jupiter's Great Red Spot) ensures the rotation
 * is unambiguous even at low angular velocities.
 */
public final class BodyTexture {

    private record Patch(double relX, double relY, double relRadius, Color color) {}

    private final Color baseColor;
    private final List<Patch> patches;
    private final Color rimColor;

    private BodyTexture(Color base, Color rim, List<Patch> patches) {
        this.baseColor = base;
        this.rimColor  = rim;
        this.patches   = List.copyOf(patches);
    }

    /**
     * Generates a texture for a body with the given seed (body ID) and radius.
     * The radius is used only for choosing patch sizes.
     */
    public static BodyTexture generate(long seed) {
        Random rng = new Random(seed);

        // Base color: saturated, medium brightness
        float hue        = rng.nextFloat();
        float saturation = 0.55f + rng.nextFloat() * 0.30f;
        float brightness = 0.55f + rng.nextFloat() * 0.30f;
        Color base       = Color.getHSBColor(hue, saturation, brightness);

        // Rim (slightly darker/desaturated edge)
        Color rim = base.darker();

        List<Patch> patches = new ArrayList<>();

        // 4-7 "continent" blobs: variations on the base hue
        int numBlobs = 4 + rng.nextInt(4);
        for (int i = 0; i < numBlobs; i++) {
            double angle  = rng.nextDouble() * 2 * Math.PI;
            double dist   = rng.nextDouble() * 0.75;  // relative distance from center
            double relX   = Math.cos(angle) * dist;
            double relY   = Math.sin(angle) * dist;
            double relR   = 0.20 + rng.nextDouble() * 0.35;

            // Vary hue slightly, brightness up or down
            float h2 = (hue + (rng.nextFloat() - 0.5f) * 0.15f + 1f) % 1f;
            float s2 = Math.min(1f, saturation + (rng.nextFloat() - 0.5f) * 0.3f);
            float b2 = Math.min(1f, brightness + (rng.nextFloat() - 0.5f) * 0.4f);
            Color blobColor = Color.getHSBColor(h2, s2, b2);

            patches.add(new Patch(relX, relY, relR, blobColor));
        }

        // One distinctive "marking" — contrasting color, offset to one side
        float markHue = (hue + 0.5f) % 1f;  // complementary hue
        Color markColor = Color.getHSBColor(markHue, 0.9f, 0.85f);
        double markAngle = rng.nextDouble() * 2 * Math.PI;
        patches.add(new Patch(
            Math.cos(markAngle) * 0.5,
            Math.sin(markAngle) * 0.5,
            0.15 + rng.nextDouble() * 0.12,
            markColor
        ));

        return new BodyTexture(base, rim, patches);
    }

    /**
     * Paints the body onto g at screen position (cx, cy) with the given screen radius
     * and rotation angle (radians). Does not modify the Graphics2D state permanently.
     */
    public void paint(Graphics2D g, int cx, int cy, int screenRadius, double angle) {
        if (screenRadius < 1) return;

        AffineTransform saved = g.getTransform();
        Shape savedClip = g.getClip();

        g.translate(cx, cy);
        g.rotate(angle);

        // Clip all painting to the body circle
        Ellipse2D bodyCircle = new Ellipse2D.Double(
            -screenRadius, -screenRadius, 2 * screenRadius, 2 * screenRadius);
        g.setClip(bodyCircle);

        // Fill base color
        g.setColor(baseColor);
        g.fill(bodyCircle);

        // Draw blobs
        for (Patch p : patches) {
            int px = (int) (p.relX() * screenRadius);
            int py = (int) (p.relY() * screenRadius);
            int pr = (int) (p.relRadius() * screenRadius);
            if (pr < 1) continue;
            g.setColor(p.color());
            g.fillOval(px - pr, py - pr, 2 * pr, 2 * pr);
        }

        // Subtle darker rim (limb darkening)
        if (screenRadius >= 4) {
            g.setClip(savedClip);
            g.clip(bodyCircle);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int rimW = Math.max(1, screenRadius / 6);
            g.setColor(new Color(0, 0, 0, 60));
            g.setStroke(new BasicStroke(rimW * 2));
            g.draw(bodyCircle);
        }

        g.setTransform(saved);
        g.setClip(savedClip);
    }

    /** The body's base color (used for trail rendering). */
    public Color getBaseColor() { return baseColor; }
}

package com.orbitaldynamics.sim.ui;

import com.orbitaldynamics.math.Vector2D;

import java.awt.*;

/**
 * 2D camera supporting pan and zoom.
 *
 * <p>World coordinates are simulation units (pixels at zoom=1).
 * Screen coordinates are Java2D pixel coordinates.
 *
 * <p>Transform: screen = world * scale + offset
 */
public final class Camera {

    private double offsetX;
    private double offsetY;
    private double scale = 1.0;

    private static final double MIN_SCALE = 0.05;
    private static final double MAX_SCALE = 20.0;
    private static final double ZOOM_FACTOR = 1.12;

    /** Initializes the camera centered on (0,0) at the panel center. */
    public void centerOn(int panelWidth, int panelHeight) {
        offsetX = panelWidth  / 2.0;
        offsetY = panelHeight / 2.0;
    }

    // -------------------------------------------------------------------------
    // Transforms
    // -------------------------------------------------------------------------

    /** Converts a world position to a screen point. */
    public Point worldToScreen(Vector2D world) {
        return new Point(
            (int) Math.round(world.x() * scale + offsetX),
            (int) Math.round(world.y() * scale + offsetY)
        );
    }

    /** Converts a screen point to a world position. */
    public Vector2D screenToWorld(int sx, int sy) {
        return new Vector2D(
            (sx - offsetX) / scale,
            (sy - offsetY) / scale
        );
    }

    /** Converts a world-space radius to a screen-space radius (always positive). */
    public int worldToScreenRadius(double worldRadius) {
        return Math.max(1, (int) Math.round(worldRadius * scale));
    }

    /** Converts a screen-space distance to a world-space distance. */
    public double screenToWorldDist(double screenDist) {
        return screenDist / scale;
    }

    // -------------------------------------------------------------------------
    // Pan
    // -------------------------------------------------------------------------

    public void pan(int dx, int dy) {
        offsetX += dx;
        offsetY += dy;
    }

    // -------------------------------------------------------------------------
    // Zoom
    // -------------------------------------------------------------------------

    /**
     * Zooms in (scrollAmount < 0) or out (scrollAmount > 0) around the
     * screen point (sx, sy).
     */
    public void zoom(int scrollAmount, int sx, int sy) {
        double factor = scrollAmount < 0 ? ZOOM_FACTOR : 1.0 / ZOOM_FACTOR;
        double newScale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, scale * factor));
        if (newScale == scale) return;

        // Zoom around (sx, sy): world point under cursor must not move
        // world = (screen - offset) / scale  →  offset = screen - world * scale
        Vector2D worldUnderCursor = screenToWorld(sx, sy);
        scale = newScale;
        offsetX = sx - worldUnderCursor.x() * scale;
        offsetY = sy - worldUnderCursor.y() * scale;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public double getScale()    { return scale; }
    public double getOffsetX()  { return offsetX; }
    public double getOffsetY()  { return offsetY; }

    public void setScale(double s)    { this.scale   = Math.max(MIN_SCALE, Math.min(MAX_SCALE, s)); }
    public void setOffset(double x, double y) { offsetX = x; offsetY = y; }

    /** Applies this camera transform to a Graphics2D context. */
    public void applyTransform(Graphics2D g, int panelWidth, int panelHeight) {
        g.translate(offsetX, offsetY);
        g.scale(scale, scale);
    }
}

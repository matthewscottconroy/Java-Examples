package com.buoyancy.equation.model;

import java.awt.Color;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An upright cylindrical object that can float, sink, or reach equilibrium in a fluid.
 *
 * <h2>Coordinate system</h2>
 * <p>Position is tracked in screen pixels: {@code cx} is the horizontal centre,
 * {@code y} is the screen y of the top face (y increases downward).
 * Physical quantities (radius, height, density, force) use SI units.
 *
 * <h2>Pixel ↔ metre conversion</h2>
 * <p>{@value #PPM} pixels = 1 metre.
 */
public class BuoyancyObject {

    /** Pixels per metre conversion factor. */
    public static final double PPM = 100.0;

    private static final AtomicInteger ID_GEN = new AtomicInteger(1);

    // ── Identity ──────────────────────────────────────────────────────────────
    private final int id;
    private String    name;
    private final Color color;

    // ── Position (screen pixels, y-down) ─────────────────────────────────────
    private double cx;   // horizontal centre
    private double y;    // top face y

    // ── Physical properties (SI) ──────────────────────────────────────────────
    private double radiusM;       // metres
    private double heightM;       // metres
    private double densityKgM3;   // kg/m³

    // ── Dynamics ──────────────────────────────────────────────────────────────
    private double vy = 0.0;   // vertical velocity m/s; positive = moving downward

    // ── UI flags ──────────────────────────────────────────────────────────────
    private boolean selected = false;
    private boolean pinned   = false;   // true while user is dragging

    public BuoyancyObject(double cx, double y, double radiusM, double heightM,
                          double densityKgM3, String name, Color color) {
        this.id           = ID_GEN.getAndIncrement();
        this.cx           = cx;
        this.y            = y;
        this.radiusM      = radiusM;
        this.heightM      = heightM;
        this.densityKgM3  = densityKgM3;
        this.name         = name;
        this.color        = color;
    }

    // ── Derived geometry ──────────────────────────────────────────────────────

    /** Volume of the full cylinder in m³. */
    public double getVolume()   { return Math.PI * radiusM * radiusM * heightM; }

    /** Mass in kg. */
    public double getMass()     { return densityKgM3 * getVolume(); }

    /** Radius in pixels (minimum 8). */
    public int getRadiusPx()    { return Math.max(8, (int) Math.round(radiusM * PPM)); }

    /** Pixel width (= 2 × radiusPx). */
    public int getWidthPx()     { return 2 * getRadiusPx(); }

    /** Height in pixels (minimum 6). */
    public int getHeightPx()    { return Math.max(6, (int) Math.round(heightM * PPM)); }

    /** Screen y of the bottom face. */
    public double getBottomY()  { return y + getHeightPx(); }

    /** Screen x of the left edge. */
    public double getLeftX()    { return cx - getRadiusPx(); }

    /**
     * Returns {@code true} if screen point (px, py) falls within (or near)
     * the cylinder's bounding box.
     */
    public boolean contains(int px, int py) {
        int margin = 6;
        return px >= cx - getRadiusPx() - margin && px <= cx + getRadiusPx() + margin
            && py >= y - margin && py <= getBottomY() + margin;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public int     getId()           { return id; }
    public String  getName()         { return name; }
    public Color   getColor()        { return color; }
    public double  getCx()           { return cx; }
    public double  getY()            { return y; }
    public double  getRadiusM()      { return radiusM; }
    public double  getHeightM()      { return heightM; }
    public double  getDensityKgM3()  { return densityKgM3; }
    public double  getVy()           { return vy; }
    public boolean isSelected()      { return selected; }
    public boolean isPinned()        { return pinned; }

    // ── Setters ───────────────────────────────────────────────────────────────

    public void setName(String n)          { this.name = n; }
    public void setCx(double cx)           { this.cx = cx; }
    public void setY(double y)             { this.y = y; }
    public void setRadiusM(double r)       { this.radiusM = Math.max(0.05, r); }
    public void setHeightM(double h)       { this.heightM = Math.max(0.05, h); }
    public void setDensityKgM3(double d)   { this.densityKgM3 = Math.max(10.0, d); }
    public void setVy(double vy)           { this.vy = vy; }
    public void setSelected(boolean s)     { this.selected = s; }
    public void setPinned(boolean p)       { this.pinned = p; }
}

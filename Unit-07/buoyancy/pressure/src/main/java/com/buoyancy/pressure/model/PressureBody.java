package com.buoyancy.pressure.model;

import java.awt.Color;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An upright cylinder immersed in fluid, used by the pressure simulator.
 *
 * <p>Identical coordinate conventions to the equation program:
 * screen y increases downward, {@value #PPM} pixels = 1 metre.
 *
 * <p>The pressure simulator can optionally run live physics on this body
 * (toggle controlled by the UI).
 */
public class PressureBody {

    public static final double PPM = 100.0;

    private static final AtomicInteger ID_GEN = new AtomicInteger(1);

    private final int   id;
    private String      name;
    private final Color color;

    // Position (pixels, y-down)
    private double cx;
    private double y;      // top face

    // Physical properties (SI)
    private double radiusM;
    private double heightM;
    private double densityKgM3;

    // Dynamics (for optional physics mode)
    private double vy       = 0.0;
    private boolean pinned  = false;
    private boolean selected= false;

    public PressureBody(double cx, double y, double radiusM, double heightM,
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

    public double getVolume()   { return Math.PI * radiusM * radiusM * heightM; }
    public double getMass()     { return densityKgM3 * getVolume(); }
    public int    getRadiusPx() { return Math.max(8, (int) Math.round(radiusM * PPM)); }
    public int    getWidthPx()  { return 2 * getRadiusPx(); }
    public int    getHeightPx() { return Math.max(6, (int) Math.round(heightM * PPM)); }
    public double getBottomY()  { return y + getHeightPx(); }
    public double getLeftX()    { return cx - getRadiusPx(); }
    public double getRightX()   { return cx + getRadiusPx(); }
    public double getTopFaceAreaM2()    { return Math.PI * radiusM * radiusM; }
    public double getBottomFaceAreaM2() { return Math.PI * radiusM * radiusM; }

    public boolean contains(int px, int py) {
        int m = 6;
        return px >= cx - getRadiusPx() - m && px <= cx + getRadiusPx() + m
            && py >= y - m && py <= getBottomY() + m;
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
    public boolean isPinned()        { return pinned; }
    public boolean isSelected()      { return selected; }

    // ── Setters ───────────────────────────────────────────────────────────────

    public void setName(String n)        { this.name = n; }
    public void setCx(double cx)         { this.cx = cx; }
    public void setY(double y)           { this.y = y; }
    public void setRadiusM(double r)     { this.radiusM = Math.max(0.05, r); }
    public void setHeightM(double h)     { this.heightM = Math.max(0.05, h); }
    public void setDensityKgM3(double d) { this.densityKgM3 = Math.max(10.0, d); }
    public void setVy(double vy)         { this.vy = vy; }
    public void setPinned(boolean p)     { this.pinned = p; }
    public void setSelected(boolean s)   { this.selected = s; }
}

package com.buoyancy.equation.model;

/**
 * Fluid environment — density, surface position, and animated wave state.
 *
 * <p>The fluid occupies the screen region from {@code surfaceY} (pixels from
 * the top of the canvas) down to the canvas floor. All physics calculations
 * use {@code surfaceY} as the reference; the wave is purely cosmetic.
 */
public class Fluid {

    // Common fluid density presets (kg/m³)
    public static final double DENSITY_FRESH_WATER = 1000.0;
    public static final double DENSITY_SEA_WATER   = 1025.0;
    public static final double DENSITY_OIL         =  850.0;
    public static final double DENSITY_MERCURY     = 13534.0;

    private double densityKgM3;
    private int    surfaceY;        // screen y of mean fluid surface (pixels)
    private double wavePhase = 0.0; // radians, advances each tick

    public Fluid(double densityKgM3, int surfaceY) {
        this.densityKgM3 = densityKgM3;
        this.surfaceY    = surfaceY;
    }

    // ── Wave animation ────────────────────────────────────────────────────────

    /** Advances the wave phase by {@code dt} seconds. */
    public void tickWave(double dt) {
        wavePhase = (wavePhase + dt * 1.8) % (2 * Math.PI);
    }

    /**
     * Returns the animated surface y at horizontal pixel {@code x}.
     * Combines two sine waves to give a natural-looking ripple.
     */
    public double getWaveY(double x) {
        return surfaceY
            + 3.5 * Math.sin(x * 0.030 + wavePhase)
            + 1.5 * Math.sin(x * 0.075 + wavePhase * 1.4);
    }

    // ── Getters / setters ─────────────────────────────────────────────────────

    public double getDensityKgM3()         { return densityKgM3; }
    public void   setDensityKgM3(double d) { this.densityKgM3 = d; }
    public int    getSurfaceY()            { return surfaceY; }
    public void   setSurfaceY(int y)       { this.surfaceY = y; }
    public double getWavePhase()           { return wavePhase; }
}

package com.buoyancy.pressure.model;

/**
 * Fluid medium properties for the pressure-differential simulator.
 *
 * <p>Pressure at depth {@code d} metres below the surface:
 * <pre>P(d) = ρ × g × d</pre>
 *
 * <p>This version has no wave animation so the pressure field renders cleanly.
 */
public class FluidMedium {

    private double densityKgM3;
    private int    surfaceY;      // screen y of fluid surface (pixels, y-down)

    public FluidMedium(double densityKgM3, int surfaceY) {
        this.densityKgM3 = densityKgM3;
        this.surfaceY    = surfaceY;
    }

    /**
     * Pressure in Pascals at the given screen y coordinate.
     * Returns 0 above the surface.
     *
     * @param screenY pixel y (y-down coordinate)
     * @param g       gravitational acceleration (m/s²)
     * @param ppm     pixels per metre
     */
    public double pressureAt(double screenY, double g, double ppm) {
        double depthPx = screenY - surfaceY;
        if (depthPx <= 0) return 0;
        return densityKgM3 * g * (depthPx / ppm);
    }

    /**
     * Maximum pressure at the given floor y (used for colour normalisation).
     */
    public double maxPressure(int floorY, double g, double ppm) {
        return pressureAt(floorY, g, ppm);
    }

    public double getDensityKgM3()         { return densityKgM3; }
    public void   setDensityKgM3(double d) { this.densityKgM3 = d; }
    public int    getSurfaceY()            { return surfaceY; }
    public void   setSurfaceY(int y)       { this.surfaceY = y; }
}

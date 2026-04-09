package com.buoyancy.pressure.physics;

import com.buoyancy.pressure.model.FluidMedium;
import com.buoyancy.pressure.model.PressureBody;

/**
 * Physics engine for the pressure-differential buoyancy simulator.
 *
 * <h2>Face-by-face force breakdown</h2>
 * <p>Archimedes' principle emerges from summing pressure forces on every face:
 * <pre>
 *   P(y) = ρ_fluid × g × depth(y)        (pressure at depth y below surface)
 *
 *   F_up   = P(y_bottom) × A_face        (upward push on bottom face)
 *   F_down = P(y_top)    × A_face        (downward push on top face)
 *   F_sides                               (cancel by symmetry → 0)
 *
 *   F_net_up = F_up − F_down
 *            = ρ_fluid × g × (depth_bottom − depth_top) × A
 *            = ρ_fluid × g × h_sub × A
 *            = ρ_fluid × g × V_sub        ← Archimedes
 * </pre>
 *
 * <p>This class computes both the face-by-face and Archimedes results so
 * the UI can display them side by side and confirm they agree.
 */
public final class PressurePhysics {

    public static final double DRAG = 2.5;  // viscous damping s⁻¹

    private PressurePhysics() {}

    // ── Geometry helpers ─────────────────────────────────────────────────────

    public static double submergedHeightM(PressureBody body, FluidMedium fluid) {
        double topY  = body.getY();
        double botY  = body.getBottomY();
        double surfY = fluid.getSurfaceY();
        double subPx = Math.max(0, botY - Math.max(topY, surfY));
        return Math.min(subPx, body.getHeightPx()) / PressureBody.PPM;
    }

    public static double submergedFraction(PressureBody body, FluidMedium fluid) {
        if (body.getHeightM() == 0) return 0;
        return submergedHeightM(body, fluid) / body.getHeightM();
    }

    // ── Bottom-face analysis ──────────────────────────────────────────────────

    /** Depth below surface (m) of the bottom face. 0 if above surface. */
    public static double bottomFaceDepthM(PressureBody body, FluidMedium fluid) {
        double depthPx = body.getBottomY() - fluid.getSurfaceY();
        return Math.max(0, depthPx) / PressureBody.PPM;
    }

    /** Pressure (Pa) at the bottom face. */
    public static double bottomFacePressure(PressureBody body, FluidMedium fluid, double g) {
        return fluid.getDensityKgM3() * g * bottomFaceDepthM(body, fluid);
    }

    /** Upward force (N) on the bottom face: P_bottom × A_face. */
    public static double forceOnBottomFace(PressureBody body, FluidMedium fluid, double g) {
        return bottomFacePressure(body, fluid, g) * body.getBottomFaceAreaM2();
    }

    // ── Top-face analysis ─────────────────────────────────────────────────────

    /** Depth below surface (m) of the top face. 0 if above surface. */
    public static double topFaceDepthM(PressureBody body, FluidMedium fluid) {
        double depthPx = body.getY() - fluid.getSurfaceY();
        return Math.max(0, depthPx) / PressureBody.PPM;
    }

    /** Pressure (Pa) at the top face. */
    public static double topFacePressure(PressureBody body, FluidMedium fluid, double g) {
        return fluid.getDensityKgM3() * g * topFaceDepthM(body, fluid);
    }

    /** Downward force (N) on the top face: P_top × A_face. */
    public static double forceOnTopFace(PressureBody body, FluidMedium fluid, double g) {
        return topFacePressure(body, fluid, g) * body.getTopFaceAreaM2();
    }

    // ── Net buoyancy from pressure summation ──────────────────────────────────

    /**
     * Net upward buoyant force (N) computed by the pressure-face method.
     * {@code F_net = F_bottom_up − F_top_down}
     * Side forces cancel by the left–right symmetry of a cylinder.
     */
    public static double netBuoyancyPressure(PressureBody body, FluidMedium fluid, double g) {
        return forceOnBottomFace(body, fluid, g) - forceOnTopFace(body, fluid, g);
    }

    /**
     * Net upward buoyant force (N) computed via Archimedes' principle.
     * {@code F_b = ρ_fluid × g × V_sub}
     */
    public static double netBuoyancyArchimedes(PressureBody body, FluidMedium fluid, double g) {
        double vSub = Math.PI * body.getRadiusM() * body.getRadiusM() * submergedHeightM(body, fluid);
        return fluid.getDensityKgM3() * g * vSub;
    }

    /** Gravitational force magnitude (N). */
    public static double gravitationalForce(PressureBody body, double g) {
        return body.getMass() * g;
    }

    /** Net downward force (N). Positive = sinks, negative = rises. */
    public static double netDownwardForce(PressureBody body, FluidMedium fluid, double g) {
        return gravitationalForce(body, g) - netBuoyancyArchimedes(body, fluid, g);
    }

    /**
     * Equilibrium top-y (pixels) for a floating object.
     * Returns {@link Double#NaN} if the object is denser than the fluid.
     */
    public static double equilibriumY(PressureBody body, FluidMedium fluid) {
        double ratio = body.getDensityKgM3() / fluid.getDensityKgM3();
        if (ratio >= 1.0) return Double.NaN;
        return fluid.getSurfaceY() - body.getHeightPx() * (1.0 - ratio);
    }

    // ── Integration ───────────────────────────────────────────────────────────

    /**
     * Advances physics by one Euler step (only when physics mode is enabled
     * and the body is not pinned).
     */
    public static void step(PressureBody body, FluidMedium fluid, double g,
                            double dt, int floorY, int ceilY) {
        if (body.isPinned()) return;

        double fNet  = netDownwardForce(body, fluid, g);
        double accel = fNet / body.getMass();
        double drag  = DRAG * body.getVy();

        double vy   = body.getVy() + (accel - drag) * dt;
        double yNew = body.getY() + vy * PressureBody.PPM * dt;

        if (yNew + body.getHeightPx() >= floorY) { yNew = floorY - body.getHeightPx(); vy = 0; }
        if (yNew < ceilY)                         { yNew = ceilY; vy = Math.max(0, vy); }

        body.setVy(vy);
        body.setY(yNew);
    }

    // ── Pressure-grid value at a pixel y ─────────────────────────────────────

    /**
     * Absolute pressure (Pa) at screen pixel y.
     */
    public static double pressureAtPixelY(double screenY, FluidMedium fluid, double g) {
        double depthPx = screenY - fluid.getSurfaceY();
        if (depthPx <= 0) return 0;
        return fluid.getDensityKgM3() * g * (depthPx / PressureBody.PPM);
    }
}

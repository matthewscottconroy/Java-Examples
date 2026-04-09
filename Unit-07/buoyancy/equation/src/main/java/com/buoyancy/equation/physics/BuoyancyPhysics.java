package com.buoyancy.equation.physics;

import com.buoyancy.equation.model.BuoyancyObject;
import com.buoyancy.equation.model.Fluid;

/**
 * Pure-static physics engine for Archimedes-equation buoyancy.
 *
 * <h2>Sign convention</h2>
 * <p>Screen y increases downward. Positive {@code vy} = moving downward.
 * Forces returned as <em>magnitudes</em> (always positive); the caller
 * decides the direction based on context.
 *
 * <h2>Integration</h2>
 * <p>First-order Euler with linear viscous drag:
 * <pre>
 *   a = (F_gravity − F_buoyancy) / mass − DRAG × vy
 *   vy += a × dt
 *   y  += vy × PPM × dt
 * </pre>
 *
 * @see BuoyancyObject
 * @see Fluid
 */
public final class BuoyancyPhysics {

    /**
     * Viscous drag coefficient (s⁻¹).  Acts as a velocity-proportional
     * damping term so objects settle at equilibrium instead of oscillating.
     * Value 2.5 gives a settling time of roughly 1 second.
     */
    public static final double DRAG = 2.5;

    private BuoyancyPhysics() {}

    // ── Core force calculations ───────────────────────────────────────────────

    /**
     * Submerged height of the cylinder in metres.
     * Returns 0 if fully above the surface, full height if fully submerged.
     */
    public static double submergedHeightM(BuoyancyObject obj, Fluid fluid) {
        double topY    = obj.getY();
        double botY    = obj.getBottomY();
        double surfY   = fluid.getSurfaceY();
        double subPx   = Math.max(0.0, botY - Math.max(topY, surfY));
        return Math.min(subPx, obj.getHeightPx()) / BuoyancyObject.PPM;
    }

    /**
     * Submerged volume of the cylinder in m³.
     */
    public static double submergedVolumeM3(BuoyancyObject obj, Fluid fluid) {
        double hSub = submergedHeightM(obj, fluid);
        return Math.PI * obj.getRadiusM() * obj.getRadiusM() * hSub;
    }

    /**
     * Buoyant force magnitude in Newtons (acts upward).
     * {@code F_b = ρ_fluid × g × V_sub}
     */
    public static double buoyantForce(BuoyancyObject obj, Fluid fluid, double g) {
        return fluid.getDensityKgM3() * g * submergedVolumeM3(obj, fluid);
    }

    /**
     * Gravitational force magnitude in Newtons (acts downward).
     * {@code F_g = m × g = ρ_obj × V_total × g}
     */
    public static double gravitationalForce(BuoyancyObject obj, double g) {
        return obj.getMass() * g;
    }

    /**
     * Net downward force in Newtons.
     * Positive = net force downward (sinks); negative = net force upward (rises).
     */
    public static double netForce(BuoyancyObject obj, Fluid fluid, double g) {
        return gravitationalForce(obj, g) - buoyantForce(obj, fluid, g);
    }

    /**
     * Fraction of the cylinder currently submerged: 0 = fully above, 1 = fully below.
     */
    public static double submergedFraction(BuoyancyObject obj, Fluid fluid) {
        if (obj.getHeightM() == 0) return 0;
        return submergedHeightM(obj, fluid) / obj.getHeightM();
    }

    /**
     * Equilibrium top-y (pixels) where the object would float with zero net force.
     * Returns {@link Double#NaN} if the object is denser than the fluid (sinks).
     *
     * <p>At equilibrium: V_sub/V_total = ρ_obj/ρ_fluid, so
     * h_sub = h_total × (ρ_obj/ρ_fluid), and the top face sits at
     * surfaceY − h_total × (1 − ρ_obj/ρ_fluid) above the surface.
     */
    public static double equilibriumY(BuoyancyObject obj, Fluid fluid) {
        double ratio = obj.getDensityKgM3() / fluid.getDensityKgM3();
        if (ratio >= 1.0) return Double.NaN;
        return fluid.getSurfaceY() - obj.getHeightPx() * (1.0 - ratio);
    }

    /**
     * Human-readable status string for the object's current motion state.
     */
    public static String statusString(BuoyancyObject obj, Fluid fluid, double g) {
        double ratio = obj.getDensityKgM3() / fluid.getDensityKgM3();
        if (ratio >= 1.0) {
            return Math.abs(obj.getVy()) < 0.04 ? "RESTING ON FLOOR" : "SINKING";
        }
        double fracSub = submergedFraction(obj, fluid);
        if (Math.abs(fracSub - ratio) < 0.025 && Math.abs(obj.getVy()) < 0.04) {
            return "FLOATING";
        }
        if (obj.getVy() > 0.04) return "SINKING";
        if (obj.getVy() < -0.04) return "RISING";
        return "SETTLING";
    }

    // ── Integration ───────────────────────────────────────────────────────────

    /**
     * Advances the physics of one object by {@code dt} seconds (Euler step).
     *
     * @param obj    object to update (pinned objects are skipped)
     * @param fluid  surrounding fluid
     * @param g      gravitational acceleration (m/s²)
     * @param dt     time step (seconds)
     * @param floorY canvas floor y (pixels) — hard floor constraint
     * @param ceilY  canvas ceiling y (pixels) — hard ceiling constraint
     */
    public static void step(BuoyancyObject obj, Fluid fluid, double g,
                            double dt, int floorY, int ceilY) {
        if (obj.isPinned()) return;

        double fNet   = netForce(obj, fluid, g);      // N, positive = downward
        double accel  = fNet / obj.getMass();          // m/s²
        double drag   = DRAG * obj.getVy();            // m/s² opposing motion
        double aTot   = accel - drag;

        double vy    = obj.getVy() + aTot * dt;
        double yNew  = obj.getY() + vy * BuoyancyObject.PPM * dt;

        // Floor constraint
        if (yNew + obj.getHeightPx() >= floorY) {
            yNew = floorY - obj.getHeightPx();
            vy   = 0.0;
        }
        // Ceiling constraint
        if (yNew < ceilY) {
            yNew = ceilY;
            vy   = Math.max(0.0, vy);
        }

        obj.setVy(vy);
        obj.setY(yNew);
    }
}

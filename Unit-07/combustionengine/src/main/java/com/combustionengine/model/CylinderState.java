package com.combustionengine.model;

/**
 * <h2>Cylinder State</h2>
 * Mutable snapshot of one cylinder's kinematic and thermodynamic state.
 *
 * <p>The 4-stroke cycle spans 720° (4π radians). {@link #cycleAngleRad}
 * tracks the cylinder's position within this cycle and advances at the
 * same rate as the crankshaft angular velocity.
 *
 * <p>Stroke mapping:
 * <pre>
 *   [0,   π)  → INTAKE
 *   [π,  2π)  → COMPRESSION
 *   [2π, 3π)  → POWER
 *   [3π, 4π)  → EXHAUST
 * </pre>
 */
public class CylinderState {

    // ── Identity ──────────────────────────────────────────────────────────────

    /** Zero-based cylinder index. */
    public final int index;

    // ── Kinematics ───────────────────────────────────────────────────────────

    /**
     * Position within the 4-stroke cycle, in radians [0, 4π).
     * Each π segment is one stroke.
     */
    public double cycleAngleRad;

    /** Piston displacement from TDC in metres (0 at TDC, strokeM at BDC). */
    public double pistonDispM;

    // ── Thermodynamics ───────────────────────────────────────────────────────

    /** In-cylinder gas pressure (Pa). */
    public double pressurePa;

    /** In-cylinder gas temperature (K). */
    public double temperatureK;

    /** Instantaneous cylinder volume (m³). */
    public double volumeM3;

    // ── Valve state ──────────────────────────────────────────────────────────

    public boolean intakeOpen;
    public boolean exhaustOpen;

    // ── Visual ───────────────────────────────────────────────────────────────

    /** Combustion glow intensity [0, 1]; fades after ignition events. */
    public double combustionGlow;

    /** Phase on the previous physics tick, used to detect stroke transitions. */
    public CyclePhase prevPhase;

    // ── Constructor ──────────────────────────────────────────────────────────

    /**
     * @param index       zero-based cylinder index
     * @param cycleOffset initial phase within the 720° cycle (radians)
     */
    public CylinderState(int index, double cycleOffset) {
        this.index          = index;
        this.cycleAngleRad  = cycleOffset % (4.0 * Math.PI);
        this.pressurePa     = 101_325.0;
        this.temperatureK   = 320.0;
        this.combustionGlow = 0.0;
        this.prevPhase      = currentPhase();
    }

    /** Current stroke phase, determined by which π-segment the cycle angle is in. */
    public CyclePhase currentPhase() {
        int seg = (int)(cycleAngleRad / Math.PI) % 4;
        return CyclePhase.values()[seg];
    }

    /**
     * Physical crank angle for piston-position geometry, in radians [0, 2π).
     * The slider-crank geometry repeats every full crank revolution.
     */
    public double crankAngleRad() { return cycleAngleRad % (2.0 * Math.PI); }
}

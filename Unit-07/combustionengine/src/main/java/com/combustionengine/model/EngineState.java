package com.combustionengine.model;

import java.util.ArrayList;
import java.util.List;

/**
 * <h2>Engine State</h2>
 * Top-level mutable simulation state: crankshaft dynamics and all per-cylinder states.
 *
 * <p>Cylinder phase offsets are spaced evenly through the 720° cycle using a
 * firing-order permutation, so power pulses are distributed as uniformly as possible:
 * <pre>
 *   phaseOffset_i = firingOrderPhase(i) × (4π / numCylinders)
 * </pre>
 *
 * <p>The 4-cylinder firing order is 1-3-4-2 (standard inline-4 even-fire).
 */
public class EngineState {

    // ── Configuration ────────────────────────────────────────────────────────

    /** Active engine configuration (geometry + thermodynamics). */
    public EngineConfig config;

    // ── Crankshaft ───────────────────────────────────────────────────────────

    /** Crankshaft angular velocity (rad/s). */
    public double omegaRadS;

    // ── Controls ─────────────────────────────────────────────────────────────

    /** Throttle position in [0, 1]. */
    public double throttle;

    /**
     * Simulation speed multiplier applied to the wall-clock dt before physics.
     * 1.0 = real-time; 0.25 = quarter speed (default, for educational clarity).
     */
    public double simSpeed = 0.25;

    // ── Observables ──────────────────────────────────────────────────────────

    /** Total instantaneous combustion torque across all cylinders (N·m). */
    public double combustionTorqueNm;

    /** Net crankshaft torque: combustion − load − friction (N·m). */
    public double netTorqueNm;

    /** Instantaneous indicated power (W). */
    public double powerW;

    /** Thermal efficiency for the current cycle type and operating conditions. */
    public double thermalEfficiency;

    // ── Cylinders ────────────────────────────────────────────────────────────

    /** Per-cylinder states, indexed 0…numCylinders−1. */
    public List<CylinderState> cylinders;

    // ── Constructor / reset ───────────────────────────────────────────────────

    /**
     * Creates an engine state from the given configuration with all cylinders
     * at rest and properly phased.
     */
    public EngineState(EngineConfig config) {
        this.config = config;
        reset();
    }

    /** Resets the engine to cold-start conditions (omega = 0, throttle = 0). */
    public void reset() {
        omegaRadS = 0.0;
        throttle  = 0.0;
        cylinders = new ArrayList<>();

        double phaseStep = 4.0 * Math.PI / config.numCylinders();
        for (int i = 0; i < config.numCylinders(); i++) {
            int fi = firingOrderPhase(i, config.numCylinders());
            cylinders.add(new CylinderState(i, fi * phaseStep));
        }
    }

    /** Returns the current engine speed in RPM. */
    public double rpm() { return omegaRadS * 60.0 / (2.0 * Math.PI); }

    /**
     * Maps a zero-based cylinder index to its firing-order phase index so that
     * power strokes are evenly spaced:
     * <ul>
     *   <li>1-cyl: phase 0</li>
     *   <li>2-cyl: phases 0, 1 (180° apart)</li>
     *   <li>4-cyl (order 1-3-4-2): phases 0, 3, 1, 2</li>
     * </ul>
     */
    private static int firingOrderPhase(int i, int n) {
        return switch (n) {
            case 4  -> new int[]{0, 3, 1, 2}[i];  // firing order 1-3-4-2
            default -> i;
        };
    }
}

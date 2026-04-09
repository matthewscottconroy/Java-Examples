package com.combustionengine.physics;

import com.combustionengine.model.*;

/**
 * <h2>Engine Physics</h2>
 * Simulates the thermodynamic cycle and crankshaft dynamics of an internal
 * combustion engine using the idealised <em>Otto cycle</em>.
 *
 * <h3>Otto cycle (ideal)</h3>
 * <pre>
 *   1 → 2  Isentropic compression    P · V^γ = const
 *   2 → 3  Isochoric heat addition   at TDC (instantaneous combustion model)
 *   3 → 4  Isentropic expansion
 *   4 → 1  Isochoric heat rejection  exhaust blow-down
 * </pre>
 *
 * <h3>Thermal efficiency</h3>
 * <pre>
 *   η_th = 1 − r_c^(1 − γ)     r_c = compression ratio, γ = 1.35
 * </pre>
 *
 * <h3>Slider-crank kinematics</h3>
 * <pre>
 *   x(φ) = r · (1 − cos φ) + l − √(l² − r²·sin²φ)
 *   V(φ) = V_c + A · x(φ)
 * </pre>
 * where r = crank radius, l = rod length, φ = crank angle (0 at TDC),
 * V_c = clearance volume, A = bore area.
 *
 * <h3>Crankshaft torque from slider-crank</h3>
 * <pre>
 *   β  = arcsin(r · sin φ / l)
 *   T  = (P − P_atm) · A · r · sin(φ + β) / cos β
 * </pre>
 *
 * <h3>Crankshaft dynamics</h3>
 * <pre>
 *   I · dω/dt = T_combustion − K_LOAD · ω − K_FRIC · ω
 * </pre>
 */
public final class EnginePhysics {

    // ── Physical constants ────────────────────────────────────────────────────

    /** Ratio of specific heats for the air-fuel mixture. */
    public static final double GAMMA          = 1.35;

    /** Specific heat at constant volume for the mixture (J / kg·K). */
    public static final double CV             = 718.0;

    /** Atmospheric pressure (Pa). */
    public static final double P_ATM          = 101_325.0;

    /** Intake charge temperature (K). */
    public static final double T_INTAKE       = 320.0;

    /** Specific gas constant for air (J / kg·K). */
    private static final double R_AIR         = 287.0;

    /** Combustion efficiency: fraction of fuel energy released as heat. */
    private static final double COMBUSTION_ETA = 0.85;

    /**
     * Scale applied to the adiabatic heat release to account for real-gas
     * effects (dissociation, incomplete mixing) that lower peak temperatures.
     * Without this, the ideal calculation over-predicts T_peak by ~4×.
     */
    private static final double HEAT_SCALE    = 0.25;

    /** Linear load torque coefficient: T_load = K_LOAD · ω  (N·m·s/rad). */
    private static final double K_LOAD        = 0.05;

    /** Linear friction torque coefficient: T_fric = K_FRIC · ω  (N·m·s/rad). */
    private static final double K_FRIC        = 0.015;

    /** Angular velocity below which the engine is considered stalled (rad/s ≈ 100 RPM). */
    public static final double STALL_OMEGA    = 10.5;

    private EnginePhysics() {}

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Advances the engine simulation by {@code dt} seconds.
     *
     * @param state current engine state (mutated in place)
     * @param dt    time step in seconds (should be ≤ 1/60 s for stability)
     */
    public static void step(EngineState state, double dt) {
        double omega  = state.omegaRadS;
        double dTheta = omega * dt;   // crank rotation this tick (radians)

        double totalTorque = 0.0;

        for (CylinderState cyl : state.cylinders) {
            CyclePhase before = cyl.currentPhase();

            // Advance cycle angle (wraps at 4π = 720°)
            cyl.cycleAngleRad = (cyl.cycleAngleRad + dTheta) % (4.0 * Math.PI);

            CyclePhase after = cyl.currentPhase();

            // ── Stroke transition effects ─────────────────────────────────────
            if (before != after) {
                switch (after) {
                    case INTAKE -> {
                        // Intake valve opens: start filling the bore with charge
                        cyl.gasLevel = 0.0;
                    }
                    case POWER -> {
                        // Spark fires: combustion glow + shockwave flash
                        cyl.combustionGlow  = Math.min(1.0, state.throttle * 1.2 + 0.1);
                        cyl.explosionFlash  = Math.min(1.0, state.throttle * 1.1 + 0.15);
                        cyl.gasLevel        = 1.0;
                    }
                    case EXHAUST -> {
                        // Exhaust valve opens: charge starts leaving
                        cyl.gasLevel = 1.0;
                    }
                    default -> {}
                }
            }

            // ── Continuous per-stroke updates ─────────────────────────────────
            switch (after) {
                case INTAKE -> {
                    // Fill rate proportional to how fast the piston is descending
                    cyl.gasLevel = Math.min(1.0, cyl.gasLevel + dt * 4.0 * state.simSpeed);
                }
                case EXHAUST -> {
                    // Purge rate proportional to piston ascending speed
                    cyl.gasLevel = Math.max(0.0, cyl.gasLevel - dt * 4.0 * state.simSpeed);
                }
                default -> {}
            }

            // Combustion glow fades during power stroke
            cyl.combustionGlow  = Math.max(0.0, cyl.combustionGlow  - dt * 5.0);
            // Explosion flash fades very quickly (it's a brief spark event)
            cyl.explosionFlash  = Math.max(0.0, cyl.explosionFlash  - dt * 12.0);

            // Slider-crank kinematics
            double phi = cyl.crankAngleRad();
            cyl.pistonDispM = pistonDisplacement(phi, state.config.crankRadiusM(), state.config.rodLengthM());
            cyl.volumeM3    = cylinderVolume(cyl.pistonDispM, state.config.boreAreaM2(), state.config.clearanceVolumeM3());

            // Thermodynamic state for this stroke
            updateThermodynamics(cyl, state.config, state.throttle);

            // Torque contribution from this cylinder
            totalTorque += cylinderTorque(cyl.pressurePa, phi,
                    state.config.crankRadiusM(), state.config.rodLengthM(),
                    state.config.boreAreaM2());

            cyl.prevPhase = after;
        }

        // ── Crankshaft dynamics ───────────────────────────────────────────────
        // I · dω/dt = T_combustion − T_load − T_friction
        double tLoad = K_LOAD * omega;
        double tFric = K_FRIC * omega;
        double tNet  = totalTorque - tLoad - tFric;
        double alpha = tNet / state.config.inertiaKgM2();

        omega += alpha * dt;
        if (omega < 0.0) omega = 0.0;

        // Starter motor: hold omega at stall threshold while throttle is applied
        if (omega < STALL_OMEGA && state.throttle > 0.02) {
            omega = STALL_OMEGA;
        }

        state.omegaRadS         = omega;
        state.combustionTorqueNm = totalTorque;
        state.netTorqueNm        = tNet;
        state.powerW             = totalTorque * omega;
        state.thermalEfficiency  = thermalEfficiency(state.config.compressionRatio());
    }

    // ── Kinematics ───────────────────────────────────────────────────────────

    /**
     * Piston displacement from TDC using the slider-crank relationship.
     *
     * <pre>
     *   x = r · (1 − cos φ) + l − √(l² − r²·sin²φ)
     * </pre>
     *
     * @param phi crank angle from TDC (radians, 0 at TDC, π at BDC)
     * @param r   crank throw (half-stroke) in metres
     * @param l   connecting-rod length in metres
     * @return displacement from TDC in metres (0 at TDC, ≈ stroke at BDC)
     */
    public static double pistonDisplacement(double phi, double r, double l) {
        double sinPhi = Math.sin(phi);
        double inner  = l * l - r * r * sinPhi * sinPhi;
        if (inner < 0.0) inner = 0.0;   // guard against floating-point underrun
        return r * (1.0 - Math.cos(phi)) + l - Math.sqrt(inner);
    }

    /**
     * Instantaneous cylinder volume.
     *
     * <pre>
     *   V = V_clearance + A_bore · x
     * </pre>
     */
    public static double cylinderVolume(double pistonDisp, double boreArea, double clearanceVol) {
        return clearanceVol + boreArea * pistonDisp;
    }

    // ── Thermodynamics ───────────────────────────────────────────────────────

    /**
     * Updates the in-cylinder pressure and temperature for the current stroke.
     *
     * <p>The model follows an idealised Otto cycle:
     * <ul>
     *   <li><b>Intake</b>: pressure = P_atm, temperature = T_intake.</li>
     *   <li><b>Compression</b>: isentropic — P · V^γ = P_atm · V_max^γ.</li>
     *   <li><b>Power</b>: instantaneous heat addition at TDC raises P and T;
     *       thereafter isentropic expansion.</li>
     *   <li><b>Exhaust</b>: pressure drops to P_atm; temperature reflects hot
     *       exhaust gas.</li>
     * </ul>
     */
    private static void updateThermodynamics(CylinderState cyl, EngineConfig cfg, double throttle) {
        CyclePhase phase = cyl.currentPhase();
        double Vc   = cfg.clearanceVolumeM3();
        double Vmax = cfg.maxVolumeM3();
        double rc   = cfg.compressionRatio();

        cyl.intakeOpen  = (phase == CyclePhase.INTAKE);
        cyl.exhaustOpen = (phase == CyclePhase.EXHAUST);

        switch (phase) {
            case INTAKE -> {
                cyl.pressurePa   = P_ATM;
                cyl.temperatureK = T_INTAKE;
            }
            case COMPRESSION -> {
                // Isentropic from BDC: P_atm · V_max^γ = P · V^γ
                double ratio     = Vmax / cyl.volumeM3;
                cyl.pressurePa   = P_ATM * Math.pow(ratio, GAMMA);
                cyl.temperatureK = T_INTAKE * Math.pow(ratio, GAMMA - 1.0);
            }
            case POWER -> {
                // Compressed state at TDC (end of compression)
                double P_comp = P_ATM  * Math.pow(rc, GAMMA);
                double T_comp = T_INTAKE * Math.pow(rc, GAMMA - 1.0);

                // Heat added per cycle: Q = η_c · scale · m_fuel · LHV
                // Air mass filling the full displacement each cycle:
                double rhoAir = P_ATM / (R_AIR * T_INTAKE);
                double mAir   = rhoAir * cfg.displacementM3();
                double mFuel  = throttle * mAir / cfg.airFuelRatio();
                double Q      = COMBUSTION_ETA * HEAT_SCALE * mFuel * cfg.fuelLHV();

                // Isochoric temperature rise at TDC: ΔT = Q / (m_mix · Cv)
                double mMix   = Math.max(mAir + mFuel, 1e-15);
                double dT     = Q / (mMix * CV);

                double T_peak = T_comp + dT;
                double P_peak = P_comp * (T_peak / T_comp);

                // Isentropic expansion from TDC (V = Vc) to current volume
                double expRatio  = cyl.volumeM3 / Vc;
                cyl.pressurePa   = P_peak / Math.pow(expRatio, GAMMA);
                cyl.temperatureK = T_peak / Math.pow(expRatio, GAMMA - 1.0);
            }
            case EXHAUST -> {
                cyl.pressurePa   = P_ATM;
                cyl.temperatureK = 700.0 + 300.0 * throttle;
            }
        }
    }

    // ── Torque ───────────────────────────────────────────────────────────────

    /**
     * Instantaneous torque contribution from one cylinder via the slider-crank.
     *
     * <pre>
     *   β  = arcsin(r · sin φ / l)
     *   T  = (P − P_atm) · A · r · sin(φ + β) / cos β
     * </pre>
     *
     * @param pressurePa in-cylinder gas pressure (Pa)
     * @param phi        crank angle from TDC (radians)
     * @param r          crank throw in metres
     * @param l          connecting-rod length in metres
     * @param boreArea   bore cross-sectional area (m²)
     * @return torque in N·m (positive = driving the crank)
     */
    public static double cylinderTorque(double pressurePa, double phi,
                                         double r, double l, double boreArea) {
        double sinPhi  = Math.sin(phi);
        double sinBeta = Math.max(-1.0, Math.min(1.0, r * sinPhi / l));
        double beta    = Math.asin(sinBeta);
        double cosBeta = Math.cos(beta);
        if (Math.abs(cosBeta) < 1e-9) return 0.0;

        double netPressure = pressurePa - P_ATM;
        return netPressure * boreArea * r * Math.sin(phi + beta) / cosBeta;
    }

    // ── Efficiency ───────────────────────────────────────────────────────────

    /**
     * Otto cycle ideal thermal efficiency.
     *
     * <pre>
     *   η_th = 1 − r_c^(1 − γ)
     * </pre>
     *
     * @param compressionRatio volumetric compression ratio
     * @return thermal efficiency in [0, 1]
     */
    public static double thermalEfficiency(double compressionRatio) {
        return 1.0 - Math.pow(compressionRatio, 1.0 - GAMMA);
    }

    /**
     * Computes the peak in-cylinder pressure at full throttle for the given
     * configuration. Used to scale the P-V diagram axis.
     */
    public static double peakPressureRef(EngineConfig cfg) {
        double rc     = cfg.compressionRatio();
        double P_comp = P_ATM   * Math.pow(rc, GAMMA);
        double T_comp = T_INTAKE * Math.pow(rc, GAMMA - 1.0);

        double rhoAir = P_ATM / (R_AIR * T_INTAKE);
        double mAir   = rhoAir * cfg.displacementM3();
        double mFuel  = mAir / cfg.airFuelRatio();
        double Q      = COMBUSTION_ETA * HEAT_SCALE * mFuel * cfg.fuelLHV();
        double mMix   = Math.max(mAir + mFuel, 1e-15);
        double dT     = Q / (mMix * CV);

        double T_peak = T_comp + dT;
        return P_comp * (T_peak / T_comp);
    }
}

package com.combustionengine.physics;

import com.combustionengine.model.*;

/**
 * <h2>Engine Physics</h2>
 * Simulates the thermodynamic cycle and crankshaft dynamics of an internal
 * combustion engine.  Two cycle types are supported:
 *
 * <h3>Otto cycle (spark-ignition, {@link EngineType#OTTO})</h3>
 * <pre>
 *   1 → 2  Isentropic compression     P · V^γ = const
 *   2 → 3  Isochoric heat addition    at TDC (constant volume)
 *   3 → 4  Isentropic expansion
 *   4 → 1  Isochoric heat rejection   exhaust blow-down
 * </pre>
 * Thermal efficiency:
 * <pre>
 *   η_Otto = 1 − r_c^(1 − γ)
 * </pre>
 *
 * <h3>Diesel cycle (compression-ignition, {@link EngineType#DIESEL})</h3>
 * <pre>
 *   1 → 2  Isentropic compression     (higher r_c than Otto)
 *   2 → 3  Isobaric heat addition     at constant pressure (fuel injection)
 *   3 → 4  Isentropic expansion
 *   4 → 1  Isochoric heat rejection   exhaust blow-down
 * </pre>
 * Thermal efficiency:
 * <pre>
 *   η_Diesel = 1 − (1 / r_c^(γ−1)) · (r_co^γ − 1) / (γ · (r_co − 1))
 * </pre>
 * where r_co = cutoff ratio = V after heat addition / V at TDC.
 *
 * <h3>Slider-crank kinematics (both cycles)</h3>
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

    /** Specific heat at constant pressure: C_p = γ · C_v  (J / kg·K). */
    public static final double CP             = GAMMA * CV;

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

        boolean isDiesel = state.config.engineType() == EngineType.DIESEL;

        for (CylinderState cyl : state.cylinders) {
            CyclePhase before = cyl.currentPhase();

            // Advance cycle angle (wraps at 4π = 720°)
            cyl.cycleAngleRad = (cyl.cycleAngleRad + dTheta) % (4.0 * Math.PI);

            CyclePhase after = cyl.currentPhase();

            // ── Stroke transition effects ─────────────────────────────────────
            if (before != after) {
                switch (after) {
                    case INTAKE -> {
                        cyl.gasLevel = 0.0;
                    }
                    case POWER -> {
                        cyl.combustionGlow = Math.min(1.0, state.throttle * 1.2 + 0.1);
                        if (isDiesel) {
                            // Compression-ignition: gradual burn, no sharp spark flash
                            cyl.explosionFlash = 0.0;
                        } else {
                            // Spark-ignition: brief shockwave flash
                            cyl.explosionFlash = Math.min(1.0, state.throttle * 1.1 + 0.15);
                        }
                        cyl.gasLevel = 1.0;
                    }
                    case EXHAUST -> {
                        cyl.gasLevel = 1.0;
                    }
                    default -> {}
                }
            }

            // ── Continuous per-stroke updates ─────────────────────────────────
            switch (after) {
                case INTAKE -> {
                    cyl.gasLevel = Math.min(1.0, cyl.gasLevel + dt * 4.0 * state.simSpeed);
                }
                case EXHAUST -> {
                    cyl.gasLevel = Math.max(0.0, cyl.gasLevel - dt * 4.0 * state.simSpeed);
                }
                default -> {}
            }

            cyl.combustionGlow  = Math.max(0.0, cyl.combustionGlow  - dt * 5.0);
            cyl.explosionFlash  = Math.max(0.0, cyl.explosionFlash  - dt * 12.0);

            // Slider-crank kinematics
            double phi = cyl.crankAngleRad();
            cyl.pistonDispM = pistonDisplacement(phi, state.config.crankRadiusM(), state.config.rodLengthM());
            cyl.volumeM3    = cylinderVolume(cyl.pistonDispM, state.config.boreAreaM2(), state.config.clearanceVolumeM3());

            updateThermodynamics(cyl, state.config, state.throttle);

            totalTorque += cylinderTorque(cyl.pressurePa, phi,
                    state.config.crankRadiusM(), state.config.rodLengthM(),
                    state.config.boreAreaM2());

            cyl.prevPhase = after;
        }

        // ── Crankshaft dynamics ───────────────────────────────────────────────
        double tLoad = K_LOAD * omega;
        double tFric = K_FRIC * omega;
        double tNet  = totalTorque - tLoad - tFric;
        double alpha = tNet / state.config.inertiaKgM2();

        omega += alpha * dt;
        if (omega < 0.0) omega = 0.0;

        if (omega < STALL_OMEGA && state.throttle > 0.02) {
            omega = STALL_OMEGA;
        }

        state.omegaRadS         = omega;
        state.combustionTorqueNm = totalTorque;
        state.netTorqueNm        = tNet;
        state.powerW             = totalTorque * omega;

        // Thermal efficiency depends on cycle type
        if (isDiesel) {
            double rco = dieselCutoffRatio(state.config, state.throttle);
            state.thermalEfficiency = dieselThermalEfficiency(state.config.compressionRatio(), rco);
        } else {
            state.thermalEfficiency = thermalEfficiency(state.config.compressionRatio());
        }
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
        if (inner < 0.0) inner = 0.0;
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
     * <p>For Otto engines the power stroke uses <em>isochoric</em> heat addition
     * (constant volume at TDC) followed by isentropic expansion.
     *
     * <p>For Diesel engines the power stroke uses <em>isobaric</em> heat addition
     * (constant pressure from TDC to the cutoff volume) followed by isentropic
     * expansion.
     */
    private static void updateThermodynamics(CylinderState cyl, EngineConfig cfg, double throttle) {
        CyclePhase phase = cyl.currentPhase();
        double Vmax = cfg.maxVolumeM3();

        cyl.intakeOpen  = (phase == CyclePhase.INTAKE);
        cyl.exhaustOpen = (phase == CyclePhase.EXHAUST);

        switch (phase) {
            case INTAKE -> {
                cyl.pressurePa   = P_ATM;
                cyl.temperatureK = T_INTAKE;
            }
            case COMPRESSION -> {
                double ratio     = Vmax / cyl.volumeM3;
                cyl.pressurePa   = P_ATM * Math.pow(ratio, GAMMA);
                cyl.temperatureK = T_INTAKE * Math.pow(ratio, GAMMA - 1.0);
            }
            case POWER -> {
                if (cfg.engineType() == EngineType.DIESEL) {
                    updateDieselPower(cyl, cfg, throttle);
                } else {
                    updateOttoPower(cyl, cfg, throttle);
                }
            }
            case EXHAUST -> {
                cyl.pressurePa   = P_ATM;
                cyl.temperatureK = 700.0 + 300.0 * throttle;
            }
        }
    }

    /**
     * Otto POWER stroke: isochoric heat addition at TDC, then isentropic expansion.
     *
     * <pre>
     *   P_peak = P_comp · (T_peak / T_comp)
     *   P(V)   = P_peak · (V_c / V)^γ
     * </pre>
     */
    private static void updateOttoPower(CylinderState cyl, EngineConfig cfg, double throttle) {
        double Vc   = cfg.clearanceVolumeM3();
        double rc   = cfg.compressionRatio();

        double P_comp = P_ATM    * Math.pow(rc, GAMMA);
        double T_comp = T_INTAKE * Math.pow(rc, GAMMA - 1.0);

        double rhoAir = P_ATM / (R_AIR * T_INTAKE);
        double mAir   = rhoAir * cfg.displacementM3();
        double mFuel  = throttle * mAir / cfg.airFuelRatio();
        double Q      = COMBUSTION_ETA * HEAT_SCALE * mFuel * cfg.fuelLHV();

        double mMix   = Math.max(mAir + mFuel, 1e-15);
        double dT     = Q / (mMix * CV);

        double T_peak = T_comp + dT;
        double P_peak = P_comp * (T_peak / T_comp);

        double expRatio  = cyl.volumeM3 / Vc;
        cyl.pressurePa   = P_peak / Math.pow(expRatio, GAMMA);
        cyl.temperatureK = T_peak / Math.pow(expRatio, GAMMA - 1.0);
    }

    /**
     * Diesel POWER stroke: isobaric heat addition from TDC to the cutoff
     * volume, then isentropic expansion.
     *
     * <pre>
     *   Phase A (V ≤ V_co):  P = P_comp,   T = T_comp · (V / V_c)
     *   Phase B (V &gt; V_co):  P = P_comp · (V_co / V)^γ
     *                         T = T_peak  · (V_co / V)^(γ−1)
     * </pre>
     * where V_co = V_c · r_co  and  r_co = T_peak / T_comp.
     */
    private static void updateDieselPower(CylinderState cyl, EngineConfig cfg, double throttle) {
        double Vc   = cfg.clearanceVolumeM3();
        double rc   = cfg.compressionRatio();

        double P_comp = P_ATM    * Math.pow(rc, GAMMA);
        double T_comp = T_INTAKE * Math.pow(rc, GAMMA - 1.0);

        double rhoAir = P_ATM / (R_AIR * T_INTAKE);
        double mAir   = rhoAir * cfg.displacementM3();
        double mFuel  = throttle * mAir / cfg.airFuelRatio();
        double Q      = COMBUSTION_ETA * HEAT_SCALE * mFuel * cfg.fuelLHV();

        double mMix   = Math.max(mAir + mFuel, 1e-15);
        // Isobaric heat addition: ΔT = Q / (m · C_p)
        double dT     = Q / (mMix * CP);
        double T_peak = T_comp + dT;
        double rco    = Math.max(1.0, T_peak / T_comp);   // cutoff ratio
        double V_co   = Vc * rco;

        double V = cyl.volumeM3;

        if (V <= V_co) {
            // Isobaric phase: pressure constant, temperature rises with volume
            cyl.pressurePa   = P_comp;
            cyl.temperatureK = T_comp * (V / Vc);
        } else {
            // Isentropic expansion from the cutoff point
            cyl.pressurePa   = P_comp * Math.pow(V_co / V, GAMMA);
            cyl.temperatureK = T_peak * Math.pow(V_co / V, GAMMA - 1.0);
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
     *   η_Otto = 1 − r_c^(1 − γ)
     * </pre>
     *
     * @param compressionRatio volumetric compression ratio
     * @return thermal efficiency in [0, 1]
     */
    public static double thermalEfficiency(double compressionRatio) {
        return 1.0 - Math.pow(compressionRatio, 1.0 - GAMMA);
    }

    /**
     * Diesel cycle ideal thermal efficiency.
     *
     * <pre>
     *   η_Diesel = 1 − (1 / r_c^(γ−1)) · (r_co^γ − 1) / (γ · (r_co − 1))
     * </pre>
     *
     * @param compressionRatio volumetric compression ratio r_c
     * @param cutoffRatio      cutoff ratio r_co = V after heat addition / V at TDC (&gt; 1)
     * @return thermal efficiency in [0, 1]
     */
    public static double dieselThermalEfficiency(double compressionRatio, double cutoffRatio) {
        if (cutoffRatio <= 1.0) return thermalEfficiency(compressionRatio); // degenerate case
        double term = (Math.pow(cutoffRatio, GAMMA) - 1.0) / (GAMMA * (cutoffRatio - 1.0));
        return 1.0 - term / Math.pow(compressionRatio, GAMMA - 1.0);
    }

    /**
     * Computes the diesel cutoff ratio r_co = T_peak / T_comp for the given
     * throttle position.  Used for efficiency calculations and the P-V diagram.
     *
     * @param cfg      engine configuration
     * @param throttle throttle position in [0, 1]
     * @return cutoff ratio (≥ 1.0)
     */
    public static double dieselCutoffRatio(EngineConfig cfg, double throttle) {
        double rc     = cfg.compressionRatio();
        double T_comp = T_INTAKE * Math.pow(rc, GAMMA - 1.0);
        double rhoAir = P_ATM / (R_AIR * T_INTAKE);
        double mAir   = rhoAir * cfg.displacementM3();
        double mFuel  = throttle * mAir / cfg.airFuelRatio();
        double Q      = COMBUSTION_ETA * HEAT_SCALE * mFuel * cfg.fuelLHV();
        double mMix   = Math.max(mAir + mFuel, 1e-15);
        double dT     = Q / (mMix * CP);
        return Math.max(1.0, 1.0 + dT / T_comp);
    }

    /**
     * Computes the peak in-cylinder pressure at full throttle for the given
     * configuration.  Used to scale the P-V diagram y-axis.
     *
     * <ul>
     *   <li>Otto: P_peak &gt; P_comp (isochoric heat addition raises pressure).</li>
     *   <li>Diesel: P_peak = P_comp (isobaric — pressure stays at compression value).</li>
     * </ul>
     */
    public static double peakPressureRef(EngineConfig cfg) {
        double rc     = cfg.compressionRatio();
        double P_comp = P_ATM * Math.pow(rc, GAMMA);

        if (cfg.engineType() == EngineType.DIESEL) {
            return P_comp;
        }

        // Otto: isochoric heat addition at TDC raises pressure above P_comp
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

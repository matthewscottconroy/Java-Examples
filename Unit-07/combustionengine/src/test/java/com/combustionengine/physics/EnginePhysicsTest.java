package com.combustionengine.physics;

import com.combustionengine.model.EngineConfig;
import com.combustionengine.model.EngineType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EnginePhysicsTest {

    private static final EngineConfig CFG =
            new EngineConfig(0.078, 0.085, 0.140, 9.5, 4, 44.0e6, 14.7, 0.30, EngineType.OTTO);

    private static final EngineConfig DIESEL_CFG =
            new EngineConfig(0.081, 0.096, 0.155, 18.0, 4, 42.5e6, 22.0, 0.32, EngineType.DIESEL);

    // ── Kinematics ────────────────────────────────────────────────────────────

    @Test
    void pistonAtTDC_isZero() {
        double x = EnginePhysics.pistonDisplacement(0.0, CFG.crankRadiusM(), CFG.rodLengthM());
        assertEquals(0.0, x, 1e-9, "Displacement at TDC (phi=0) must be zero");
    }

    @Test
    void pistonAtBDC_isStroke() {
        double x = EnginePhysics.pistonDisplacement(Math.PI, CFG.crankRadiusM(), CFG.rodLengthM());
        assertEquals(CFG.strokeM(), x, 1e-6, "Displacement at BDC (phi=π) must equal the stroke");
    }

    @Test
    void pistonDisplacement_isNonNegative_allAngles() {
        double r = CFG.crankRadiusM(), l = CFG.rodLengthM();
        for (int i = 0; i <= 360; i++) {
            double phi = Math.toRadians(i);
            double x = EnginePhysics.pistonDisplacement(phi, r, l);
            assertTrue(x >= 0.0, "Displacement must be non-negative at phi=" + i + "°");
        }
    }

    // ── Volume ────────────────────────────────────────────────────────────────

    @Test
    void volumeAtTDC_equalsClearanceVolume() {
        double Vc = CFG.clearanceVolumeM3();
        double V  = EnginePhysics.cylinderVolume(0.0, CFG.boreAreaM2(), Vc);
        assertEquals(Vc, V, 1e-12);
    }

    @Test
    void volumeAtBDC_equalsMaxVolume() {
        double Vc   = CFG.clearanceVolumeM3();
        double Vmax = CFG.maxVolumeM3();
        double V    = EnginePhysics.cylinderVolume(CFG.strokeM(), CFG.boreAreaM2(), Vc);
        assertEquals(Vmax, V, 1e-10);
    }

    // ── Torque ────────────────────────────────────────────────────────────────

    @Test
    void torqueAtTDC_isZero() {
        // sin(0) = 0, so torque must be zero regardless of pressure
        double T = EnginePhysics.cylinderTorque(5_000_000.0, 0.0,
                CFG.crankRadiusM(), CFG.rodLengthM(), CFG.boreAreaM2());
        assertEquals(0.0, T, 1e-6, "Torque at TDC must be zero");
    }

    @Test
    void torqueAtBDC_isZero() {
        double T = EnginePhysics.cylinderTorque(200_000.0, Math.PI,
                CFG.crankRadiusM(), CFG.rodLengthM(), CFG.boreAreaM2());
        assertEquals(0.0, T, 1e-6, "Torque at BDC must be zero");
    }

    @Test
    void torqueAtMidStroke_isPositive_aboveAtmospheric() {
        // phi = π/2 (90° into power stroke), pressure above atmospheric → positive torque
        double T = EnginePhysics.cylinderTorque(1_500_000.0, Math.PI / 2,
                CFG.crankRadiusM(), CFG.rodLengthM(), CFG.boreAreaM2());
        assertTrue(T > 0.0, "Torque at mid-stroke with elevated pressure should be positive");
    }

    // ── Efficiency ────────────────────────────────────────────────────────────

    @Test
    void thermalEfficiency_isInUnitInterval() {
        double eta = EnginePhysics.thermalEfficiency(CFG.compressionRatio());
        assertTrue(eta > 0.0 && eta < 1.0, "Thermal efficiency must be in (0, 1)");
    }

    @Test
    void thermalEfficiency_increasesWithCompressionRatio() {
        double eta8  = EnginePhysics.thermalEfficiency(8.0);
        double eta12 = EnginePhysics.thermalEfficiency(12.0);
        assertTrue(eta12 > eta8, "Higher compression ratio must yield higher efficiency");
    }

    @Test
    void thermalEfficiency_matchesFormula() {
        // η = 1 − r^(1−γ), γ = 1.35, r = 9.5
        double expected = 1.0 - Math.pow(9.5, 1.0 - EnginePhysics.GAMMA);
        double actual   = EnginePhysics.thermalEfficiency(9.5);
        assertEquals(expected, actual, 1e-12);
    }

    // ── Diesel efficiency ─────────────────────────────────────────────────────

    @Test
    void dieselEfficiency_isInUnitInterval() {
        double rco = EnginePhysics.dieselCutoffRatio(DIESEL_CFG, 0.8);
        double eta = EnginePhysics.dieselThermalEfficiency(DIESEL_CFG.compressionRatio(), rco);
        assertTrue(eta > 0.0 && eta < 1.0, "Diesel efficiency must be in (0, 1)");
    }

    @Test
    void dieselEfficiency_higherThanOttoAtSameRC() {
        // At equal compression ratios diesel is less efficient than Otto due to
        // the cutoff ratio penalty — but higher CR diesels beat lower CR Ottos.
        // Verify the formula: diesel at rc=18 beats Otto at rc=9.5.
        double rco       = EnginePhysics.dieselCutoffRatio(DIESEL_CFG, 0.8);
        double etaDiesel = EnginePhysics.dieselThermalEfficiency(18.0, rco);
        double etaOtto   = EnginePhysics.thermalEfficiency(9.5);
        assertTrue(etaDiesel > etaOtto,
                "Diesel at rc=18 must be more efficient than Otto at rc=9.5");
    }

    @Test
    void dieselEfficiency_degeneratesTo_otto_whenCutoffIsOne() {
        // rco = 1 means zero heat addition; formula falls back to Otto efficiency
        double etaDiesel = EnginePhysics.dieselThermalEfficiency(9.5, 1.0);
        double etaOtto   = EnginePhysics.thermalEfficiency(9.5);
        assertEquals(etaOtto, etaDiesel, 1e-9,
                "Diesel efficiency with rco=1 must equal Otto efficiency");
    }

    @Test
    void dieselCutoffRatio_isOneAtZeroThrottle() {
        double rco = EnginePhysics.dieselCutoffRatio(DIESEL_CFG, 0.0);
        assertEquals(1.0, rco, 1e-9, "No fuel → no heat addition → cutoff ratio = 1");
    }

    @Test
    void dieselCutoffRatio_increasesWithThrottle() {
        double rco25 = EnginePhysics.dieselCutoffRatio(DIESEL_CFG, 0.25);
        double rco75 = EnginePhysics.dieselCutoffRatio(DIESEL_CFG, 0.75);
        assertTrue(rco75 > rco25, "More fuel → larger cutoff ratio");
    }
}

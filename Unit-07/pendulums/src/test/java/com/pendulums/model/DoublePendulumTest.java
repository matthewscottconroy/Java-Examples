package com.pendulums.model;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link DoublePendulum}.
 */
@DisplayName("DoublePendulum")
class DoublePendulumTest {

    private static final double L1 = 140.0, L2 = 140.0;
    private static final double M1 = 1.0,   M2 = 1.0;
    private static final double G  = 980.0;

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("stores initial angles with zero angular velocity")
        void initialState() {
            DoublePendulum dp = make(Math.PI / 4, Math.PI / 3);
            assertEquals(Math.PI / 4, dp.getTheta1(), 1e-12);
            assertEquals(Math.PI / 3, dp.getTheta2(), 1e-12);
            assertEquals(0.0, dp.getOmega1(), 1e-12);
            assertEquals(0.0, dp.getOmega2(), 1e-12);
        }

        @Test
        @DisplayName("bob positions match trigonometry")
        void bobPositions() {
            double t1 = Math.PI / 6, t2 = -Math.PI / 4;
            DoublePendulum dp = make(t1, t2);
            assertEquals(L1 * Math.sin(t1), dp.bob1RelX(), 1e-9);
            assertEquals(L1 * Math.cos(t1), dp.bob1RelY(), 1e-9);
            assertEquals(L2 * Math.sin(t2), dp.bob2RelX(), 1e-9);
            assertEquals(L2 * Math.cos(t2), dp.bob2RelY(), 1e-9);
        }

        @Test
        @DisplayName("both bobs at equilibrium → zero relative positions")
        void bothAtEquilibrium() {
            DoublePendulum dp = make(0, 0);
            assertEquals(0.0, dp.bob1RelX(), 1e-12);
            assertEquals(L1, dp.bob1RelY(), 1e-12);
            assertEquals(0.0, dp.bob2RelX(), 1e-12);
            assertEquals(L2, dp.bob2RelY(), 1e-12);
        }
    }

    // -------------------------------------------------------------------------
    // Energy
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("energy")
    class Energy {

        @Test
        @DisplayName("total energy = KE + PE identity")
        void totalIsSum() {
            DoublePendulum dp = make(0.8, 0.5);
            assertEquals(dp.kineticEnergy() + dp.potentialEnergy(), dp.totalEnergy(), 1e-9);
        }

        @Test
        @DisplayName("kinetic energy is zero when both omegas are zero")
        void keZeroAtRest() {
            DoublePendulum dp = make(1.0, -0.5);
            assertEquals(0.0, dp.kineticEnergy(), 1e-12);
        }

        @Test
        @DisplayName("RK4 approximately conserves total energy over short simulation")
        void energyConservation() {
            DoublePendulum dp = make(0.3, 0.2);
            double e0 = dp.totalEnergy();
            for (int i = 0; i < 5000; i++) dp.step(0.001);
            assertEquals(e0, dp.totalEnergy(), Math.abs(e0) * 0.005,
                    "energy drift must be < 0.5% over 5 seconds");
        }

        @Test
        @DisplayName("energy is higher for larger initial angles")
        void higherAngleMoreEnergy() {
            DoublePendulum small  = make(0.1, 0.1);
            DoublePendulum large  = make(1.5, 1.5);
            assertTrue(large.totalEnergy() > small.totalEnergy());
        }
    }

    // -------------------------------------------------------------------------
    // Dynamics
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("dynamics")
    class Dynamics {

        @Test
        @DisplayName("system at equilibrium stays at equilibrium")
        void equilibriumIsStable() {
            DoublePendulum dp = make(0, 0);
            for (int i = 0; i < 1000; i++) dp.step(0.001);
            assertEquals(0.0, dp.getTheta1(), 1e-10);
            assertEquals(0.0, dp.getTheta2(), 1e-10);
        }

        @Test
        @DisplayName("small perturbation produces oscillation (omega becomes non-zero)")
        void perturbationOscillates() {
            DoublePendulum dp = make(0.05, 0.0);
            dp.step(0.001);
            assertNotEquals(0.0, dp.getOmega1(), 1e-12);
        }

        @Test
        @DisplayName("reset restores angles and clears velocities")
        void resetRestoresState() {
            DoublePendulum dp = make(0.5, 0.3);
            for (int i = 0; i < 200; i++) dp.step(0.005);
            dp.reset(0.7, -0.4);
            assertEquals(0.7,  dp.getTheta1(), 1e-12);
            assertEquals(-0.4, dp.getTheta2(), 1e-12);
            assertEquals(0.0,  dp.getOmega1(), 1e-12);
            assertEquals(0.0,  dp.getOmega2(), 1e-12);
        }

        @Test
        @DisplayName("chaos: two nearby initial conditions diverge")
        void chaoticDivergence() {
            // 2.5 rad (~143°) and 1.5 rad (~86°) are well inside the chaotic regime.
            // Track max separation so the test passes even if trajectories happen
            // to be close at the final sample point.
            DoublePendulum dp1 = make(2.5, 1.5);
            DoublePendulum dp2 = new DoublePendulum(2.5 + 1e-4, 1.5, L1, L2, M1, M2, G);
            double maxDiff = 0;
            for (int i = 0; i < 15000; i++) {
                dp1.step(0.001);
                dp2.step(0.001);
                maxDiff = Math.max(maxDiff, Math.abs(dp1.getTheta1() - dp2.getTheta1()));
            }
            assertTrue(maxDiff > 1.0,
                    "chaotic trajectories must separate by > 1 rad at some point within 15 s");
        }
    }

    // -------------------------------------------------------------------------
    // Trail
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("trail")
    class Trail {

        @Test
        @DisplayName("trail size is bounded by MAX_TRAIL")
        void trailBounded() {
            DoublePendulum dp = make(0.8, 0.5);
            for (int i = 0; i < DoublePendulum.MAX_TRAIL + 100; i++) {
                dp.step(0.001);
                dp.recordTrail(350, 100);
            }
            assertEquals(DoublePendulum.MAX_TRAIL, dp.getTrail().size());
        }

        @Test
        @DisplayName("reset clears trail")
        void resetClearsTrail() {
            DoublePendulum dp = make(0.5, 0.4);
            for (int i = 0; i < 20; i++) { dp.step(0.01); dp.recordTrail(0, 0); }
            assertFalse(dp.getTrail().isEmpty());
            dp.reset(0.5, 0.4);
            assertTrue(dp.getTrail().isEmpty());
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static DoublePendulum make(double t1, double t2) {
        return new DoublePendulum(t1, t2, L1, L2, M1, M2, G);
    }
}

package com.bifurcation.model;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link LorenzAttractor}.
 */
@DisplayName("LorenzAttractor")
class LorenzAttractorTest {

    private static final double SIGMA = 10.0;
    private static final double RHO   = 28.0;
    private static final double BETA  = 8.0 / 3.0;
    private static final double DT    = 0.005;

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("default initial state (0.1, 0, 0) is stored correctly")
        void initialState() {
            LorenzAttractor la = lorenz();
            assertEquals(0.1, la.getX(), 1e-12);
            assertEquals(0.0, la.getY(), 1e-12);
            assertEquals(0.0, la.getZ(), 1e-12);
        }

        @Test
        @DisplayName("parameters are stored correctly")
        void parametersStored() {
            LorenzAttractor la = lorenz();
            assertEquals(SIGMA, la.getSigma(), 1e-12);
            assertEquals(RHO,   la.getRho(),   1e-12);
            assertEquals(BETA,  la.getBeta(),  1e-12);
        }

        @Test
        @DisplayName("shadow trajectory starts at x + SHADOW_OFFSET")
        void shadowOffset() {
            LorenzAttractor la = lorenz();
            assertEquals(0.1 + LorenzAttractor.SHADOW_OFFSET, la.getShadowX(), 1e-15);
            assertEquals(0.0, la.getShadowY(), 1e-15);
            assertEquals(0.0, la.getShadowZ(), 1e-15);
        }

        @Test
        @DisplayName("SHADOW_OFFSET is 1e-5")
        void shadowOffsetValue() {
            assertEquals(1e-5, LorenzAttractor.SHADOW_OFFSET, 1e-20);
        }
    }

    // -------------------------------------------------------------------------
    // Step advances state
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("step")
    class Step {

        @Test
        @DisplayName("one step changes the state (x, y, z are not all unchanged)")
        void stepChangesState() {
            LorenzAttractor la = lorenz();
            double x0 = la.getX(), y0 = la.getY(), z0 = la.getZ();
            la.step(DT);
            // At least one coordinate must change
            boolean changed = (la.getX() != x0) || (la.getY() != y0) || (la.getZ() != z0);
            assertTrue(changed, "step must change the state");
        }

        @Test
        @DisplayName("after many steps x stays in bounded range |x| < 30")
        void xBounded() {
            LorenzAttractor la = lorenz();
            for (int i = 0; i < 10000; i++) la.step(DT);
            assertTrue(Math.abs(la.getX()) < 30.0,
                    "|x| must be < 30 for classic parameters, got " + la.getX());
        }

        @Test
        @DisplayName("after many steps y stays in bounded range |y| < 50")
        void yBounded() {
            LorenzAttractor la = lorenz();
            for (int i = 0; i < 10000; i++) la.step(DT);
            assertTrue(Math.abs(la.getY()) < 50.0,
                    "|y| must be < 50 for classic parameters, got " + la.getY());
        }

        @Test
        @DisplayName("after many steps z stays in bounded range 0 < z < 60")
        void zBounded() {
            LorenzAttractor la = lorenz();
            // Warm up to get onto the attractor
            for (int i = 0; i < 2000; i++) la.step(DT);
            // Check over a long run
            for (int i = 0; i < 8000; i++) {
                la.step(DT);
                assertTrue(la.getZ() < 60.0,
                        "z must be < 60 for classic parameters, got " + la.getZ());
                assertTrue(la.getZ() > -5.0,
                        "z must be > -5 for classic parameters, got " + la.getZ());
            }
        }
    }

    // -------------------------------------------------------------------------
    // Trail
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("trail")
    class Trail {

        @Test
        @DisplayName("trail grows after recordTrail calls")
        void trailGrows() {
            LorenzAttractor la = lorenz();
            la.step(DT); la.recordTrail();
            la.step(DT); la.recordTrail();
            assertEquals(2, la.getTrail().size());
        }

        @Test
        @DisplayName("trail does not exceed MAX_TRAIL")
        void trailCapped() {
            LorenzAttractor la = lorenz();
            for (int i = 0; i < LorenzAttractor.MAX_TRAIL + 100; i++) {
                la.step(DT);
                la.recordTrail();
            }
            assertEquals(LorenzAttractor.MAX_TRAIL, la.getTrail().size());
        }

        @Test
        @DisplayName("shadow trail does not exceed MAX_TRAIL")
        void shadowTrailCapped() {
            LorenzAttractor la = lorenz();
            for (int i = 0; i < LorenzAttractor.MAX_TRAIL + 100; i++) {
                la.step(DT);
                la.recordTrail();
            }
            assertEquals(LorenzAttractor.MAX_TRAIL, la.getShadowTrail().size());
        }

        @Test
        @DisplayName("reset clears trail and shadow trail")
        void resetClearsTrail() {
            LorenzAttractor la = lorenz();
            for (int i = 0; i < 50; i++) { la.step(DT); la.recordTrail(); }
            assertTrue(la.getTrail().size() > 0);
            la.reset();
            assertEquals(0, la.getTrail().size());
            assertEquals(0, la.getShadowTrail().size());
        }
    }

    // -------------------------------------------------------------------------
    // Reset
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("reset")
    class Reset {

        @Test
        @DisplayName("reset() restores initial state (x=0.1, y=0, z=0)")
        void resetRestoresDefaultInitialState() {
            LorenzAttractor la = lorenz();
            for (int i = 0; i < 1000; i++) la.step(DT);
            la.reset();
            assertEquals(0.1, la.getX(), 1e-12);
            assertEquals(0.0, la.getY(), 1e-12);
            assertEquals(0.0, la.getZ(), 1e-12);
        }

        @Test
        @DisplayName("reset(x0,y0,z0) restores custom initial state")
        void resetRestoresCustomInitialState() {
            LorenzAttractor la = lorenz();
            for (int i = 0; i < 500; i++) la.step(DT);
            la.reset(1.0, 2.0, 3.0);
            assertEquals(1.0, la.getX(), 1e-12);
            assertEquals(2.0, la.getY(), 1e-12);
            assertEquals(3.0, la.getZ(), 1e-12);
        }

        @Test
        @DisplayName("reset restores shadow trajectory offset")
        void resetRestoresShadowOffset() {
            LorenzAttractor la = lorenz();
            for (int i = 0; i < 500; i++) la.step(DT);
            la.reset();
            assertEquals(0.1 + LorenzAttractor.SHADOW_OFFSET, la.getShadowX(), 1e-15);
        }
    }

    // -------------------------------------------------------------------------
    // Sensitive dependence on initial conditions
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("sensitive dependence")
    class SensitiveDependence {

        @Test
        @DisplayName("two nearby initial conditions diverge: |Δ| > 1.0 after 10000 steps")
        void trajectoresDiverge() {
            LorenzAttractor la = lorenz();
            // 10000 steps × dt=0.005 = 50 simulated seconds.
            // Lorenz Lyapunov exponent ≈ 0.9 → divergence ≈ 1e-5 · e^(0.9·50) ≈ 1.6
            for (int i = 0; i < 10000; i++) la.step(DT);
            double div = la.divergence();
            assertTrue(div > 1.0,
                    "Shadow trajectory should have diverged significantly (|Δ| > 1.0), got " + div);
        }

        @Test
        @DisplayName("initial divergence equals SHADOW_OFFSET")
        void initialDivergenceIsOffset() {
            LorenzAttractor la = lorenz();
            assertEquals(LorenzAttractor.SHADOW_OFFSET, la.divergence(), 1e-15);
        }
    }

    // -------------------------------------------------------------------------
    // Getters / setters
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("getters and setters")
    class GettersSetters {

        @Test
        @DisplayName("setSigma / setRho / setBeta update the parameters")
        void settersUpdateParameters() {
            LorenzAttractor la = lorenz();
            la.setSigma(12.0);
            la.setRho(35.0);
            la.setBeta(2.0);
            assertEquals(12.0, la.getSigma(), 1e-12);
            assertEquals(35.0, la.getRho(),   1e-12);
            assertEquals(2.0,  la.getBeta(),  1e-12);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static LorenzAttractor lorenz() {
        return new LorenzAttractor(0.1, 0.0, 0.0, SIGMA, RHO, BETA);
    }
}

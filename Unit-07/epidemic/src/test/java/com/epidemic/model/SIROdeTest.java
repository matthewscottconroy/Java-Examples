package com.epidemic.model;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link SIROde}.
 */
@DisplayName("SIROde")
class SIROdeTest {

    private static final double N    = 1000.0;
    private static final double I0   = 10.0;
    private static final double BETA = 0.30;
    private static final double GAMMA= 0.05;
    private static final double DT   = 0.1;
    private static final double EPS  = 1e-6;

    // -------------------------------------------------------------------------
    // Construction / reset
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("construction and reset")
    class ConstructionReset {

        @Test
        @DisplayName("default constructor creates valid initial state")
        void defaultConstructor() {
            SIROde m = new SIROde();
            assertEquals(SIROde.DEFAULT_N,  m.getN(),   1e-12);
            assertEquals(SIROde.DEFAULT_I0, m.getI(),   1e-12);
            assertEquals(SIROde.DEFAULT_N - SIROde.DEFAULT_I0, m.getS(), 1e-12);
            assertEquals(0.0, m.getR(), 1e-12);
        }

        @Test
        @DisplayName("parametric constructor sets state correctly")
        void parametricConstructor() {
            SIROde m = new SIROde(N, I0);
            assertEquals(N,       m.getN(),  1e-12);
            assertEquals(I0,      m.getI(),  1e-12);
            assertEquals(N - I0,  m.getS(),  1e-12);
            assertEquals(0.0,     m.getR(),  1e-12);
        }

        @Test
        @DisplayName("reset restores initial conditions")
        void resetRestoresState() {
            SIROde m = new SIROde(N, I0);
            for (int t = 0; t < 100; t++) m.step(DT, BETA, GAMMA);
            m.reset(I0);
            assertEquals(I0,     m.getI(), 1e-12);
            assertEquals(N - I0, m.getS(), 1e-12);
            assertEquals(0.0,    m.getR(), 1e-12);
        }

        @Test
        @DisplayName("reset with different i0 uses new value")
        void resetWithDifferentI0() {
            SIROde m = new SIROde(N, I0);
            m.reset(50.0);
            assertEquals(50.0,       m.getI(), 1e-12);
            assertEquals(N - 50.0,   m.getS(), 1e-12);
            assertEquals(0.0,        m.getR(), 1e-12);
        }
    }

    // -------------------------------------------------------------------------
    // Conservation: S + I + R ≈ N
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("S+I+R ≈ N conservation")
    class Conservation {

        @Test
        @DisplayName("conserved() ≈ N at initial state")
        void conservedAtStart() {
            SIROde m = new SIROde(N, I0);
            assertEquals(N, m.conserved(), EPS);
        }

        @Test
        @DisplayName("conserved() ≈ N after 100 steps")
        void conservedDuringEpidemic() {
            SIROde m = new SIROde(N, I0);
            for (int t = 0; t < 100; t++) {
                m.step(DT, BETA, GAMMA);
                assertEquals(N, m.conserved(), EPS,
                        "S+I+R conservation violated at step " + t);
            }
        }

        @Test
        @DisplayName("conserved() ≈ N after 1000 steps")
        void conservedLongRun() {
            SIROde m = new SIROde(N, I0);
            for (int t = 0; t < 1000; t++) m.step(DT, BETA, GAMMA);
            assertEquals(N, m.conserved(), EPS);
        }

        @Test
        @DisplayName("conserved() ≈ N with high beta and gamma")
        void conservedHighParams() {
            SIROde m = new SIROde(N, I0);
            for (int t = 0; t < 200; t++) {
                m.step(DT, 0.8, 0.3);
                assertEquals(N, m.conserved(), EPS,
                        "conservation violated at step " + t);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Monotonicity
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("monotonicity")
    class Monotonicity {

        @Test
        @DisplayName("S is monotonically non-increasing")
        void sNonIncreasing() {
            SIROde m = new SIROde(N, I0);
            double prevS = m.getS();
            for (int t = 0; t < 500; t++) {
                m.step(DT, BETA, GAMMA);
                double curS = m.getS();
                assertTrue(curS <= prevS + EPS,
                        "S increased at step " + t + ": " + prevS + " -> " + curS);
                prevS = curS;
            }
        }

        @Test
        @DisplayName("R is monotonically non-decreasing")
        void rNonDecreasing() {
            SIROde m = new SIROde(N, I0);
            double prevR = m.getR();
            for (int t = 0; t < 500; t++) {
                m.step(DT, BETA, GAMMA);
                double curR = m.getR();
                assertTrue(curR >= prevR - EPS,
                        "R decreased at step " + t + ": " + prevR + " -> " + curR);
                prevR = curR;
            }
        }
    }

    // -------------------------------------------------------------------------
    // R₀ < 1 → I decreases
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("R0 < 1 causes I to decrease")
    class SubThreshold {

        @Test
        @DisplayName("with R0 << 1, I(t) declines from the start")
        void iDecreasesWhenR0LessThanOne() {
            // R0 = beta/gamma; choose beta=0.02, gamma=0.15 → R0 ≈ 0.13
            double betaLow  = 0.02;
            double gammaHi  = 0.15;
            SIROde m = new SIROde(N, I0);
            double i0 = m.getI();
            for (int t = 0; t < 20; t++) m.step(DT, betaLow, gammaHi);
            assertTrue(m.getI() < i0,
                    "I should decrease when R0 = " + (betaLow / gammaHi) + " < 1");
        }

        @Test
        @DisplayName("with R0 > 1, I(t) rises initially")
        void iIncreasesWhenR0GreaterThanOne() {
            // R0 = 0.3/0.05 = 6
            SIROde m = new SIROde(N, I0);
            double i0 = m.getI();
            for (int t = 0; t < 30; t++) m.step(DT, BETA, GAMMA);
            assertTrue(m.getI() > i0,
                    "I should grow initially when R0 = " + (BETA / GAMMA) + " > 1");
        }
    }

    // -------------------------------------------------------------------------
    // Edge cases
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("edge cases")
    class EdgeCases {

        @Test
        @DisplayName("getN returns the fixed population size")
        void getNCorrect() {
            SIROde m = new SIROde(500, 5);
            assertEquals(500.0, m.getN(), 1e-12);
        }

        @Test
        @DisplayName("S never goes negative")
        void sNeverNegative() {
            SIROde m = new SIROde(N, I0);
            for (int t = 0; t < 2000; t++) m.step(DT, 0.9, 0.01);
            assertTrue(m.getS() >= 0, "S must never be negative");
        }

        @Test
        @DisplayName("I never goes negative")
        void iNeverNegative() {
            SIROde m = new SIROde(N, I0);
            for (int t = 0; t < 2000; t++) m.step(DT, 0.01, 0.9);
            assertTrue(m.getI() >= 0, "I must never be negative");
        }
    }
}

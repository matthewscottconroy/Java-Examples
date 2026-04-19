package com.bifurcation.model;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link LogisticMap}.
 */
@DisplayName("LogisticMap")
class LogisticMapTest {

    // -------------------------------------------------------------------------
    // Basic iteration
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("iterate")
    class Iterate {

        @Test
        @DisplayName("r=0: all iterates are 0 regardless of initial x")
        void rZeroConvergesToZero() {
            LogisticMap map = new LogisticMap(0.0);
            double x = 0.7;
            for (int i = 0; i < 10; i++) x = map.iterate(x);
            assertEquals(0.0, x, 1e-15);
        }

        @Test
        @DisplayName("r=2: iterates converge to fixed point 0.5 from x=0.1")
        void rTwoConvergesToFixedPoint() {
            LogisticMap map = new LogisticMap(2.0);
            double x = 0.1;
            for (int i = 0; i < 500; i++) x = map.iterate(x);
            assertEquals(0.5, x, 1e-9, "r=2 should converge to fixed point 0.5");
        }

        @Test
        @DisplayName("r=4.0: all iterates remain in [0, 1]")
        void rFourStaysInUnitInterval() {
            LogisticMap map = new LogisticMap(4.0);
            double x = 0.3;
            for (int i = 0; i < 10000; i++) {
                x = map.iterate(x);
                assertTrue(x >= 0.0 && x <= 1.0,
                        "x must stay in [0,1] for r=4, got x=" + x);
            }
        }

        @Test
        @DisplayName("r=3.2: period-2 orbit (after transient, two-step map returns same value)")
        void rThreeTwoPeriodTwo() {
            LogisticMap map = new LogisticMap(3.2);
            double x = 0.5;
            // Discard transient
            for (int i = 0; i < 1000; i++) x = map.iterate(x);
            // After settling, two more iterates should return to same x
            double x1 = map.iterate(x);
            double x2 = map.iterate(x1);
            assertEquals(x, x2, 1e-6,
                    "r=3.2 should have period-2 orbit: iterate twice should return to x");
        }
    }

    // -------------------------------------------------------------------------
    // Fixed point
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("fixedPoint")
    class FixedPoint {

        @Test
        @DisplayName("fixedPoint(2.5) = (2.5-1)/2.5 = 0.6")
        void fixedPointFormula() {
            LogisticMap map = new LogisticMap(2.5);
            assertEquals(0.6, map.fixedPoint(2.5), 1e-12);
        }

        @Test
        @DisplayName("fixedPoint(r≤1) returns 0.0 (extinction)")
        void fixedPointExtinction() {
            LogisticMap map = new LogisticMap(0.5);
            assertEquals(0.0, map.fixedPoint(0.5), 1e-12);
            assertEquals(0.0, map.fixedPoint(1.0), 1e-12);
        }

        @Test
        @DisplayName("fixedPoint(r) satisfies x = r*x*(1-x) for 1 < r < 3")
        void fixedPointIsSelfConsistent() {
            LogisticMap map = new LogisticMap(2.7);
            double fp = map.fixedPoint(2.7);
            // f(fp) = fp
            double fOfFp = 2.7 * fp * (1.0 - fp);
            assertEquals(fp, fOfFp, 1e-10, "fixed point must satisfy f(x*) = x*");
        }

        @Test
        @DisplayName("iterates converge to fixedPoint value for r=2.5")
        void iteratesConvergeToFixedPoint() {
            LogisticMap map = new LogisticMap(2.5);
            double x = 0.3;
            for (int i = 0; i < 500; i++) x = map.iterate(x);
            assertEquals(map.fixedPoint(2.5), x, 1e-8);
        }
    }

    // -------------------------------------------------------------------------
    // Bifurcation points
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("bifurcationPoints")
    class BifurcationPoints {

        @Test
        @DisplayName("returns non-empty array of expected length")
        void returnsExpectedLength() {
            LogisticMap map = new LogisticMap(3.0);
            int rSteps = 50, attractor = 20;
            double[] pts = map.bifurcationPoints(2.5, 4.0, rSteps, 300, attractor);
            assertEquals(rSteps * attractor, pts.length);
        }

        @Test
        @DisplayName("all attractor values remain in [0, 1]")
        void allValuesInUnitInterval() {
            LogisticMap map = new LogisticMap(3.5);
            double[] pts = map.bifurcationPoints(2.4, 4.0, 100, 500, 50);
            for (double v : pts) {
                assertTrue(v >= 0.0 && v <= 1.0,
                        "bifurcation point out of [0,1]: " + v);
            }
        }

        @Test
        @DisplayName("r=2.5 attractor points cluster near fixed point (r-1)/r")
        void attractorNearFixedPointForStableR() {
            LogisticMap map = new LogisticMap(2.5);
            double expected = map.fixedPoint(2.5);
            double[] pts = map.bifurcationPoints(2.5, 2.5, 1, 1000, 50);
            for (double v : pts) {
                assertEquals(expected, v, 1e-6,
                        "r=2.5 attractor should be near fixed point " + expected);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Chaos detection (Lyapunov exponent)
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("isChaotic")
    class IsChaotic {

        @Test
        @DisplayName("r=2.5 (stable fixed point) is not chaotic")
        void stableFixedPointNotChaotic() {
            LogisticMap map = new LogisticMap(2.5);
            assertFalse(map.isChaotic(2.5, 5000),
                    "r=2.5 should not be chaotic");
        }

        @Test
        @DisplayName("r=3.2 (period-2 orbit) is not chaotic")
        void periodTwoNotChaotic() {
            LogisticMap map = new LogisticMap(3.2);
            assertFalse(map.isChaotic(3.2, 5000),
                    "r=3.2 (period-2) should not be chaotic");
        }

        @Test
        @DisplayName("r=4.0 (fully chaotic) is chaotic")
        void fullyChaotic() {
            LogisticMap map = new LogisticMap(4.0);
            assertTrue(map.isChaotic(4.0, 5000),
                    "r=4.0 should be chaotic");
        }

        @Test
        @DisplayName("r=3.9 is chaotic")
        void nearlyFullyChaotic() {
            LogisticMap map = new LogisticMap(3.9);
            assertTrue(map.isChaotic(3.9, 5000),
                    "r=3.9 should be chaotic");
        }
    }

    // -------------------------------------------------------------------------
    // Feigenbaum constant
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("FEIGENBAUM_DELTA")
    class FeigenbaumDelta {

        @Test
        @DisplayName("FEIGENBAUM_DELTA has correct value to 4 decimal places")
        void feigenbaumCorrectToFourDecimalPlaces() {
            // Known value: 4.6692016...
            assertEquals(4.6692, LogisticMap.FEIGENBAUM_DELTA, 0.00005,
                    "FEIGENBAUM_DELTA should be 4.6692... to 4 decimal places");
        }

        @Test
        @DisplayName("FEIGENBAUM_DELTA is positive and greater than 4")
        void feigenbaumPositiveAndGreaterThanFour() {
            assertTrue(LogisticMap.FEIGENBAUM_DELTA > 4.0,
                    "Feigenbaum delta must be greater than 4");
            assertTrue(LogisticMap.FEIGENBAUM_DELTA < 5.0,
                    "Feigenbaum delta must be less than 5");
        }
    }

    // -------------------------------------------------------------------------
    // Getters / setters
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("getters and setters")
    class GettersSetters {

        @Test
        @DisplayName("getR returns the value passed to constructor")
        void getR() {
            LogisticMap map = new LogisticMap(3.7);
            assertEquals(3.7, map.getR(), 1e-12);
        }

        @Test
        @DisplayName("setR updates the r value")
        void setR() {
            LogisticMap map = new LogisticMap(2.0);
            map.setR(3.8);
            assertEquals(3.8, map.getR(), 1e-12);
        }
    }
}

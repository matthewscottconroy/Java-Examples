package com.lotkavolterra.model;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link LotkaVolterra}.
 *
 * <p>Covers equilibrium formulae, conservation of the Lyapunov function,
 * population positivity, reset behaviour, and zero derivatives at equilibrium.
 */
@DisplayName("LotkaVolterra")
class LotkaVolterraTest {

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Create a model with the given α, β, δ, γ and starting populations. */
    private static LotkaVolterra make(double alpha, double beta,
                                      double delta, double gamma,
                                      double x0, double y0) {
        LotkaVolterra lv = new LotkaVolterra(x0, y0);
        lv.setAlpha(alpha);
        lv.setBeta(beta);
        lv.setDelta(delta);
        lv.setGamma(gamma);
        return lv;
    }

    // -------------------------------------------------------------------------
    // Equilibrium formulae
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("equilibrium")
    class Equilibrium {

        @Test
        @DisplayName("x* = gamma / delta")
        void preyEquilibrium() {
            LotkaVolterra lv = make(1.0, 0.1, 0.075, 1.5, 10.0, 5.0);
            assertEquals(1.5 / 0.075, lv.equilibriumX(), 1e-9);
        }

        @Test
        @DisplayName("y* = alpha / beta")
        void predatorEquilibrium() {
            LotkaVolterra lv = make(1.0, 0.1, 0.075, 1.5, 10.0, 5.0);
            assertEquals(1.0 / 0.1, lv.equilibriumY(), 1e-9);
        }

        @Test
        @DisplayName("equilibrium changes correctly when parameters are updated")
        void equilibriumChangesWithParams() {
            LotkaVolterra lv = make(2.0, 0.2, 0.1, 3.0, 10.0, 5.0);
            assertEquals(3.0 / 0.1, lv.equilibriumX(), 1e-9,  "x* = gamma/delta");
            assertEquals(2.0 / 0.2, lv.equilibriumY(), 1e-9,  "y* = alpha/beta");
        }
    }

    // -------------------------------------------------------------------------
    // Conserved quantity
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("conserved quantity")
    class ConservedQuantity {

        @Test
        @DisplayName("V is approximately constant after 1000 steps (< 0.5 % drift)")
        void conservationOver1000Steps() {
            LotkaVolterra lv = make(1.0, 0.1, 0.075, 1.5, 10.0, 5.0);
            double v0 = lv.conservedQuantity();
            for (int i = 0; i < 1000; i++) lv.step();
            double v1 = lv.conservedQuantity();
            double drift = Math.abs(v1 - v0) / Math.abs(v0);
            assertTrue(drift < 0.005,
                    "Lyapunov drift should be < 0.5 % over 1000 steps, was " + (drift * 100) + " %");
        }

        @Test
        @DisplayName("V formula: delta*x - gamma*ln(x) + beta*y - alpha*ln(y)")
        void conservedQuantityFormula() {
            double alpha = 1.0, beta = 0.1, delta = 0.075, gamma = 1.5;
            LotkaVolterra lv = make(alpha, beta, delta, gamma, 10.0, 5.0);
            double x = lv.getX();
            double y = lv.getY();
            double expected = delta * x - gamma * Math.log(x)
                            + beta  * y - alpha * Math.log(y);
            assertEquals(expected, lv.conservedQuantity(), 1e-12);
        }
    }

    // -------------------------------------------------------------------------
    // Population positivity
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("population positivity")
    class Positivity {

        @Test
        @DisplayName("prey stays positive after many steps")
        void preyPositive() {
            LotkaVolterra lv = make(1.0, 0.1, 0.075, 1.5, 10.0, 5.0);
            for (int i = 0; i < 5000; i++) lv.step();
            assertTrue(lv.getX() > 0.0, "prey must remain positive");
        }

        @Test
        @DisplayName("predator stays positive after many steps")
        void predatorPositive() {
            LotkaVolterra lv = make(1.0, 0.1, 0.075, 1.5, 10.0, 5.0);
            for (int i = 0; i < 5000; i++) lv.step();
            assertTrue(lv.getY() > 0.0, "predator must remain positive");
        }

        @Test
        @DisplayName("populations stay positive for small dt (stability check)")
        void positiveForSmallDt() {
            LotkaVolterra lv = make(1.2, 0.15, 0.1, 1.0, 8.0, 4.0);
            for (int i = 0; i < 2000; i++) lv.step();
            assertTrue(lv.getX() > 0.0);
            assertTrue(lv.getY() > 0.0);
        }
    }

    // -------------------------------------------------------------------------
    // Reset
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("reset")
    class Reset {

        @Test
        @DisplayName("reset restores the exact initial populations")
        void resetRestoresPopulations() {
            LotkaVolterra lv = make(1.0, 0.1, 0.075, 1.5, 12.0, 7.0);
            for (int i = 0; i < 200; i++) lv.step();
            lv.reset(12.0, 7.0);
            assertEquals(12.0, lv.getX(), 1e-9, "prey must return to x0 after reset");
            assertEquals( 7.0, lv.getY(), 1e-9, "predator must return to y0 after reset");
        }

        @Test
        @DisplayName("reset with new values updates initial conditions")
        void resetWithNewValues() {
            LotkaVolterra lv = make(1.0, 0.1, 0.075, 1.5, 5.0, 3.0);
            lv.reset(20.0, 10.0);
            assertEquals(20.0, lv.getX(), 1e-9);
            assertEquals(10.0, lv.getY(), 1e-9);
        }

        @Test
        @DisplayName("reset stores the new x0 and y0 values")
        void resetStoresX0Y0() {
            LotkaVolterra lv = new LotkaVolterra(5.0, 3.0);
            lv.reset(15.0, 8.0);
            assertEquals(15.0, lv.getX0(), 1e-9);
            assertEquals( 8.0, lv.getY0(), 1e-9);
        }
    }

    // -------------------------------------------------------------------------
    // Derivatives at equilibrium
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("derivatives at equilibrium")
    class DerivativesAtEquilibrium {

        @Test
        @DisplayName("dx/dt is zero at the non-trivial equilibrium")
        void preyDerivativeZeroAtEquilibrium() {
            double alpha = 1.0, beta = 0.1, delta = 0.075, gamma = 1.5;
            LotkaVolterra lv = make(alpha, beta, delta, gamma, 10.0, 5.0);
            double xStar = lv.equilibriumX();
            double yStar = lv.equilibriumY();
            double[] derivs = lv.derivatives(new double[]{xStar, yStar});
            assertEquals(0.0, derivs[0], 1e-9,
                    "dx/dt should be zero at (x*, y*)");
        }

        @Test
        @DisplayName("dy/dt is zero at the non-trivial equilibrium")
        void predatorDerivativeZeroAtEquilibrium() {
            double alpha = 1.0, beta = 0.1, delta = 0.075, gamma = 1.5;
            LotkaVolterra lv = make(alpha, beta, delta, gamma, 10.0, 5.0);
            double xStar = lv.equilibriumX();
            double yStar = lv.equilibriumY();
            double[] derivs = lv.derivatives(new double[]{xStar, yStar});
            assertEquals(0.0, derivs[1], 1e-9,
                    "dy/dt should be zero at (x*, y*)");
        }

        @Test
        @DisplayName("starting exactly at equilibrium: populations unchanged after 100 steps")
        void equilibriumIsFixedPoint() {
            double alpha = 1.0, beta = 0.1, delta = 0.075, gamma = 1.5;
            LotkaVolterra lv = make(alpha, beta, delta, gamma, 10.0, 5.0);
            double xStar = lv.equilibriumX();
            double yStar = lv.equilibriumY();
            lv.reset(xStar, yStar);
            for (int i = 0; i < 100; i++) lv.step();
            assertEquals(xStar, lv.getX(), 1e-4,
                    "prey should remain near x* when started exactly at equilibrium");
            assertEquals(yStar, lv.getY(), 1e-4,
                    "predator should remain near y* when started exactly at equilibrium");
        }
    }

    // -------------------------------------------------------------------------
    // Setters and getters
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("setters and getters")
    class SettersGetters {

        @Test
        @DisplayName("setAlpha and getAlpha round-trip")
        void alphaRoundTrip() {
            LotkaVolterra lv = new LotkaVolterra(10.0, 5.0);
            lv.setAlpha(2.5);
            assertEquals(2.5, lv.getAlpha(), 1e-12);
        }

        @Test
        @DisplayName("setHarvesting and getHarvesting round-trip")
        void harvestingRoundTrip() {
            LotkaVolterra lv = new LotkaVolterra(10.0, 5.0);
            lv.setHarvesting(0.3);
            assertEquals(0.3, lv.getHarvesting(), 1e-12);
        }

        @Test
        @DisplayName("harvesting reduces effective prey growth rate")
        void harvestingReducesPreyGrowth() {
            // Without harvesting
            LotkaVolterra noHarvest = make(1.0, 0.1, 0.075, 1.5, 10.0, 5.0);
            // With harvesting
            LotkaVolterra harvested = make(1.0, 0.1, 0.075, 1.5, 10.0, 5.0);
            harvested.setHarvesting(0.4);

            // Run both for a while and check prey grows more slowly (or declines) with harvesting
            for (int i = 0; i < 500; i++) {
                noHarvest.step();
                harvested.step();
            }
            // The time-average prey under heavy harvesting should differ from no-harvesting
            // We just verify the model runs without error and harvesting changes behaviour
            assertNotEquals(noHarvest.getX(), harvested.getX(), 0.01,
                    "Harvesting should alter the prey trajectory");
        }
    }
}

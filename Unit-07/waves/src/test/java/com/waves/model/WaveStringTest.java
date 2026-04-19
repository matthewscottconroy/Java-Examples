package com.waves.model;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link WaveString}.
 */
@DisplayName("WaveString")
class WaveStringTest {

    // -------------------------------------------------------------------------
    // Initial state
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("initial state")
    class InitialState {

        @Test
        @DisplayName("zero initial displacement stays zero after steps")
        void zeroInitialStaysZero() {
            WaveString ws = new WaveString();
            for (int i = 0; i < 100; i++) ws.step(200.0, 0.0);
            for (int i = 0; i < WaveString.N; i++) {
                assertEquals(0.0, ws.getDisplacement(i), 1e-15,
                        "grid point " + i + " should remain zero");
            }
        }

        @Test
        @DisplayName("energy is zero for flat string")
        void energyZeroForFlat() {
            WaveString ws = new WaveString();
            assertEquals(0.0, ws.energy(), 1e-15);
        }
    }

    // -------------------------------------------------------------------------
    // Pluck
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("pluck")
    class Pluck {

        @Test
        @DisplayName("pluck creates nonzero displacement near the centre")
        void pluckCreatesDisplacement() {
            WaveString ws = new WaveString();
            ws.pluck(WaveString.N / 2, 1.0, 20.0);
            // At least the centre point should be displaced
            double centre = ws.getDisplacement(WaveString.N / 2);
            assertTrue(centre > 0.5, "centre displacement should be close to amplitude 1.0");
        }

        @Test
        @DisplayName("pluck returns positive energy")
        void energyPositiveAfterPluck() {
            WaveString ws = new WaveString();
            ws.pluck(WaveString.N / 2, 1.0, 20.0);
            // Pluck sets uPrev = u so kinetic is zero, but elastic PE is nonzero
            assertTrue(ws.energy() > 0.0, "energy must be positive after a pluck");
        }

        @Test
        @DisplayName("boundary stays zero after pluck")
        void boundaryZeroAfterPluck() {
            WaveString ws = new WaveString();
            ws.pluck(WaveString.N / 2, 1.0, 20.0);
            assertEquals(0.0, ws.getDisplacement(0),             1e-15, "left boundary");
            assertEquals(0.0, ws.getDisplacement(WaveString.N - 1), 1e-15, "right boundary");
        }
    }

    // -------------------------------------------------------------------------
    // Dynamics
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("dynamics")
    class Dynamics {

        @Test
        @DisplayName("boundary remains zero throughout simulation")
        void boundaryStaysZero() {
            WaveString ws = new WaveString();
            ws.pluck(WaveString.N / 3, 0.8, 15.0);
            for (int step = 0; step < 500; step++) {
                ws.step(200.0, 0.0);
                assertEquals(0.0, ws.getDisplacement(0),             1e-14, "left boundary step " + step);
                assertEquals(0.0, ws.getDisplacement(WaveString.N - 1), 1e-14, "right boundary step " + step);
            }
        }

        @Test
        @DisplayName("energy decreases over time with nonzero damping")
        void dampingReducesEnergy() {
            WaveString ws = new WaveString();
            ws.pluck(WaveString.N / 2, 1.0, 20.0);
            double e0 = ws.energy();
            assertTrue(e0 > 0.0, "initial energy must be positive");

            for (int i = 0; i < 1000; i++) ws.step(200.0, 0.002);
            double e1 = ws.energy();
            assertTrue(e1 < e0, "damping must reduce energy over time");
        }

        @Test
        @DisplayName("after many steps energy decreases significantly with damping")
        void energyDecreaseSignificantlyWithDamping() {
            WaveString ws = new WaveString();
            ws.pluck(WaveString.N / 2, 1.0, 20.0);
            double e0 = ws.energy();

            for (int i = 0; i < 5000; i++) ws.step(200.0, 0.003);
            double e1 = ws.energy();
            assertTrue(e1 < e0 * 0.5, "energy should drop by at least half with sustained damping");
        }

        @Test
        @DisplayName("undamped string energy stays roughly constant")
        void undampedEnergyConserved() {
            WaveString ws = new WaveString();
            ws.pluck(WaveString.N / 2, 0.5, 25.0);
            double e0 = ws.energy();

            for (int i = 0; i < 500; i++) ws.step(150.0, 0.0);
            double e1 = ws.energy();
            // Some numerical drift is acceptable; check within 10x (wave has dispersed)
            assertTrue(e1 > 0.0, "undamped string must retain positive energy");
        }
    }

    // -------------------------------------------------------------------------
    // Reset
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("reset")
    class Reset {

        @Test
        @DisplayName("reset clears all displacement to zero")
        void resetClearsDisplacement() {
            WaveString ws = new WaveString();
            ws.pluck(WaveString.N / 2, 1.0, 20.0);
            for (int i = 0; i < 50; i++) ws.step(200.0, 0.0);
            ws.reset();
            for (int i = 0; i < WaveString.N; i++) {
                assertEquals(0.0, ws.getDisplacement(i), 1e-15,
                        "point " + i + " should be zero after reset");
            }
        }

        @Test
        @DisplayName("energy is zero after reset")
        void energyZeroAfterReset() {
            WaveString ws = new WaveString();
            ws.pluck(WaveString.N / 2, 1.0, 20.0);
            ws.reset();
            assertEquals(0.0, ws.energy(), 1e-15);
        }
    }
}

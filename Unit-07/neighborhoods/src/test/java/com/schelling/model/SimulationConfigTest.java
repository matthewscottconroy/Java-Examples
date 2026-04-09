package com.schelling.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link SimulationConfig} and its {@link SimulationConfig.Builder}.
 */
@DisplayName("SimulationConfig")
class SimulationConfigTest {

    @Nested
    @DisplayName("Builder — happy path")
    class HappyPath {

        @Test
        @DisplayName("defaults() produces a valid config")
        void defaultsIsValid() {
            SimulationConfig cfg = SimulationConfig.defaults();
            assertEquals(SimulationConfig.DEFAULT_ROWS,  cfg.getRows());
            assertEquals(SimulationConfig.DEFAULT_COLS,  cfg.getCols());
            assertEquals(SimulationConfig.DEFAULT_SATISFACTION_THRESHOLD,
                cfg.getSatisfactionThreshold(), 1e-9);
            assertEquals(SimulationConfig.DEFAULT_EMPTY_FRACTION,
                cfg.getEmptyFraction(), 1e-9);
        }

        @Test
        @DisplayName("builder fields round-trip correctly")
        void builderRoundTrip() {
            SimulationConfig cfg = new SimulationConfig.Builder()
                .rows(20)
                .cols(30)
                .satisfactionThreshold(0.4)
                .emptyFraction(0.2)
                .typeBFraction(0.6)
                .randomSeed(99L)
                .build();

            assertEquals(20,   cfg.getRows());
            assertEquals(30,   cfg.getCols());
            assertEquals(0.4,  cfg.getSatisfactionThreshold(), 1e-9);
            assertEquals(0.2,  cfg.getEmptyFraction(), 1e-9);
            assertEquals(0.6,  cfg.getTypeBFraction(), 1e-9);
            assertEquals(99L,  cfg.getRandomSeed());
        }
    }

    @Nested
    @DisplayName("Builder — validation")
    class Validation {

        @Test
        @DisplayName("rows < 2 throws IllegalArgumentException")
        void rowsTooSmall() {
            assertThrows(IllegalArgumentException.class,
                () -> new SimulationConfig.Builder().rows(1).build());
        }

        @Test
        @DisplayName("cols < 2 throws IllegalArgumentException")
        void colsTooSmall() {
            assertThrows(IllegalArgumentException.class,
                () -> new SimulationConfig.Builder().cols(1).build());
        }

        @Test
        @DisplayName("threshold < 0 throws IllegalArgumentException")
        void thresholdBelowZero() {
            assertThrows(IllegalArgumentException.class,
                () -> new SimulationConfig.Builder().satisfactionThreshold(-0.01).build());
        }

        @Test
        @DisplayName("threshold > 1 throws IllegalArgumentException")
        void thresholdAboveOne() {
            assertThrows(IllegalArgumentException.class,
                () -> new SimulationConfig.Builder().satisfactionThreshold(1.01).build());
        }

        @Test
        @DisplayName("emptyFraction >= 1 throws IllegalArgumentException")
        void emptyFractionTooLarge() {
            assertThrows(IllegalArgumentException.class,
                () -> new SimulationConfig.Builder().emptyFraction(1.0).build());
        }

        @Test
        @DisplayName("emptyFraction < 0 throws IllegalArgumentException")
        void emptyFractionNegative() {
            assertThrows(IllegalArgumentException.class,
                () -> new SimulationConfig.Builder().emptyFraction(-0.1).build());
        }
    }
}

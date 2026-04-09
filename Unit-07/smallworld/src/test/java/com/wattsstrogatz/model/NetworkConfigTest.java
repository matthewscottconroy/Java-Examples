package com.wattsstrogatz.model;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link NetworkConfig} and its {@link NetworkConfig.Builder}.
 *
 * <p>Covers default values, successful builds, and all validated constraint
 * violations that should throw {@link IllegalArgumentException}.
 */
@DisplayName("NetworkConfig")
class NetworkConfigTest {

    // =========================================================================
    // Defaults
    // =========================================================================

    @Nested
    @DisplayName("Default values")
    class Defaults {

        private final NetworkConfig cfg = NetworkConfig.defaults();

        @Test
        @DisplayName("default node count is 60")
        void defaultNodeCount() {
            assertEquals(NetworkConfig.DEFAULT_NODE_COUNT, cfg.getNodeCount());
        }

        @Test
        @DisplayName("default k is 3")
        void defaultK() {
            assertEquals(NetworkConfig.DEFAULT_K, cfg.getK());
        }

        @Test
        @DisplayName("default rewiring probability is 0.05")
        void defaultP() {
            assertEquals(NetworkConfig.DEFAULT_REWIRING_PROBABILITY,
                cfg.getRewiringProbability(), 1e-12);
        }

        @Test
        @DisplayName("default seed is 42")
        void defaultSeed() {
            assertEquals(NetworkConfig.DEFAULT_RANDOM_SEED, cfg.getRandomSeed());
        }

        @Test
        @DisplayName("getTotalEdges() equals n*k for defaults")
        void defaultTotalEdges() {
            assertEquals(cfg.getNodeCount() * cfg.getK(), cfg.getTotalEdges());
        }
    }

    // =========================================================================
    // Builder — valid configurations
    // =========================================================================

    @Nested
    @DisplayName("Builder — valid builds")
    class ValidBuilds {

        @Test
        @DisplayName("can set n, k, p, seed independently")
        void roundTrip() {
            NetworkConfig cfg = new NetworkConfig.Builder()
                .nodeCount(30).k(4).rewiringProbability(0.25).randomSeed(99L)
                .build();

            assertEquals(30,   cfg.getNodeCount());
            assertEquals(4,    cfg.getK());
            assertEquals(0.25, cfg.getRewiringProbability(), 1e-12);
            assertEquals(99L,  cfg.getRandomSeed());
        }

        @Test
        @DisplayName("p = 0 is a valid edge value")
        void pZero() {
            assertDoesNotThrow(() ->
                new NetworkConfig.Builder().rewiringProbability(0.0).build());
        }

        @Test
        @DisplayName("p = 1 is a valid edge value")
        void pOne() {
            assertDoesNotThrow(() ->
                new NetworkConfig.Builder().rewiringProbability(1.0).build());
        }

        @Test
        @DisplayName("n = 2k+1 is the minimum valid n for k=2")
        void minimumN() {
            // n must be strictly > 2k; for k=2 minimum n is 5
            assertDoesNotThrow(() ->
                new NetworkConfig.Builder().nodeCount(5).k(2).build());
        }

        @Test
        @DisplayName("getTotalEdges() equals n*k for custom config")
        void totalEdges() {
            NetworkConfig cfg = new NetworkConfig.Builder()
                .nodeCount(20).k(3).build();
            assertEquals(60, cfg.getTotalEdges());
        }
    }

    // =========================================================================
    // Builder — constraint violations
    // =========================================================================

    @Nested
    @DisplayName("Builder — constraint violations")
    class ConstraintViolations {

        @Test
        @DisplayName("nodeCount < 4 throws immediately")
        void nodeCountTooSmall() {
            assertThrows(IllegalArgumentException.class,
                () -> new NetworkConfig.Builder().nodeCount(3));
        }

        @Test
        @DisplayName("k < 1 throws immediately")
        void kTooSmall() {
            assertThrows(IllegalArgumentException.class,
                () -> new NetworkConfig.Builder().k(0));
        }

        @Test
        @DisplayName("p < 0 throws immediately")
        void pNegative() {
            assertThrows(IllegalArgumentException.class,
                () -> new NetworkConfig.Builder().rewiringProbability(-0.01));
        }

        @Test
        @DisplayName("p > 1 throws immediately")
        void pTooLarge() {
            assertThrows(IllegalArgumentException.class,
                () -> new NetworkConfig.Builder().rewiringProbability(1.01));
        }

        @Test
        @DisplayName("n <= 2k throws at build() time")
        void nNotGreaterThan2k() {
            // n=4, k=2 → n == 2k → invalid
            assertThrows(IllegalArgumentException.class,
                () -> new NetworkConfig.Builder().nodeCount(4).k(2).build());
        }

        @Test
        @DisplayName("n < 2k throws at build() time")
        void nLessThan2k() {
            // n=5, k=3 → n < 2k → invalid
            assertThrows(IllegalArgumentException.class,
                () -> new NetworkConfig.Builder().nodeCount(5).k(3).build());
        }
    }

    // =========================================================================
    // toString
    // =========================================================================

    @Test
    @DisplayName("toString() contains n, k, p, seed")
    void toStringContainsFields() {
        NetworkConfig cfg = new NetworkConfig.Builder()
            .nodeCount(10).k(2).rewiringProbability(0.5).randomSeed(7L).build();
        String s = cfg.toString();
        assertTrue(s.contains("10"),  "toString should contain n=10");
        assertTrue(s.contains("2"),   "toString should contain k=2");
        assertTrue(s.contains("0.5"), "toString should contain p=0.5");
        assertTrue(s.contains("7"),   "toString should contain seed=7");
    }
}

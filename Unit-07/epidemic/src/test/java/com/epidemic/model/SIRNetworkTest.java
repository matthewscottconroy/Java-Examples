package com.epidemic.model;

import org.junit.jupiter.api.*;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link SIRNetwork}.
 */
@DisplayName("SIRNetwork")
class SIRNetworkTest {

    private static final int    N    = 100;
    private static final int    K    = 6;
    private static final double P    = 0.15;
    private static final long   SEED = 42L;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private SIRNetwork freshModel() {
        WattsStrogatz g = WattsStrogatz.build(N, K, P, SEED);
        SIRNetwork m = new SIRNetwork(g);
        m.reset(3, new Random(7L));
        return m;
    }

    // -------------------------------------------------------------------------
    // Construction / reset
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("construction and reset")
    class ConstructionReset {

        @Test
        @DisplayName("all nodes start as SUSCEPTIBLE before reset")
        void allSusceptibleBeforeReset() {
            WattsStrogatz g = WattsStrogatz.build(N, K, P, SEED);
            SIRNetwork m = new SIRNetwork(g);
            assertEquals(N, m.getSusceptible());
            assertEquals(0, m.getInfected());
            assertEquals(0, m.getRecovered());
        }

        @Test
        @DisplayName("reset seeds exactly seedCount INFECTED nodes")
        void resetSeeds() {
            SIRNetwork m = freshModel();
            assertEquals(3, m.getInfected(),
                    "should have exactly 3 infected after reset(3, ...)");
            assertEquals(N - 3, m.getSusceptible());
            assertEquals(0, m.getRecovered());
        }

        @Test
        @DisplayName("reset can be called multiple times")
        void resetIdempotent() {
            SIRNetwork m = freshModel();
            // Step a bit to change state
            Random rng = new Random();
            for (int t = 0; t < 20; t++) m.step(0.3, 0.05, rng);
            // Reset again
            m.reset(5, new Random(99L));
            assertEquals(5, m.getInfected());
            assertEquals(N - 5, m.getSusceptible());
            assertEquals(0, m.getRecovered());
        }

        @Test
        @DisplayName("getN returns the node count")
        void getNReturnsNodeCount() {
            SIRNetwork m = freshModel();
            assertEquals(N, m.getN());
        }
    }

    // -------------------------------------------------------------------------
    // Conservation: S + I + R = N
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("S+I+R = N invariant")
    class Conservation {

        @Test
        @DisplayName("S+I+R = N immediately after reset")
        void conservedAtReset() {
            SIRNetwork m = freshModel();
            assertEquals(N, m.getSusceptible() + m.getInfected() + m.getRecovered());
        }

        @Test
        @DisplayName("S+I+R = N after 50 steps")
        void conservedDuringEpidemic() {
            SIRNetwork m = freshModel();
            Random rng = new Random(13L);
            for (int t = 0; t < 50; t++) {
                m.step(0.3, 0.05, rng);
                assertEquals(N, m.getSusceptible() + m.getInfected() + m.getRecovered(),
                        "conservation violated at step " + t);
            }
        }

        @Test
        @DisplayName("S+I+R = N after 200 steps with aggressive beta")
        void conservedAggressiveBeta() {
            SIRNetwork m = freshModel();
            Random rng = new Random(21L);
            for (int t = 0; t < 200; t++) {
                m.step(0.8, 0.1, rng);
                assertEquals(N, m.getSusceptible() + m.getInfected() + m.getRecovered(),
                        "conservation violated at step " + t);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Epidemic over
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("isEpidemicOver")
    class EpidemicOver {

        @Test
        @DisplayName("isEpidemicOver is false when infected nodes exist")
        void notOverWithInfected() {
            SIRNetwork m = freshModel();
            assertFalse(m.isEpidemicOver());
        }

        @Test
        @DisplayName("isEpidemicOver is true when no infected nodes remain")
        void overWithNoInfected() {
            // Force all nodes to RECOVERED via reset and manual check
            WattsStrogatz g = WattsStrogatz.build(N, K, P, SEED);
            SIRNetwork m = new SIRNetwork(g);
            // Don't call reset — all are SUSCEPTIBLE, so I=0 → epidemic "over"
            assertTrue(m.isEpidemicOver(),
                    "isEpidemicOver should be true when I=0");
        }

        @Test
        @DisplayName("isEpidemicOver eventually becomes true with beta=0 (no spread)")
        void overEventuallyWithHighGamma() {
            SIRNetwork m = freshModel();
            Random rng = new Random(5L);
            // With beta=0, no new infections; gamma=1.0 → instant recovery
            int steps = 0;
            while (!m.isEpidemicOver() && steps < 1000) {
                m.step(0.0, 1.0, rng);
                steps++;
            }
            assertTrue(m.isEpidemicOver(),
                    "epidemic should end when gamma=1.0 (instant recovery)");
        }
    }

    // -------------------------------------------------------------------------
    // RECOVERED never becomes SUSCEPTIBLE
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("RECOVERED nodes stay RECOVERED")
    class RecoveredStaysRecovered {

        @Test
        @DisplayName("no recovered node ever becomes susceptible again")
        void recoveredPermanent() {
            SIRNetwork m = freshModel();
            Random rng = new Random(77L);

            for (int t = 0; t < 100; t++) {
                // Record which nodes are recovered before this step
                boolean[] wasRecovered = new boolean[N];
                for (int i = 0; i < N; i++) {
                    wasRecovered[i] = m.getState(i) == NodeState.RECOVERED;
                }
                m.step(0.3, 0.05, rng);
                // Check that no previously-recovered node is now susceptible or infected
                for (int i = 0; i < N; i++) {
                    if (wasRecovered[i]) {
                        assertEquals(NodeState.RECOVERED, m.getState(i),
                                "node " + i + " reverted from RECOVERED at step " + t);
                    }
                }
            }
        }
    }
}

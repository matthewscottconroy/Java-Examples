package com.epidemic.model;

import org.junit.jupiter.api.*;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link WattsStrogatz}.
 */
@DisplayName("WattsStrogatz")
class WattsStrogatzTest {

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("construction")
    class Construction {

        @Test
        @DisplayName("builds without exception for standard parameters")
        void buildsStandard() {
            assertDoesNotThrow(() -> WattsStrogatz.build(100, 6, 0.15, 42L));
        }

        @Test
        @DisplayName("builds without exception for small graph")
        void buildsSmall() {
            assertDoesNotThrow(() -> WattsStrogatz.build(10, 2, 0.0, 1L));
        }

        @Test
        @DisplayName("builds for p=0 (pure ring lattice)")
        void buildsPureRing() {
            assertDoesNotThrow(() -> WattsStrogatz.build(50, 4, 0.0, 99L));
        }

        @Test
        @DisplayName("builds for p=1 (maximum rewiring)")
        void buildsFullRewire() {
            assertDoesNotThrow(() -> WattsStrogatz.build(50, 4, 1.0, 7L));
        }

        @Test
        @DisplayName("throws for n < 3")
        void throwsForSmallN() {
            assertThrows(IllegalArgumentException.class,
                    () -> new WattsStrogatz(2, 2, 0.1, new Random()));
        }

        @Test
        @DisplayName("throws for k >= n")
        void throwsForKGeN() {
            assertThrows(IllegalArgumentException.class,
                    () -> new WattsStrogatz(10, 10, 0.1, new Random()));
        }

        @Test
        @DisplayName("throws for odd k")
        void throwsForOddK() {
            assertThrows(IllegalArgumentException.class,
                    () -> new WattsStrogatz(20, 5, 0.1, new Random()));
        }

        @Test
        @DisplayName("throws for p out of [0,1]")
        void throwsForBadP() {
            assertThrows(IllegalArgumentException.class,
                    () -> new WattsStrogatz(20, 4, 1.5, new Random()));
        }
    }

    // -------------------------------------------------------------------------
    // Node count
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("node count")
    class NodeCount {

        @Test
        @DisplayName("nodeCount returns n for n=100")
        void nodeCountCorrect() {
            WattsStrogatz g = WattsStrogatz.build(100, 6, 0.15, 1L);
            assertEquals(100, g.nodeCount());
        }

        @Test
        @DisplayName("nodeCount returns n for n=50")
        void nodeCountSmall() {
            WattsStrogatz g = WattsStrogatz.build(50, 4, 0.0, 2L);
            assertEquals(50, g.nodeCount());
        }

        @Test
        @DisplayName("nodeCount returns n for n=200")
        void nodeCountLarge() {
            WattsStrogatz g = WattsStrogatz.build(200, 6, 0.1, 3L);
            assertEquals(200, g.nodeCount());
        }
    }

    // -------------------------------------------------------------------------
    // Ring lattice degree (p=0)
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("ring lattice (p=0)")
    class RingLattice {

        @Test
        @DisplayName("each node has exactly k neighbours in the ring (p=0)")
        void exactDegreeRing() {
            int n = 30, k = 6;
            WattsStrogatz g = WattsStrogatz.build(n, k, 0.0, 5L);
            for (int i = 0; i < n; i++) {
                assertEquals(k, g.neighbors(i).size(),
                        "node " + i + " should have degree k=" + k + " in ring lattice");
            }
        }

        @Test
        @DisplayName("each node has at least k/2 neighbours after rewiring (p=0.5)")
        void atLeastHalfKAfterRewire() {
            int n = 40, k = 6;
            // With p=0.5 some edges are rewired but degree is preserved
            WattsStrogatz g = WattsStrogatz.build(n, k, 0.5, 13L);
            for (int i = 0; i < n; i++) {
                // Degree can grow as rewired edges are added; it stays >= k/2
                // (in the standard algorithm degree is preserved at exactly k
                //  because each rewired edge replaces exactly one endpoint)
                assertTrue(g.neighbors(i).size() >= k / 2,
                        "node " + i + " should have at least k/2 neighbours");
            }
        }
    }

    // -------------------------------------------------------------------------
    // No self-loops
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("no self-loops")
    class NoSelfLoops {

        @Test
        @DisplayName("no node is its own neighbour (p=0)")
        void noSelfLoopsRing() {
            WattsStrogatz g = WattsStrogatz.build(50, 4, 0.0, 8L);
            for (int i = 0; i < g.nodeCount(); i++) {
                assertFalse(g.neighbors(i).contains(i),
                        "node " + i + " must not be its own neighbour");
            }
        }

        @Test
        @DisplayName("no node is its own neighbour after rewiring (p=0.3)")
        void noSelfLoopsRewired() {
            WattsStrogatz g = WattsStrogatz.build(60, 6, 0.3, 17L);
            for (int i = 0; i < g.nodeCount(); i++) {
                assertFalse(g.neighbors(i).contains(i),
                        "node " + i + " must not be its own neighbour");
            }
        }
    }

    // -------------------------------------------------------------------------
    // Symmetry
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("symmetry")
    class Symmetry {

        @Test
        @DisplayName("adjacency is symmetric: if j in neighbors(i) then i in neighbors(j) (p=0)")
        void symmetricRing() {
            WattsStrogatz g = WattsStrogatz.build(40, 4, 0.0, 21L);
            for (int i = 0; i < g.nodeCount(); i++) {
                for (int j : g.neighbors(i)) {
                    assertTrue(g.neighbors(j).contains(i),
                            "edge (" + i + "," + j + ") must be symmetric");
                }
            }
        }

        @Test
        @DisplayName("adjacency is symmetric after rewiring (p=0.4)")
        void symmetricRewired() {
            WattsStrogatz g = WattsStrogatz.build(60, 6, 0.4, 33L);
            for (int i = 0; i < g.nodeCount(); i++) {
                for (int j : g.neighbors(i)) {
                    assertTrue(g.neighbors(j).contains(i),
                            "edge (" + i + "," + j + ") must be symmetric after rewiring");
                }
            }
        }

        @Test
        @DisplayName("adjacency is symmetric for large graph (p=0.15)")
        void symmetricLarge() {
            WattsStrogatz g = WattsStrogatz.build(150, 8, 0.15, 55L);
            for (int i = 0; i < g.nodeCount(); i++) {
                for (int j : g.neighbors(i)) {
                    assertTrue(g.neighbors(j).contains(i),
                            "edge (" + i + "," + j + ") must be symmetric");
                }
            }
        }
    }
}

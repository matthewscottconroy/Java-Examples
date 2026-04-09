package com.wattsstrogatz.model;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Network} and {@link Edge}.
 */
@DisplayName("Network")
class NetworkTest {

    // =========================================================================
    // Ring lattice construction
    // =========================================================================

    @Nested
    @DisplayName("Ring lattice")
    class RingLattice {

        @Test
        @DisplayName("node count is correct")
        void nodeCount() {
            Network net = Network.ringLattice(20, 2);
            assertEquals(20, net.getNodeCount());
        }

        @Test
        @DisplayName("edge count equals n*k")
        void edgeCount() {
            int n = 20, k = 2;
            Network net = Network.ringLattice(n, k);
            assertEquals(n * k, net.getEdgeCount());
        }

        @Test
        @DisplayName("every node has degree 2k in a ring")
        void uniformDegree() {
            int n = 20, k = 3;
            Network net = Network.ringLattice(n, k);
            for (int i = 0; i < n; i++) {
                assertEquals(2 * k, net.degree(i),
                    "Node " + i + " has wrong degree");
            }
        }

        @Test
        @DisplayName("adjacency is symmetric")
        void symmetric() {
            Network net = Network.ringLattice(10, 2);
            for (int u = 0; u < 10; u++) {
                for (int v : net.neighbours(u)) {
                    assertTrue(net.hasEdge(v, u),
                        "Edge " + u + "-" + v + " is not symmetric");
                }
            }
        }

        @Test
        @DisplayName("throws when n <= 2k")
        void throwsWhenNTooSmall() {
            assertThrows(IllegalArgumentException.class,
                () -> Network.ringLattice(4, 2)); // 4 == 2*2, need n > 2k
        }
    }

    // =========================================================================
    // Rewiring
    // =========================================================================

    @Nested
    @DisplayName("Rewiring")
    class Rewiring {

        @Test
        @DisplayName("rewireEdge updates adjacency correctly")
        void rewireUpdatesAdjacency() {
            Network net = Network.ringLattice(10, 1);
            Edge edge = net.getEdges().get(0); // e.g. 0-1
            int u = edge.getU();
            int oldV = edge.getV();
            // find a node not connected to u
            int newV = -1;
            for (int i = 0; i < 10; i++) {
                if (i != u && !net.hasEdge(u, i)) { newV = i; break; }
            }
            assumeTrue(newV >= 0, "No free target found");

            net.rewireEdge(edge, u, newV);

            assertTrue(net.hasEdge(u, newV), "New edge should exist");
            assertFalse(net.hasEdge(u, oldV), "Old edge should be gone");
        }

        @Test
        @DisplayName("rewired edge is marked as rewired")
        void edgeMarkedRewired() {
            Network net = Network.ringLattice(10, 1);
            Edge edge = net.getEdges().get(0);
            int u = edge.getU();
            int newV = -1;
            for (int i = 0; i < 10; i++) {
                if (i != u && !net.hasEdge(u, i)) { newV = i; break; }
            }
            assumeTrue(newV >= 0, "No free target found");

            net.rewireEdge(edge, u, newV);
            assertTrue(edge.isRewired());
            assertEquals(1, net.getRewiredEdgeCount());
        }

        @Test
        @DisplayName("getRewiredEdgeCount returns 0 on fresh ring")
        void noRewiredOnFreshLattice() {
            Network net = Network.ringLattice(12, 2);
            assertEquals(0, net.getRewiredEdgeCount());
        }
    }

    // =========================================================================
    // Edge
    // =========================================================================

    @Nested
    @DisplayName("Edge")
    class EdgeTests {

        @Test
        @DisplayName("connects() is symmetric")
        void connectsSymmetric() {
            Edge e = new Edge(3, 7);
            assertTrue(e.connects(3, 7));
            assertTrue(e.connects(7, 3));
            assertFalse(e.connects(3, 5));
        }

        @Test
        @DisplayName("new edge is not rewired")
        void notRewiredInitially() {
            assertFalse(new Edge(0, 1).isRewired());
        }

        @Test
        @DisplayName("originalU is preserved after rewire")
        void originalUPreserved() {
            Edge e = new Edge(2, 5);
            e.rewire(2, 9);
            assertEquals(2, e.getOriginalU());
        }
    }

    // =========================================================================
    // Bounds
    // =========================================================================

    @Test
    @DisplayName("out-of-range node throws IndexOutOfBoundsException")
    void outOfBounds() {
        Network net = Network.ringLattice(5, 1);
        assertThrows(IndexOutOfBoundsException.class, () -> net.degree(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> net.degree(5));
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private static void assumeTrue(boolean condition, String message) {
        org.junit.jupiter.api.Assumptions.assumeTrue(condition, message);
    }
}

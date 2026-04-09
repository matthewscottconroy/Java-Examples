package com.wattsstrogatz.model;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link NetworkMetrics}.
 */
@DisplayName("NetworkMetrics")
class NetworkMetricsTest {

    // =========================================================================
    // Clustering coefficient
    // =========================================================================

    @Nested
    @DisplayName("Clustering coefficient")
    class ClusteringCoefficient {

        @Test
        @DisplayName("complete graph K4 has clustering 1.0")
        void completeGraphK4() {
            Network net = new Network(4);
            // Add all 6 edges
            net.addEdge(new Edge(0, 1)); net.addEdge(new Edge(0, 2));
            net.addEdge(new Edge(0, 3)); net.addEdge(new Edge(1, 2));
            net.addEdge(new Edge(1, 3)); net.addEdge(new Edge(2, 3));

            assertEquals(1.0,
                NetworkMetrics.averageClusteringCoefficient(net), 1e-9);
        }

        @Test
        @DisplayName("star graph has clustering 0.0")
        void starGraph() {
            // Hub 0 connected to 1,2,3,4 — no edges between leaves
            Network net = new Network(5);
            net.addEdge(new Edge(0, 1)); net.addEdge(new Edge(0, 2));
            net.addEdge(new Edge(0, 3)); net.addEdge(new Edge(0, 4));
            // Leaves have degree 1 → excluded from average
            // Hub has degree 4 but none of its neighbours are connected
            assertEquals(0.0,
                NetworkMetrics.averageClusteringCoefficient(net), 1e-9);
        }

        @Test
        @DisplayName("local coefficient is -1.0 for degree-1 node")
        void degreeOneNode() {
            Network net = new Network(3);
            net.addEdge(new Edge(0, 1));
            assertEquals(-1.0, NetworkMetrics.localClusteringCoefficient(net, 1), 1e-9);
        }

        @Test
        @DisplayName("ring lattice has expected clustering ~3(k-1)/(2(2k-1))")
        void ringLatticeClusteringFormula() {
            // For n=20, k=2: C₀ = 3(2-1)/(2(2*2-1)) = 3/6 = 0.5
            int n = 20, k = 2;
            Network net = Network.ringLattice(n, k);
            double expected = (3.0 * (k - 1)) / (2.0 * (2 * k - 1));
            double actual   = NetworkMetrics.averageClusteringCoefficient(net);
            assertEquals(expected, actual, 0.01,
                "Ring lattice clustering should match analytical formula");
        }
    }

    // =========================================================================
    // Average shortest path length
    // =========================================================================

    @Nested
    @DisplayName("Average shortest path length")
    class AvgPathLength {

        @Test
        @DisplayName("path graph P4: average = (1+2+3+1+2+1)/6 = 10/6")
        void pathGraphP4() {
            // 0-1-2-3  (path graph)
            Network net = new Network(4);
            net.addEdge(new Edge(0, 1)); net.addEdge(new Edge(1, 2));
            net.addEdge(new Edge(2, 3));

            double avg = NetworkMetrics.averageShortestPathLength(net);
            // Pairs: (0,1)=1,(0,2)=2,(0,3)=3,(1,2)=1,(1,3)=2,(2,3)=1 → 10/6
            assertEquals(10.0 / 6.0, avg, 1e-9);
        }

        @Test
        @DisplayName("complete graph K3 has average path length 1.0")
        void completeK3() {
            Network net = new Network(3);
            net.addEdge(new Edge(0, 1)); net.addEdge(new Edge(0, 2));
            net.addEdge(new Edge(1, 2));
            assertEquals(1.0, NetworkMetrics.averageShortestPathLength(net), 1e-9);
        }

        @Test
        @DisplayName("disconnected graph returns POSITIVE_INFINITY")
        void disconnected() {
            Network net = new Network(4);
            net.addEdge(new Edge(0, 1));   // component {0,1}
            net.addEdge(new Edge(2, 3));   // component {2,3}
            assertEquals(Double.POSITIVE_INFINITY,
                NetworkMetrics.averageShortestPathLength(net));
        }

        @Test
        @DisplayName("BFS distances are correct on simple path")
        void bfsDistances() {
            Network net = new Network(4);
            net.addEdge(new Edge(0, 1)); net.addEdge(new Edge(1, 2));
            net.addEdge(new Edge(2, 3));
            int[] d = NetworkMetrics.bfsDistances(net, 0);
            assertArrayEquals(new int[]{0, 1, 2, 3}, d);
        }
    }

    // =========================================================================
    // Snapshot
    // =========================================================================

    @Test
    @DisplayName("snapshot returns consistent C and L")
    void snapshotConsistent() {
        Network net = Network.ringLattice(16, 2);
        NetworkMetrics.MetricsSnapshot snap = NetworkMetrics.snapshot(net);
        double expectedC = NetworkMetrics.averageClusteringCoefficient(net);
        double expectedL = NetworkMetrics.averageShortestPathLength(net);
        assertEquals(expectedC, snap.getClusteringCoefficient(), 1e-9);
        assertEquals(expectedL, snap.getAvgPathLength(), 1e-9);
    }
}

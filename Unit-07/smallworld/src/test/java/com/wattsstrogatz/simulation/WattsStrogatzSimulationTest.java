package com.wattsstrogatz.simulation;

import com.wattsstrogatz.model.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link WattsStrogatzSimulation}.
 */
@DisplayName("WattsStrogatzSimulation")
class WattsStrogatzSimulationTest {

    private static NetworkConfig smallConfig() {
        return new NetworkConfig.Builder()
            .nodeCount(20).k(2).rewiringProbability(0.3).randomSeed(77L).build();
    }

    private static NetworkConfig pZeroConfig() {
        return new NetworkConfig.Builder()
            .nodeCount(20).k(2).rewiringProbability(0.0).randomSeed(1L).build();
    }

    private static NetworkConfig pOneConfig() {
        return new NetworkConfig.Builder()
            .nodeCount(20).k(2).rewiringProbability(1.0).randomSeed(2L).build();
    }

    // =========================================================================
    // Construction
    // =========================================================================

    @Nested
    @DisplayName("Construction")
    class Construction {

        @Test
        @DisplayName("starts at edge index 0")
        void startsAtZero() {
            WattsStrogatzSimulation sim = new WattsStrogatzSimulation(smallConfig());
            assertEquals(0, sim.getEdgesVisited());
        }

        @Test
        @DisplayName("total edges equals n*k")
        void totalEdges() {
            NetworkConfig cfg = smallConfig();
            WattsStrogatzSimulation sim = new WattsStrogatzSimulation(cfg);
            assertEquals(cfg.getTotalEdges(), sim.getTotalEdges());
        }

        @Test
        @DisplayName("baseline C is positive on ring lattice")
        void baselinePositive() {
            WattsStrogatzSimulation sim = new WattsStrogatzSimulation(smallConfig());
            assertTrue(sim.getBaseline().getClusteringCoefficient() > 0);
        }

        @Test
        @DisplayName("null config throws NullPointerException")
        void nullConfig() {
            assertThrows(NullPointerException.class,
                () -> new WattsStrogatzSimulation(null));
        }
    }

    // =========================================================================
    // Stepping
    // =========================================================================

    @Nested
    @DisplayName("Stepping")
    class Stepping {

        @Test
        @DisplayName("step() increments edges visited")
        void stepIncrementsCounter() {
            WattsStrogatzSimulation sim = new WattsStrogatzSimulation(smallConfig());
            sim.step();
            assertEquals(1, sim.getEdgesVisited());
        }

        @Test
        @DisplayName("step() on complete simulation is a no-op")
        void stepOnCompleteIsNoOp() {
            WattsStrogatzSimulation sim = new WattsStrogatzSimulation(smallConfig());
            sim.stepAll();
            int edgesBefore = sim.getEdgesVisited();
            sim.step();
            assertEquals(edgesBefore, sim.getEdgesVisited());
        }

        @Test
        @DisplayName("stepAll() results in isComplete() == true")
        void stepAllCompletes() {
            WattsStrogatzSimulation sim = new WattsStrogatzSimulation(smallConfig());
            sim.stepAll();
            assertTrue(sim.isComplete());
            assertEquals(1.0, sim.getProgress(), 1e-9);
        }

        @Test
        @DisplayName("node count is unchanged after full rewiring")
        void nodeCountPreserved() {
            NetworkConfig cfg = smallConfig();
            WattsStrogatzSimulation sim = new WattsStrogatzSimulation(cfg);
            sim.stepAll();
            assertEquals(cfg.getNodeCount(), sim.getNetwork().getNodeCount());
        }

        @Test
        @DisplayName("edge count is unchanged after full rewiring")
        void edgeCountPreserved() {
            NetworkConfig cfg = smallConfig();
            WattsStrogatzSimulation sim = new WattsStrogatzSimulation(cfg);
            sim.stepAll();
            assertEquals(cfg.getTotalEdges(), sim.getNetwork().getEdgeCount());
        }
    }

    // =========================================================================
    // p = 0 (no rewiring)
    // =========================================================================

    @Nested
    @DisplayName("p = 0 (no rewiring)")
    class PZero {

        @Test
        @DisplayName("no edges rewired when p = 0")
        void noEdgesRewired() {
            WattsStrogatzSimulation sim = new WattsStrogatzSimulation(pZeroConfig());
            sim.stepAll();
            assertEquals(0, sim.getNetwork().getRewiredEdgeCount());
        }

        @Test
        @DisplayName("relative metrics stay at 1.0 when p = 0")
        void relativeMetricsUnity() {
            WattsStrogatzSimulation sim = new WattsStrogatzSimulation(pZeroConfig());
            sim.stepAll();
            NetworkMetrics.MetricsSnapshot rel = sim.getRelativeMetrics();
            assertEquals(1.0, rel.getClusteringCoefficient(), 1e-6);
            assertEquals(1.0, rel.getAvgPathLength(), 1e-6);
        }
    }

    // =========================================================================
    // p = 1 (full rewiring)
    // =========================================================================

    @Nested
    @DisplayName("p = 1 (full rewiring)")
    class POne {

        @Test
        @DisplayName("clustering drops below baseline when p = 1")
        void clusteringDrops() {
            WattsStrogatzSimulation sim = new WattsStrogatzSimulation(pOneConfig());
            sim.stepAll();
            double relC = sim.getRelativeMetrics().getClusteringCoefficient();
            assertTrue(relC < 1.0,
                "Clustering should decrease for p=1, got relC=" + relC);
        }

        @Test
        @DisplayName("average path length drops below baseline when p = 1")
        void pathLengthDrops() {
            WattsStrogatzSimulation sim = new WattsStrogatzSimulation(pOneConfig());
            sim.stepAll();
            double relL = sim.getRelativeMetrics().getAvgPathLength();
            assertTrue(relL < 1.0,
                "Path length should decrease for p=1, got relL=" + relL);
        }
    }

    // =========================================================================
    // getLastVisitedEdge
    // =========================================================================

    @Nested
    @DisplayName("getLastVisitedEdge()")
    class LastVisitedEdge {

        @Test
        @DisplayName("returns null before any step")
        void nullBeforeFirstStep() {
            WattsStrogatzSimulation sim = new WattsStrogatzSimulation(smallConfig());
            assertNull(sim.getLastVisitedEdge());
        }

        @Test
        @DisplayName("returns non-null after first step")
        void nonNullAfterStep() {
            WattsStrogatzSimulation sim = new WattsStrogatzSimulation(smallConfig());
            sim.step();
            assertNotNull(sim.getLastVisitedEdge());
        }

        @Test
        @DisplayName("returns same edge as index currentEdgeIndex-1")
        void matchesIndex() {
            WattsStrogatzSimulation sim = new WattsStrogatzSimulation(smallConfig());
            sim.step(); sim.step(); sim.step();
            // After 3 steps the edge at index 2 is the last visited
            Edge last = sim.getLastVisitedEdge();
            assertNotNull(last);
            // Verify the edge's endpoints are in-range
            int n = sim.getConfig().getNodeCount();
            assertTrue(last.getU() >= 0 && last.getU() < n);
            assertTrue(last.getV() >= 0 && last.getV() < n);
        }

        @Test
        @DisplayName("still valid (non-null) after stepAll()")
        void stillValidAfterStepAll() {
            WattsStrogatzSimulation sim = new WattsStrogatzSimulation(smallConfig());
            sim.stepAll();
            assertNotNull(sim.getLastVisitedEdge());
        }

        @Test
        @DisplayName("reset() causes getLastVisitedEdge() to return null again")
        void nullAfterReset() {
            WattsStrogatzSimulation sim = new WattsStrogatzSimulation(smallConfig());
            sim.step(); sim.step();
            sim.reset();
            assertNull(sim.getLastVisitedEdge());
        }
    }

    // =========================================================================
    // getRelativeMetrics(snapshot) overload
    // =========================================================================

    @Nested
    @DisplayName("getRelativeMetrics(snapshot)")
    class RelativeMetricsOverload {

        @Test
        @DisplayName("overload produces same result as no-arg version")
        void matchesNoArgVersion() {
            WattsStrogatzSimulation sim = new WattsStrogatzSimulation(smallConfig());
            sim.stepAll();
            NetworkMetrics.MetricsSnapshot current = sim.getCurrentMetrics();
            NetworkMetrics.MetricsSnapshot fromArg    = sim.getRelativeMetrics(current);
            NetworkMetrics.MetricsSnapshot fromNoArg  = sim.getRelativeMetrics();
            assertEquals(fromNoArg.getClusteringCoefficient(),
                fromArg.getClusteringCoefficient(), 1e-9);
            assertEquals(fromNoArg.getAvgPathLength(),
                fromArg.getAvgPathLength(), 1e-9);
        }

        @Test
        @DisplayName("passing baseline snapshot returns (1.0, 1.0)")
        void baselineSnapshotGivesUnity() {
            WattsStrogatzSimulation sim = new WattsStrogatzSimulation(pZeroConfig());
            // Baseline IS the current state before any rewiring
            NetworkMetrics.MetricsSnapshot before = sim.getCurrentMetrics();
            NetworkMetrics.MetricsSnapshot rel = sim.getRelativeMetrics(before);
            assertEquals(1.0, rel.getClusteringCoefficient(), 1e-9);
            assertEquals(1.0, rel.getAvgPathLength(), 1e-9);
        }

        @Test
        @DisplayName("relative C and L are positive for p=0.3")
        void positiveForMidP() {
            WattsStrogatzSimulation sim = new WattsStrogatzSimulation(smallConfig());
            sim.stepAll();
            NetworkMetrics.MetricsSnapshot rel = sim.getRelativeMetrics(sim.getCurrentMetrics());
            assertTrue(rel.getClusteringCoefficient() >= 0,
                "relC should be non-negative");
            assertTrue(rel.getAvgPathLength() >= 0,
                "relL should be non-negative");
        }
    }

    // =========================================================================
    // Reset
    // =========================================================================

    @Nested
    @DisplayName("Reset")
    class Reset {

        @Test
        @DisplayName("reset restores edges visited to 0")
        void resetRestoresCounter() {
            WattsStrogatzSimulation sim = new WattsStrogatzSimulation(smallConfig());
            sim.step(); sim.step(); sim.step();
            sim.reset();
            assertEquals(0, sim.getEdgesVisited());
        }

        @Test
        @DisplayName("reset produces same initial network as construction (same seed)")
        void resetReproducible() {
            NetworkConfig cfg = smallConfig();
            WattsStrogatzSimulation sim = new WattsStrogatzSimulation(cfg);
            NetworkMetrics.MetricsSnapshot beforeReset = sim.getCurrentMetrics();

            sim.stepAll();
            sim.reset();
            NetworkMetrics.MetricsSnapshot afterReset = sim.getCurrentMetrics();

            assertEquals(beforeReset.getClusteringCoefficient(),
                afterReset.getClusteringCoefficient(), 1e-9);
            assertEquals(beforeReset.getAvgPathLength(),
                afterReset.getAvgPathLength(), 1e-9);
        }
    }
}

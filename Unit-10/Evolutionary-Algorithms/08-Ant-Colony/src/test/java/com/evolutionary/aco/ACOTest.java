package com.evolutionary.aco;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ACOTest {

    // 4-city symmetric graph with known optimal tour
    // Optimal: 0→1→3→2→0 or any rotation, length = 2+4+3+1 = 10... let's check:
    // Edges: 0-1=2, 0-2=1, 0-3=5, 1-2=6, 1-3=4, 2-3=3
    // Tours (return to start):
    //   0→1→2→3→0: 2+6+3+5 = 16
    //   0→1→3→2→0: 2+4+3+1 = 10  ← optimal
    //   0→2→1→3→0: 1+6+4+5 = 16
    static final Graph SMALL_GRAPH = Graph.of(new int[][]{
        { 0, 2, 1, 5 },
        { 2, 0, 6, 4 },
        { 1, 6, 0, 3 },
        { 5, 4, 3, 0 }
    });

    static ACO.Config defaultConfig(long seed) {
        return new ACO.Config(10, 200, 1.0, 2.0, 0.1, 100.0, 0.1, seed);
    }

    // ---------------------------------------------------------------
    // Graph
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Graph: weight is symmetric")
    void graph_symmetric() {
        assertEquals(SMALL_GRAPH.weight(0, 1), SMALL_GRAPH.weight(1, 0));
        assertEquals(SMALL_GRAPH.weight(2, 3), SMALL_GRAPH.weight(3, 2));
    }

    @Test
    @DisplayName("Graph: hasEdge returns false for self-loops (weight 0)")
    void graph_selfLoopNotEdge() {
        assertFalse(SMALL_GRAPH.hasEdge(0, 0));
        assertFalse(SMALL_GRAPH.hasEdge(2, 2));
    }

    // ---------------------------------------------------------------
    // Tour validity
    // ---------------------------------------------------------------

    @Test
    @DisplayName("ACO tour visits every city exactly once")
    void tour_visitsAllCities() {
        ACO aco = new ACO(defaultConfig(0L), SMALL_GRAPH);
        ACO.Result result = aco.solve();

        int[] tour = result.tour();
        assertEquals(4, tour.length, "Tour must contain all 4 cities");

        Set<Integer> visited = new HashSet<>();
        for (int city : tour) visited.add(city);
        assertEquals(4, visited.size(), "Each city must appear exactly once");
    }

    @Test
    @DisplayName("ACO reported tour length matches actual computed length")
    void tour_lengthMatchesComputed() {
        ACO aco = new ACO(defaultConfig(0L), SMALL_GRAPH);
        ACO.Result result = aco.solve();

        int[] tour = result.tour();
        double computed = 0;
        for (int i = 0; i < tour.length; i++) {
            computed += SMALL_GRAPH.weight(tour[i], tour[(i + 1) % tour.length]);
        }

        assertEquals(computed, result.length(), 1e-9, "Reported length must match computed length");
    }

    // ---------------------------------------------------------------
    // Optimality on small instance
    // ---------------------------------------------------------------

    @Test
    @DisplayName("ACO finds near-optimal tour on 4-city instance (length ≤ 14)")
    void smallGraph_nearOptimal() {
        // Optimal is 10; we allow some slack for stochastic variation
        ACO aco = new ACO(defaultConfig(7L), SMALL_GRAPH);
        ACO.Result result = aco.solve();
        assertTrue(result.length() <= 14.0,
            "Expected tour ≤ 14, got " + result.length());
    }

    @Test
    @DisplayName("ACO finds optimal tour on 4-city instance with multiple seeds")
    void smallGraph_optimal_multipleSeeds() {
        // At least one of these seeds should hit the optimal tour of 10
        double best = Double.MAX_VALUE;
        for (long seed = 0; seed < 5; seed++) {
            ACO aco = new ACO(defaultConfig(seed), SMALL_GRAPH);
            ACO.Result result = aco.solve();
            best = Math.min(best, result.length());
        }
        assertEquals(10.0, best, 1e-9, "At least one seed should find the optimal tour of 10");
    }

    // ---------------------------------------------------------------
    // Pheromone
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Pheromone on edges used by ants grows above initial value")
    void pheromone_growsOnUsedEdges() {
        ACO aco = new ACO(defaultConfig(0L), SMALL_GRAPH);
        aco.solve();

        double[][] ph = aco.pheromone();
        double maxPheromone = 0;
        for (double[] row : ph) for (double v : row) maxPheromone = Math.max(maxPheromone, v);
        assertTrue(maxPheromone > 0.1, "Pheromone on used edges must exceed initial value 0.1");
    }

    @Test
    @DisplayName("Pheromone matrix is symmetric after solving")
    void pheromone_isSymmetric() {
        ACO aco = new ACO(defaultConfig(0L), SMALL_GRAPH);
        aco.solve();

        double[][] ph = aco.pheromone();
        for (int i = 0; i < ph.length; i++) {
            for (int j = 0; j < ph.length; j++) {
                assertEquals(ph[i][j], ph[j][i], 1e-9,
                    "Pheromone matrix must be symmetric at (" + i + "," + j + ")");
            }
        }
    }

    // ---------------------------------------------------------------
    // Larger instance
    // ---------------------------------------------------------------

    @Test
    @DisplayName("ACO solves 8-city network routing instance in reasonable time")
    void eightCityNetwork_completesAndIsValid() {
        Graph g = Graph.of(new int[][]{
            {  0, 12,  6,  9, 14, 22, 18, 24 },
            { 12,  0,  8, 10, 20, 18,  6, 10 },
            {  6,  8,  0,  8, 15, 20, 14, 18 },
            {  9, 10,  8,  0, 16, 12, 11, 16 },
            { 14, 20, 15, 16,  0, 25, 22, 28 },
            { 22, 18, 20, 12, 25,  0, 14, 19 },
            { 18,  6, 14, 11, 22, 14,  0,  6 },
            { 24, 10, 18, 16, 28, 19,  6,  0 },
        });

        ACO.Config config = new ACO.Config(20, 300, 1.0, 2.0, 0.1, 100.0, 0.1, 42L);
        ACO aco = new ACO(config, g);
        ACO.Result result = aco.solve();

        // All 8 cities visited exactly once
        Set<Integer> visited = new HashSet<>();
        for (int city : result.tour()) visited.add(city);
        assertEquals(8, visited.size());

        // Some reasonable upper bound — naive random tour would average much higher
        assertTrue(result.length() < 120.0,
            "8-city tour should be under 120ms latency, got " + result.length());
    }
}

package com.evolutionary.aco;

/**
 * Network routing optimiser — finds the shortest round-trip through 8 data
 * centres using Ant Colony Optimisation.
 *
 * <p>Distances (latency in ms) between data centres are given as a symmetric
 * weight matrix. ACO discovers the TSP tour that minimises total round-trip
 * latency — the order to visit each data centre exactly once and return home.
 */
public class Main {

    static final String[] NODES = {
        "London", "Frankfurt", "Amsterdam", "Paris",
        "Dublin",  "Madrid",   "Zurich",    "Milan"
    };

    // Symmetric latency matrix (ms). 0 = no direct link (same node or missing).
    static final int[][] LATENCY = {
        //  LON  FRA  AMS  PAR  DUB  MAD  ZUR  MIL
        {    0,  12,   6,   9,  14,  22,  18,  24 },  // London
        {   12,   0,   8,  10,  20,  18,   6,  10 },  // Frankfurt
        {    6,   8,   0,   8,  15,  20,  14,  18 },  // Amsterdam
        {    9,  10,   8,   0,  16,  12,  11,  16 },  // Paris
        {   14,  20,  15,  16,   0,  25,  22,  28 },  // Dublin
        {   22,  18,  20,  12,  25,   0,  14,  19 },  // Madrid
        {   18,   6,  14,  11,  22,  14,   0,   6 },  // Zurich
        {   24,  10,  18,  16,  28,  19,   6,   0 },  // Milan
    };

    public static void main(String[] args) {
        Graph graph = Graph.of(LATENCY);

        ACO.Config config = new ACO.Config(
            20,     // numAnts
            300,    // iterations
            1.0,    // alpha (pheromone influence)
            2.0,    // beta  (heuristic influence)
            0.1,    // rho   (evaporation rate)
            100.0,  // q     (deposit constant)
            0.1,    // initialPheromone
            42L
        );

        ACO aco = new ACO(config, graph);
        ACO.Result result = aco.solve();

        System.out.println("=== Ant Colony Optimisation — Network Router ===\n");
        System.out.println("Data centres: " + String.join(", ", NODES));
        System.out.println();
        System.out.print("Best tour: ");
        StringBuilder route = new StringBuilder();
        for (int i = 0; i < result.tour().length; i++) {
            if (i > 0) route.append(" → ");
            route.append(NODES[result.tour()[i]]);
        }
        route.append(" → ").append(NODES[result.tour()[0]]);
        System.out.println(route);
        System.out.printf("Total latency: %.0f ms%n", result.length());
        System.out.printf("Iterations:    %d%n", result.iterations());
    }
}

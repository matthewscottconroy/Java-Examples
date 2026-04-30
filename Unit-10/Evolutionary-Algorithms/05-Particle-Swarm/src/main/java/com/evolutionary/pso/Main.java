package com.evolutionary.pso;

/**
 * Demonstrates PSO on a WiFi access point placement problem.
 *
 * A building's floor plan is modelled as a 100×100 grid. Three access points
 * must be placed to maximise total signal coverage. PSO finds the optimal
 * (x, y) coordinates for each AP.
 *
 * The fitness function rewards placing APs far apart (maximising coverage area)
 * while penalising placement outside the building bounds.
 */
public class Main {

    static final int BUILDING_W = 100, BUILDING_H = 100;
    static final int NUM_APS = 3;

    public static void main(String[] args) {
        System.out.println("=== Particle Swarm Optimisation — WiFi Planner ===\n");
        System.out.printf("Building: %dx%d, placing %d access points%n%n",
            BUILDING_W, BUILDING_H, NUM_APS);

        // Chromosome: [x1, y1, x2, y2, x3, y3]
        int dim = NUM_APS * 2;
        double[] lo = new double[dim], hi = new double[dim];
        for (int i = 0; i < dim; i += 2) {
            lo[i] = 0; hi[i] = BUILDING_W;
            lo[i+1] = 0; hi[i+1] = BUILDING_H;
        }

        PSO.Config config = new PSO.Config(
            /*swarmSize*/       50,
            /*dimensions*/      dim,
            /*lowerBound*/      lo,
            /*upperBound*/      hi,
            /*inertia*/         0.72,
            /*cognitiveCoeff*/  1.49,
            /*socialCoeff*/     1.49,
            /*maxVelocityFrac*/ 0.2
        );

        PSO pso = new PSO(config, Main::coverage, 42L);
        PSO.Result result = pso.optimise(500);

        System.out.println("Optimal AP placement:");
        double[] pos = result.position();
        for (int i = 0; i < NUM_APS; i++) {
            System.out.printf("  AP%d: (%.1f, %.1f)%n", i + 1, pos[i*2], pos[i*2+1]);
        }
        System.out.printf("Coverage score: %.2f%n", result.fitness());
        System.out.printf("Function evaluations: %,d%n", result.iterations());

        // Benchmark: Sphere function
        System.out.println("\n--- Benchmark: 5D Sphere (minimum at origin) ---");
        double[] sLo = {-10,-10,-10,-10,-10}, sHi = {10,10,10,10,10};
        PSO.Config sConfig = new PSO.Config(30, 5, sLo, sHi, 0.72, 1.49, 1.49, 0.2);
        PSO spherePSO = new PSO(sConfig, x -> {
            double s = 0; for (double xi : x) s += xi*xi; return -s;
        }, 1L);
        PSO.Result sResult = spherePSO.optimise(200);
        System.out.printf("  Sphere value: %.6f (optimum = 0)%n", -sResult.fitness());
    }

    /**
     * Coverage fitness: rewards APs being spread out across the building.
     * Uses pairwise distances between APs as a proxy for total coverage area.
     */
    static double coverage(double[] pos) {
        double score = 0;
        for (int i = 0; i < NUM_APS; i++) {
            for (int j = i + 1; j < NUM_APS; j++) {
                double dx = pos[i*2] - pos[j*2];
                double dy = pos[i*2+1] - pos[j*2+1];
                score += Math.sqrt(dx*dx + dy*dy);
            }
        }
        return score;
    }
}

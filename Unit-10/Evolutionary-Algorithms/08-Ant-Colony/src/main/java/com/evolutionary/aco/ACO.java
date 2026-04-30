package com.evolutionary.aco;

import java.util.*;

/**
 * Ant Colony Optimisation for the Travelling Salesman Problem.
 *
 * <p>ACO is a population-based metaheuristic inspired by the foraging behaviour
 * of ants. Real ants deposit pheromone on paths they travel; shorter paths
 * accumulate more pheromone (more ants per unit time), biasing future ants.
 * ACO models this with a pheromone matrix that is updated after each iteration.
 *
 * <h2>Decision rule</h2>
 * At each step an ant at node {@code i} chooses next node {@code j} with probability:
 * <pre>
 *   P(i→j) ∝ τ(i,j)^α × η(i,j)^β
 * </pre>
 * where τ is pheromone strength and η = 1/distance is the heuristic desirability.
 * α controls how much ants trust pheromone; β controls how much they trust distance.
 *
 * <h2>Pheromone update</h2>
 * <pre>
 *   τ(i,j) ← (1-ρ) × τ(i,j) + Σ Δτ_k(i,j)
 * </pre>
 * Evaporation rate ρ ∈ (0,1) prevents indefinite accumulation.
 * Deposit Δτ_k = Q/tourLength for each ant k that used edge (i,j).
 */
public class ACO {

    public record Config(
        int numAnts,
        int iterations,
        double alpha,       // pheromone influence
        double beta,        // heuristic influence
        double rho,         // evaporation rate
        double q,           // pheromone deposit constant
        double initialPheromone,
        long seed
    ) {}

    public record Result(int[] tour, double length, int iterations) {
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < tour.length; i++) {
                if (i > 0) sb.append(" → ");
                sb.append(tour[i]);
            }
            sb.append(" → ").append(tour[0]);
            return sb + " (length=" + String.format("%.2f", length) + ")";
        }
    }

    private final Config config;
    private final Graph graph;
    private final Random rng;

    private double[][] pheromone;

    public ACO(Config config, Graph graph) {
        this.config = config;
        this.graph  = graph;
        this.rng    = new Random(config.seed());
    }

    public Result solve() {
        initialisePheromone();
        int n = graph.size();

        int[] bestTour = null;
        double bestLength = Double.MAX_VALUE;

        for (int iter = 0; iter < config.iterations(); iter++) {
            int[][] tours    = new int[config.numAnts()][n];
            double[] lengths = new double[config.numAnts()];

            for (int k = 0; k < config.numAnts(); k++) {
                tours[k]   = buildTour(rng.nextInt(n));
                lengths[k] = tourLength(tours[k]);

                if (lengths[k] < bestLength) {
                    bestLength = lengths[k];
                    bestTour   = tours[k].clone();
                }
            }

            evaporate();
            deposit(tours, lengths);
        }

        return new Result(bestTour, bestLength, config.iterations());
    }

    private void initialisePheromone() {
        int n = graph.size();
        pheromone = new double[n][n];
        for (double[] row : pheromone) Arrays.fill(row, config.initialPheromone());
    }

    private int[] buildTour(int start) {
        int n = graph.size();
        int[] tour = new int[n];
        boolean[] visited = new boolean[n];

        tour[0] = start;
        visited[start] = true;

        for (int step = 1; step < n; step++) {
            int current = tour[step - 1];
            tour[step] = chooseNext(current, visited);
            visited[tour[step]] = true;
        }

        return tour;
    }

    private int chooseNext(int current, boolean[] visited) {
        int n = graph.size();
        double[] prob = new double[n];
        double total = 0;

        for (int j = 0; j < n; j++) {
            if (!visited[j] && graph.hasEdge(current, j)) {
                double tau = Math.pow(pheromone[current][j], config.alpha());
                double eta = Math.pow(1.0 / graph.weight(current, j), config.beta());
                prob[j] = tau * eta;
                total += prob[j];
            }
        }

        if (total == 0) {
            // No pheromone signal — pick uniformly among unvisited reachable nodes
            for (int j = 0; j < n; j++) {
                if (!visited[j] && graph.hasEdge(current, j)) return j;
            }
            // Fallback: any unvisited node (disconnected graph)
            for (int j = 0; j < n; j++) if (!visited[j]) return j;
        }

        double r = rng.nextDouble() * total;
        double cumulative = 0;
        for (int j = 0; j < n; j++) {
            cumulative += prob[j];
            if (r <= cumulative) return j;
        }
        // rounding safety
        for (int j = 0; j < n; j++) if (!visited[j]) return j;
        return -1;
    }

    private double tourLength(int[] tour) {
        double len = 0;
        int n = tour.length;
        for (int i = 0; i < n; i++) {
            len += graph.weight(tour[i], tour[(i + 1) % n]);
        }
        return len;
    }

    private void evaporate() {
        int n = graph.size();
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                pheromone[i][j] *= (1.0 - config.rho());
    }

    private void deposit(int[][] tours, double[] lengths) {
        for (int k = 0; k < tours.length; k++) {
            double delta = config.q() / lengths[k];
            int n = tours[k].length;
            for (int step = 0; step < n; step++) {
                int i = tours[k][step];
                int j = tours[k][(step + 1) % n];
                pheromone[i][j] += delta;
                pheromone[j][i] += delta;
            }
        }
    }

    /** Exposes pheromone matrix for testing. */
    double[][] pheromone() { return pheromone; }
}

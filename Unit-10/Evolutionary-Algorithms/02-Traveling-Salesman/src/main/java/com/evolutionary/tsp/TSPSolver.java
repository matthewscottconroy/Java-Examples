package com.evolutionary.tsp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Genetic algorithm for the Traveling Salesman Problem.
 *
 * <p>Key adaptations for permutation chromosomes:
 * <ul>
 *   <li><b>Ordered Crossover (OX)</b> — preserves relative order of cities
 *       from each parent while avoiding duplicates.</li>
 *   <li><b>Swap Mutation</b> — swaps two random positions instead of bit-flip,
 *       since flipping a bit would produce an invalid permutation.</li>
 * </ul>
 */
public class TSPSolver {

    private final int    populationSize;
    private final double mutationRate;
    private final int    tournamentSize;
    private final Random rng;

    public TSPSolver(int populationSize, double mutationRate, int tournamentSize, long seed) {
        this.populationSize = populationSize;
        this.mutationRate   = mutationRate;
        this.tournamentSize = tournamentSize;
        this.rng            = new Random(seed);
    }

    public Route solve(List<City> cities, int generations) {
        List<Route> population = new ArrayList<>(populationSize);
        for (int i = 0; i < populationSize; i++) population.add(Route.random(cities, rng));

        for (int gen = 0; gen < generations; gen++) {
            List<Route> nextGen = new ArrayList<>(populationSize);
            nextGen.add(best(population));  // elitism

            while (nextGen.size() < populationSize) {
                Route p1 = tournamentSelect(population);
                Route p2 = tournamentSelect(population);
                Route child = orderedCrossover(p1, p2);
                child = swapMutate(child);
                nextGen.add(child);
            }
            population = nextGen;
        }
        return best(population);
    }

    private Route best(List<Route> pop) {
        return pop.stream().max(Comparator.comparingDouble(Route::fitness)).orElseThrow();
    }

    private Route tournamentSelect(List<Route> pop) {
        Route best = null;
        for (int i = 0; i < tournamentSize; i++) {
            Route candidate = pop.get(rng.nextInt(pop.size()));
            if (best == null || candidate.fitness() > best.fitness()) best = candidate;
        }
        return best;
    }

    /**
     * Ordered Crossover (OX):
     * 1. Copy a random sub-segment from parent1 into the child.
     * 2. Fill remaining positions with cities from parent2 in order,
     *    skipping cities already in the child.
     */
    private Route orderedCrossover(Route p1, Route p2) {
        int n = p1.order().length;
        int[] child = new int[n];
        Arrays.fill(child, -1);

        int start = rng.nextInt(n), end = rng.nextInt(n);
        if (start > end) { int t = start; start = end; end = t; }

        for (int i = start; i <= end; i++) child[i] = p1.order()[i];

        int pos = (end + 1) % n;
        for (int i = 0; i < n; i++) {
            int city = p2.order()[(end + 1 + i) % n];
            if (!contains(child, city)) {
                child[pos] = city;
                pos = (pos + 1) % n;
            }
        }
        return new Route(child, p1.cities());
    }

    private boolean contains(int[] arr, int val) {
        for (int v : arr) if (v == val) return true;
        return false;
    }

    /** Swap mutation: swap two random positions in the permutation. */
    private Route swapMutate(Route route) {
        if (rng.nextDouble() >= mutationRate) return route;
        int[] order = route.order().clone();
        int i = rng.nextInt(order.length), j = rng.nextInt(order.length);
        int tmp = order[i]; order[i] = order[j]; order[j] = tmp;
        return new Route(order, route.cities());
    }
}

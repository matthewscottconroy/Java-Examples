package com.evolutionary.tsp;

import java.util.List;
import java.util.Random;

/**
 * Demonstrates TSP on a courier delivery route across 15 city locations.
 */
public class Main {

    public static final List<City> CITIES = List.of(
        new City("Warehouse",   10, 10),
        new City("Supermarket", 25, 40),
        new City("Hospital",    60, 20),
        new City("School",      45, 65),
        new City("Library",     80, 45),
        new City("Park",        35, 80),
        new City("Stadium",     70, 75),
        new City("Airport",     90, 15),
        new City("Mall",        55, 35),
        new City("Museum",      20, 60),
        new City("Hotel",       75, 55),
        new City("Station",     40, 25),
        new City("University",  65, 85),
        new City("Clinic",      15, 85),
        new City("Factory",     85, 30)
    );

    public static void main(String[] args) {
        System.out.println("=== Traveling Salesman — Delivery Route Optimizer ===\n");
        System.out.println("Locations: " + CITIES.size());

        // Random baseline
        Random rng = new Random(0);
        double worstDist = Double.MIN_VALUE, bestRandom = Double.MAX_VALUE;
        for (int i = 0; i < 1000; i++) {
            double d = Route.random(CITIES, rng).totalDistance();
            if (d < bestRandom) bestRandom = d;
            if (d > worstDist) worstDist = d;
        }
        System.out.printf("Random routes (1000 trials):  best=%.1f  worst=%.1f%n%n",
            bestRandom, worstDist);

        // GA
        TSPSolver solver = new TSPSolver(
            /*populationSize*/ 200,
            /*mutationRate*/   0.15,
            /*tournamentSize*/ 5,
            /*seed*/           42L
        );

        Route best = solver.solve(CITIES, 500);
        System.out.printf("GA result after 500 generations:%n");
        System.out.printf("  Distance:  %.2f%n", best.totalDistance());
        System.out.printf("  Improvement over random best: %.1f%%%n",
            (bestRandom - best.totalDistance()) / bestRandom * 100);
        System.out.println("\nRoute:");
        for (int idx : best.order()) {
            City c = CITIES.get(idx);
            System.out.printf("  → %-15s (%.0f, %.0f)%n", c.name(), c.x(), c.y());
        }
        System.out.printf("  → %s (return)%n", CITIES.get(best.order()[0]).name());
    }
}

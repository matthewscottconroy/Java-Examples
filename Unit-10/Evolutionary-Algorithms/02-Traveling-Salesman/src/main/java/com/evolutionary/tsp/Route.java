package com.evolutionary.tsp;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * A permutation chromosome representing a delivery route.
 *
 * The genome is an array of city indices — the order in which cities are visited.
 * The route is a closed loop: the last city returns to the first.
 */
public record Route(int[] order, List<City> cities) {

    /** Creates a random route (random permutation of city indices). */
    public static Route random(List<City> cities, Random rng) {
        int n = cities.size();
        int[] order = new int[n];
        for (int i = 0; i < n; i++) order[i] = i;
        // Fisher-Yates shuffle
        for (int i = n - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = order[i]; order[i] = order[j]; order[j] = tmp;
        }
        return new Route(order, cities);
    }

    /** Total distance of the closed tour. */
    public double totalDistance() {
        double dist = 0;
        int n = order.length;
        for (int i = 0; i < n; i++) {
            dist += cities.get(order[i]).distanceTo(cities.get(order[(i + 1) % n]));
        }
        return dist;
    }

    /** Fitness = 1 / totalDistance (shorter routes score higher). */
    public double fitness() { return 1.0 / totalDistance(); }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int idx : order) sb.append(cities.get(idx).name()).append(" → ");
        sb.append(cities.get(order[0]).name());  // return to start
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Route r)) return false;
        return Arrays.equals(order, r.order);
    }

    @Override
    public int hashCode() { return Arrays.hashCode(order); }
}

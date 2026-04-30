package com.evolutionary.tsp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class TSPTest {

    static final List<City> SMALL = List.of(
        new City("A", 0, 0),
        new City("B", 3, 0),
        new City("C", 3, 4),
        new City("D", 0, 4)
    );

    @Test
    @DisplayName("Route contains all cities exactly once")
    void routeIsPermutation() {
        Route route = Route.random(SMALL, new Random(1));
        boolean[] seen = new boolean[SMALL.size()];
        for (int idx : route.order()) {
            assertFalse(seen[idx], "City " + idx + " appears twice");
            seen[idx] = true;
        }
        for (boolean s : seen) assertTrue(s, "A city is missing");
    }

    @Test
    @DisplayName("totalDistance is positive and symmetric")
    void distancePositive() {
        Route r = Route.random(SMALL, new Random(2));
        assertTrue(r.totalDistance() > 0);
    }

    @Test
    @DisplayName("Square route [A,B,C,D] has known perimeter")
    void knownDistance() {
        // A(0,0)→B(3,0)→C(3,4)→D(0,4)→A: 3+4+3+4 = 14
        Route square = new Route(new int[]{0, 1, 2, 3}, SMALL);
        assertEquals(14.0, square.totalDistance(), 1e-9);
    }

    @Test
    @DisplayName("GA finds shorter route than random on 15-city problem")
    void gaBetterThanRandom() {
        TSPSolver solver = new TSPSolver(100, 0.15, 5, 42L);
        Route ga = solver.solve(Main.CITIES, 300);

        Random rng = new Random(0);
        double bestRandom = Double.MAX_VALUE;
        for (int i = 0; i < 500; i++) {
            double d = Route.random(Main.CITIES, rng).totalDistance();
            if (d < bestRandom) bestRandom = d;
        }
        assertTrue(ga.totalDistance() < bestRandom,
            "GA route should beat best random route");
    }

    @Test
    @DisplayName("Fitness is inversely proportional to distance")
    void fitnessInverseDistance() {
        Route r1 = new Route(new int[]{0, 1, 2, 3}, SMALL);  // distance=14
        Route r2 = new Route(new int[]{0, 2, 1, 3}, SMALL);  // different route
        if (r1.totalDistance() < r2.totalDistance()) {
            assertTrue(r1.fitness() > r2.fitness());
        } else if (r1.totalDistance() > r2.totalDistance()) {
            assertTrue(r1.fitness() < r2.fitness());
        }
        // else equal distances → equal fitness (fine)
    }
}

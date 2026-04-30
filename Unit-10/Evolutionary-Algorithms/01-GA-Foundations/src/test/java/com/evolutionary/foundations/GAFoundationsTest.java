package com.evolutionary.foundations;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GAFoundationsTest {

    @Test
    @DisplayName("GA finds the optimal solution for a simple all-ones target")
    void findsAllOnes() {
        // Fitness = number of 1-bits (OneMax problem)
        GeneticAlgorithm ga = new GeneticAlgorithm(50, 10, 0.8, 0.05, 3, 1L);
        Individual best = ga.evolve(ind -> {
            int count = 0;
            for (boolean g : ind.genes()) if (g) count++;
            return count;
        }, 200);
        assertEquals(10.0, best.fitness(), "Should find the all-ones optimum");
    }

    @Test
    @DisplayName("GA improves fitness over generations (longer run finds better solution)")
    void improvesFitness() {
        // More generations → higher fitness for a 20-bit OneMax
        GeneticAlgorithm gaShort = new GeneticAlgorithm(50, 20, 0.8, 0.02, 3, 7L);
        GeneticAlgorithm gaLong  = new GeneticAlgorithm(50, 20, 0.8, 0.02, 3, 7L);

        Individual shortRun = gaShort.evolve(ind -> {
            int c = 0; for (boolean g : ind.genes()) if (g) c++; return c;
        }, 5);
        Individual longRun  = gaLong.evolve(ind -> {
            int c = 0; for (boolean g : ind.genes()) if (g) c++; return c;
        }, 500);
        assertTrue(longRun.fitness() >= shortRun.fitness());
    }

    @Test
    @DisplayName("Individual toString has correct length")
    void individualToString() {
        Individual ind = Individual.random(16, new java.util.Random(0));
        assertEquals(16, ind.toString().length());
        assertTrue(ind.toString().matches("[01]+"));
    }

    @Test
    @DisplayName("GA solves the feature-selection fitness function from Main")
    void featureSelection() {
        GeneticAlgorithm ga = new GeneticAlgorithm(100, 20, 0.8, 0.02, 5, 42L);
        Individual best = ga.evolve(ind -> {
            double score = 0;
            for (int i = 0; i < ind.length(); i++)
                if (ind.genes()[i]) score += Main.FEATURE_VALUE[i];
            return score;
        }, 200);

        // Optimal is to include only positive-value features
        double optimal = 0;
        for (double v : Main.FEATURE_VALUE) if (v > 0) optimal += v;
        assertTrue(best.fitness() >= 0.90 * optimal,
            "GA should find at least 90% of optimal fitness");
    }

    @Test
    @DisplayName("Elitism: best individual is always preserved")
    void elitismPreservesBest() {
        // Verify fitness is non-decreasing by running multiple seeds
        for (long seed = 0; seed < 5; seed++) {
            final long s = seed;
            GeneticAlgorithm ga = new GeneticAlgorithm(30, 10, 0.8, 0.05, 3, s);
            // The best fitness must never decrease across generations (elitism)
            // We verify indirectly: running longer never gives a worse result
            Individual short_ = new GeneticAlgorithm(30, 10, 0.8, 0.05, 3, s)
                .evolve(ind -> { int c = 0; for (boolean g : ind.genes()) if (g) c++; return c; }, 10);
            Individual long_ = ga
                .evolve(ind -> { int c = 0; for (boolean g : ind.genes()) if (g) c++; return c; }, 100);
            assertTrue(long_.fitness() >= short_.fitness(),
                "Longer run should not be worse (seed=" + seed + ")");
        }
    }
}

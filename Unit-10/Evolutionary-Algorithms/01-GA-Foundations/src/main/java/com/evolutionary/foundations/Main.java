package com.evolutionary.foundations;

/**
 * Demonstrates the GA on a feature-selection problem.
 *
 * A machine learning pipeline has 20 candidate features. Each feature either
 * helps the model or introduces noise. The GA evolves a binary mask
 * (1=include, 0=exclude) to maximise a simulated validation score.
 *
 * The fitness function rewards including "good" features (even indices)
 * and penalises "noisy" features (odd indices), modelling a scenario
 * where we don't know in advance which features are useful.
 */
public class Main {

    // Simulated feature quality: positive = helpful, negative = noisy
    static final double[] FEATURE_VALUE = {
         0.8, -0.3,  0.6, -0.1,  0.9, -0.4,  0.7, -0.2,
         0.5, -0.5,  0.4, -0.6,  1.0, -0.1,  0.3, -0.3,
         0.6, -0.2,  0.8, -0.4
    };

    public static void main(String[] args) {
        System.out.println("=== Genetic Algorithm Foundations — Feature Selection ===\n");
        System.out.println("Task: find the best subset of 20 candidate features.");
        System.out.println("Even-indexed features are helpful; odd-indexed are noisy.\n");

        GeneticAlgorithm ga = new GeneticAlgorithm(
            /*populationSize*/   100,
            /*chromosomeLength*/ 20,
            /*crossoverRate*/    0.80,
            /*mutationRate*/     0.02,
            /*tournamentSize*/   5,
            /*seed*/             42L
        );

        // Fitness = sum of values for selected features
        Individual best = ga.evolve(ind -> {
            double score = 0;
            for (int i = 0; i < ind.length(); i++) if (ind.genes()[i]) score += FEATURE_VALUE[i];
            return score;
        }, 200);

        System.out.println("Best individual found: " + best);
        System.out.printf("Fitness: %.4f%n%n", best.fitness());

        System.out.println("Selected features:");
        for (int i = 0; i < best.length(); i++) {
            if (best.genes()[i]) {
                System.out.printf("  Feature %2d  value=%.1f  %s%n",
                    i, FEATURE_VALUE[i], FEATURE_VALUE[i] > 0 ? "✓ helpful" : "✗ noisy");
            }
        }

        // Optimal: select all features with positive value
        double optimal = 0;
        for (double v : FEATURE_VALUE) if (v > 0) optimal += v;
        System.out.printf("%nOptimal fitness (all good features only): %.4f%n", optimal);
        System.out.printf("GA achieved: %.1f%% of optimal%n", best.fitness() / optimal * 100);
    }
}

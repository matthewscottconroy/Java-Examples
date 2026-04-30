package com.evolutionary.continuous;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.ToDoubleFunction;

/**
 * Genetic algorithm over real-valued (floating-point) chromosomes.
 *
 * <p>Operators differ from the binary GA:
 * <ul>
 *   <li><b>Arithmetic crossover</b> — child gene = α·p1 + (1−α)·p2 for a random α ∈ [0,1].</li>
 *   <li><b>Gaussian mutation</b> — add noise sampled from N(0, σ), clamped to bounds.</li>
 * </ul>
 *
 * @param dimensions number of real-valued parameters
 */
public class ContinuousGA {

    public record Individual(double[] genes, double fitness) {
        Individual withFitness(double f) { return new Individual(genes.clone(), f); }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < genes.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(String.format("%.4f", genes[i]));
            }
            return sb.append("]").toString();
        }

        @Override public boolean equals(Object o) {
            return o instanceof Individual ind && Arrays.equals(genes, ind.genes);
        }
        @Override public int hashCode() { return Arrays.hashCode(genes); }
    }

    private final int      populationSize;
    private final int      dimensions;
    private final double[] lowerBound;
    private final double[] upperBound;
    private final double   mutationRate;
    private final double   mutationSigma;   // std dev for Gaussian mutation
    private final int      tournamentSize;
    private final Random   rng;

    public ContinuousGA(int populationSize, int dimensions,
                         double[] lowerBound, double[] upperBound,
                         double mutationRate, double mutationSigma,
                         int tournamentSize, long seed) {
        this.populationSize = populationSize;
        this.dimensions     = dimensions;
        this.lowerBound     = lowerBound;
        this.upperBound     = upperBound;
        this.mutationRate   = mutationRate;
        this.mutationSigma  = mutationSigma;
        this.tournamentSize = tournamentSize;
        this.rng            = new Random(seed);
    }

    public Individual evolve(ToDoubleFunction<double[]> objectiveFn, int generations) {
        List<Individual> pop = initialPopulation();
        evaluate(pop, objectiveFn);

        for (int gen = 0; gen < generations; gen++) {
            List<Individual> next = new ArrayList<>(populationSize);
            next.add(best(pop));  // elitism

            while (next.size() < populationSize) {
                Individual p1 = tournament(pop), p2 = tournament(pop);
                Individual child = arithmeticCrossover(p1, p2);
                child = gaussianMutate(child);
                next.add(child);
            }
            pop = next;
            evaluate(pop, objectiveFn);
        }
        return best(pop);
    }

    private List<Individual> initialPopulation() {
        List<Individual> pop = new ArrayList<>(populationSize);
        for (int i = 0; i < populationSize; i++) {
            double[] genes = new double[dimensions];
            for (int d = 0; d < dimensions; d++)
                genes[d] = lowerBound[d] + rng.nextDouble() * (upperBound[d] - lowerBound[d]);
            pop.add(new Individual(genes, 0.0));
        }
        return pop;
    }

    private void evaluate(List<Individual> pop, ToDoubleFunction<double[]> fn) {
        pop.replaceAll(ind -> ind.withFitness(fn.applyAsDouble(ind.genes())));
    }

    private Individual best(List<Individual> pop) {
        return pop.stream().max(Comparator.comparingDouble(Individual::fitness)).orElseThrow();
    }

    private Individual tournament(List<Individual> pop) {
        Individual best = null;
        for (int i = 0; i < tournamentSize; i++) {
            Individual c = pop.get(rng.nextInt(pop.size()));
            if (best == null || c.fitness() > best.fitness()) best = c;
        }
        return best;
    }

    private Individual arithmeticCrossover(Individual p1, Individual p2) {
        double[] child = new double[dimensions];
        for (int d = 0; d < dimensions; d++) {
            double alpha = rng.nextDouble();
            child[d] = alpha * p1.genes()[d] + (1 - alpha) * p2.genes()[d];
        }
        return new Individual(child, 0.0);
    }

    private Individual gaussianMutate(Individual ind) {
        double[] genes = ind.genes().clone();
        boolean mutated = false;
        for (int d = 0; d < dimensions; d++) {
            if (rng.nextDouble() < mutationRate) {
                genes[d] += rng.nextGaussian() * mutationSigma;
                genes[d] = Math.max(lowerBound[d], Math.min(upperBound[d], genes[d]));
                mutated = true;
            }
        }
        return mutated ? new Individual(genes, 0.0) : ind;
    }
}

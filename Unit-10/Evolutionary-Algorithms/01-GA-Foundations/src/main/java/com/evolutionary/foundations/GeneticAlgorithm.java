package com.evolutionary.foundations;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.ToDoubleFunction;

/**
 * A configurable binary genetic algorithm.
 *
 * <p>The standard loop:
 * <ol>
 *   <li>Evaluate fitness for every individual in the population.</li>
 *   <li>Select parents by tournament selection.</li>
 *   <li>Produce offspring via single-point crossover.</li>
 *   <li>Apply bit-flip mutation.</li>
 *   <li>Carry the best individual into the next generation (elitism).</li>
 *   <li>Repeat until the termination condition is met.</li>
 * </ol>
 */
public class GeneticAlgorithm {

    private final int    populationSize;
    private final int    chromosomeLength;
    private final double crossoverRate;
    private final double mutationRate;
    private final int    tournamentSize;
    private final Random rng;

    public GeneticAlgorithm(int populationSize, int chromosomeLength,
                             double crossoverRate, double mutationRate,
                             int tournamentSize, long seed) {
        this.populationSize   = populationSize;
        this.chromosomeLength = chromosomeLength;
        this.crossoverRate    = crossoverRate;
        this.mutationRate     = mutationRate;
        this.tournamentSize   = tournamentSize;
        this.rng              = new Random(seed);
    }

    /**
     * Runs the GA for a fixed number of generations.
     *
     * @param fitnessFunction maps an individual to its fitness (higher = better)
     * @param generations     number of generations to evolve
     * @return the best individual found
     */
    public Individual evolve(ToDoubleFunction<Individual> fitnessFunction, int generations) {
        List<Individual> population = initialPopulation();
        evaluate(population, fitnessFunction);

        for (int gen = 0; gen < generations; gen++) {
            List<Individual> nextGen = new ArrayList<>(populationSize);

            // Elitism: keep the best individual
            Individual elite = best(population);
            nextGen.add(elite);

            while (nextGen.size() < populationSize) {
                Individual parent1 = tournamentSelect(population);
                Individual parent2 = tournamentSelect(population);

                Individual child1, child2;
                if (rng.nextDouble() < crossoverRate) {
                    child1 = crossover(parent1, parent2);
                    child2 = crossover(parent2, parent1);
                } else {
                    child1 = parent1; child2 = parent2;
                }

                child1 = mutate(child1);
                child2 = mutate(child2);
                nextGen.add(child1);
                if (nextGen.size() < populationSize) nextGen.add(child2);
            }

            population = nextGen;
            evaluate(population, fitnessFunction);
        }
        return best(population);
    }

    private List<Individual> initialPopulation() {
        List<Individual> pop = new ArrayList<>(populationSize);
        for (int i = 0; i < populationSize; i++) pop.add(Individual.random(chromosomeLength, rng));
        return pop;
    }

    private void evaluate(List<Individual> pop, ToDoubleFunction<Individual> fn) {
        pop.replaceAll(ind -> ind.withFitness(fn.applyAsDouble(ind)));
    }

    private Individual best(List<Individual> pop) {
        return pop.stream().max(Comparator.comparingDouble(Individual::fitness)).orElseThrow();
    }

    private Individual tournamentSelect(List<Individual> pop) {
        Individual best = null;
        for (int i = 0; i < tournamentSize; i++) {
            Individual candidate = pop.get(rng.nextInt(pop.size()));
            if (best == null || candidate.fitness() > best.fitness()) best = candidate;
        }
        return best;
    }

    /** Single-point crossover: genes before the cut point come from parent1, rest from parent2. */
    private Individual crossover(Individual p1, Individual p2) {
        int cut = rng.nextInt(chromosomeLength);
        boolean[] child = new boolean[chromosomeLength];
        for (int i = 0; i < chromosomeLength; i++)
            child[i] = i < cut ? p1.genes()[i] : p2.genes()[i];
        return new Individual(child, 0.0);
    }

    /** Bit-flip mutation: each gene flips with probability mutationRate. */
    private Individual mutate(Individual ind) {
        boolean[] genes = ind.genes().clone();
        boolean mutated = false;
        for (int i = 0; i < genes.length; i++) {
            if (rng.nextDouble() < mutationRate) { genes[i] = !genes[i]; mutated = true; }
        }
        return mutated ? new Individual(genes, 0.0) : ind;
    }
}

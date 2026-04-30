package com.evolutionary.multiobjective;

import java.util.*;
import java.util.function.Function;

/**
 * NSGA-II: Non-dominated Sorting Genetic Algorithm II.
 *
 * <p>Produces a Pareto front of trade-off solutions rather than a single
 * best answer. The key mechanisms are:
 * <ul>
 *   <li><b>Non-dominated sorting</b> — assigns each solution a rank (front number).
 *       Rank 0 = Pareto-optimal front; rank 1 = optimal ignoring rank-0; etc.</li>
 *   <li><b>Crowding distance</b> — measures how isolated a solution is within its
 *       front. Larger distance = more diverse = preferred when ranks tie.</li>
 *   <li><b>Tournament selection</b> — prefers lower rank; breaks ties by higher
 *       crowding distance.</li>
 * </ul>
 */
public class NSGA2 {

    public record Config(
        int populationSize,
        int generations,
        double crossoverRate,
        double mutationRate,
        double mutationSigma,
        double[] lowerBound,
        double[] upperBound,
        long seed
    ) {}

    private final Config config;
    private final Function<double[], double[]> objectiveFunction;
    private final Random rng;

    public NSGA2(Config config, Function<double[], double[]> objectiveFunction) {
        this.config = config;
        this.objectiveFunction = objectiveFunction;
        this.rng = new Random(config.seed());
    }

    public List<Solution> run() {
        List<Solution> population = initialise();
        evaluate(population);

        for (int gen = 0; gen < config.generations(); gen++) {
            List<Solution> offspring = createOffspring(population);
            evaluate(offspring);

            List<Solution> combined = new ArrayList<>(population);
            combined.addAll(offspring);

            List<List<Solution>> fronts = nonDominatedSort(combined);
            assignCrowdingDistances(fronts);

            population = selectNextGeneration(fronts, config.populationSize());
        }

        List<List<Solution>> finalFronts = nonDominatedSort(population);
        assignCrowdingDistances(finalFronts);
        return finalFronts.get(0);
    }

    private List<Solution> initialise() {
        List<Solution> pop = new ArrayList<>(config.populationSize());
        for (int i = 0; i < config.populationSize(); i++) {
            pop.add(Solution.random(config.lowerBound().length,
                                    config.lowerBound(), config.upperBound(), rng));
        }
        return pop;
    }

    private void evaluate(List<Solution> population) {
        for (int i = 0; i < population.size(); i++) {
            Solution s = population.get(i);
            double[] obj = objectiveFunction.apply(s.genes());
            population.set(i, s.withObjectives(obj));
        }
    }

    // -----------------------------------------------------------------
    // Non-dominated sorting
    // -----------------------------------------------------------------

    List<List<Solution>> nonDominatedSort(List<Solution> population) {
        int n = population.size();
        int[] dominationCount = new int[n];
        List<List<Integer>> dominated = new ArrayList<>();
        for (int i = 0; i < n; i++) dominated.add(new ArrayList<>());

        List<Integer> currentFront = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) continue;
                if (population.get(i).dominates(population.get(j))) {
                    dominated.get(i).add(j);
                } else if (population.get(j).dominates(population.get(i))) {
                    dominationCount[i]++;
                }
            }
            if (dominationCount[i] == 0) currentFront.add(i);
        }

        List<List<Solution>> fronts = new ArrayList<>();
        int rank = 0;

        while (!currentFront.isEmpty()) {
            List<Solution> frontSolutions = new ArrayList<>();
            List<Integer> nextFront = new ArrayList<>();

            for (int i : currentFront) {
                Solution s = population.get(i).withRank(rank);
                population.set(i, s);
                frontSolutions.add(s);

                for (int j : dominated.get(i)) {
                    dominationCount[j]--;
                    if (dominationCount[j] == 0) nextFront.add(j);
                }
            }

            fronts.add(frontSolutions);
            currentFront = nextFront;
            rank++;
        }

        return fronts;
    }

    // -----------------------------------------------------------------
    // Crowding distance
    // -----------------------------------------------------------------

    void assignCrowdingDistances(List<List<Solution>> fronts) {
        for (List<Solution> front : fronts) {
            int n = front.size();
            if (n == 0) continue;
            double[] distance = new double[n];

            int numObj = front.get(0).numObjectives();
            for (int m = 0; m < numObj; m++) {
                final int obj = m;
                front.sort(Comparator.comparingDouble(s -> s.objectives()[obj]));

                distance[0] = Double.MAX_VALUE;
                distance[n - 1] = Double.MAX_VALUE;

                double minObj = front.get(0).objectives()[obj];
                double maxObj = front.get(n - 1).objectives()[obj];
                double range = maxObj - minObj;
                if (range == 0) continue;

                for (int i = 1; i < n - 1; i++) {
                    distance[i] += (front.get(i + 1).objectives()[obj]
                                  - front.get(i - 1).objectives()[obj]) / range;
                }
            }

            for (int i = 0; i < n; i++) {
                Solution old = front.get(i);
                front.set(i, old.withCrowdingDistance(distance[i]));
            }
        }
    }

    // -----------------------------------------------------------------
    // Selection and offspring creation
    // -----------------------------------------------------------------

    private Solution tournamentSelect(List<Solution> population) {
        Solution a = population.get(rng.nextInt(population.size()));
        Solution b = population.get(rng.nextInt(population.size()));
        return crowdedComparison(a, b) <= 0 ? a : b;
    }

    /** Lower rank wins; equal rank → higher crowding distance wins. */
    private int crowdedComparison(Solution a, Solution b) {
        if (a.rank() != b.rank()) return Integer.compare(a.rank(), b.rank());
        return Double.compare(b.crowdingDistance(), a.crowdingDistance());
    }

    private List<Solution> createOffspring(List<Solution> population) {
        List<Solution> offspring = new ArrayList<>(config.populationSize());
        while (offspring.size() < config.populationSize()) {
            Solution p1 = tournamentSelect(population);
            Solution p2 = tournamentSelect(population);
            Solution child = rng.nextDouble() < config.crossoverRate()
                ? arithmeticCrossover(p1, p2)
                : p1;
            offspring.add(mutate(child));
        }
        return offspring;
    }

    private Solution arithmeticCrossover(Solution p1, Solution p2) {
        double alpha = rng.nextDouble();
        double[] genes = new double[p1.dimensions()];
        for (int d = 0; d < genes.length; d++) {
            genes[d] = alpha * p1.genes()[d] + (1 - alpha) * p2.genes()[d];
        }
        return new Solution(genes, new double[0], 0, 0.0);
    }

    private Solution mutate(Solution s) {
        double[] genes = s.genes().clone();
        for (int d = 0; d < genes.length; d++) {
            if (rng.nextDouble() < config.mutationRate()) {
                genes[d] += rng.nextGaussian() * config.mutationSigma();
                genes[d] = Math.max(config.lowerBound()[d],
                            Math.min(config.upperBound()[d], genes[d]));
            }
        }
        return new Solution(genes, new double[0], 0, 0.0);
    }

    private List<Solution> selectNextGeneration(List<List<Solution>> fronts, int targetSize) {
        List<Solution> next = new ArrayList<>();
        for (List<Solution> front : fronts) {
            if (next.size() + front.size() <= targetSize) {
                next.addAll(front);
            } else {
                List<Solution> sorted = new ArrayList<>(front);
                sorted.sort((a, b) -> Double.compare(b.crowdingDistance(), a.crowdingDistance()));
                next.addAll(sorted.subList(0, targetSize - next.size()));
                break;
            }
        }
        return next;
    }
}

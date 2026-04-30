package com.evolutionary.multiobjective;

import java.util.Arrays;
import java.util.Random;

/**
 * A candidate solution in a multi-objective optimisation problem.
 *
 * <p>Each solution has a real-valued chromosome (the decision variables),
 * an array of objective values (costs to minimise), a Pareto rank, and
 * a crowding distance used by NSGA-II for selection.
 */
public record Solution(double[] genes, double[] objectives, int rank, double crowdingDistance) {

    public int dimensions() { return genes.length; }
    public int numObjectives() { return objectives.length; }

    public Solution withRank(int rank) {
        return new Solution(genes, objectives, rank, crowdingDistance);
    }

    public Solution withCrowdingDistance(double d) {
        return new Solution(genes, objectives, rank, d);
    }

    public Solution withObjectives(double[] objectives) {
        return new Solution(genes, objectives, rank, crowdingDistance);
    }

    /**
     * Returns true if this solution dominates other — it is no worse on all
     * objectives and strictly better on at least one.
     */
    public boolean dominates(Solution other) {
        boolean strictlyBetterOnOne = false;
        for (int i = 0; i < objectives.length; i++) {
            if (objectives[i] > other.objectives[i]) return false;
            if (objectives[i] < other.objectives[i]) strictlyBetterOnOne = true;
        }
        return strictlyBetterOnOne;
    }

    public static Solution random(int dimensions, double[] lower, double[] upper, Random rng) {
        double[] genes = new double[dimensions];
        for (int d = 0; d < dimensions; d++) {
            genes[d] = lower[d] + rng.nextDouble() * (upper[d] - lower[d]);
        }
        return new Solution(genes, new double[0], 0, 0.0);
    }

    @Override
    public String toString() {
        return "genes=" + Arrays.toString(Arrays.stream(genes)
                .mapToObj(v -> String.format("%.3f", v)).toArray())
             + " obj=" + Arrays.toString(Arrays.stream(objectives)
                .mapToObj(v -> String.format("%.4f", v)).toArray())
             + " rank=" + rank;
    }
}

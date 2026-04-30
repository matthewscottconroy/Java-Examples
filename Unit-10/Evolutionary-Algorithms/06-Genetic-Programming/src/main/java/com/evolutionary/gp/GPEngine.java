package com.evolutionary.gp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Genetic Programming engine for symbolic regression.
 *
 * <p>Evolves expression trees to fit a dataset of (x, y) pairs.
 * Fitness is negative mean squared error — higher is better (less error).
 *
 * <p>GP operators:
 * <ul>
 *   <li><b>Subtree crossover</b> — swap a random subtree between two parents.</li>
 *   <li><b>Point mutation</b> — replace a random node with a new random subtree.</li>
 * </ul>
 */
public class GPEngine {

    private final int    populationSize;
    private final int    maxDepth;
    private final double crossoverRate;
    private final double mutationRate;
    private final int    tournamentSize;
    private final Random rng;

    public GPEngine(int populationSize, int maxDepth, double crossoverRate,
                     double mutationRate, int tournamentSize, long seed) {
        this.populationSize = populationSize;
        this.maxDepth       = maxDepth;
        this.crossoverRate  = crossoverRate;
        this.mutationRate   = mutationRate;
        this.tournamentSize = tournamentSize;
        this.rng            = new Random(seed);
    }

    public record DataPoint(double x, double y) {}

    public ExprTree evolve(List<DataPoint> data, int generations) {
        List<ExprTree> population = new ArrayList<>(populationSize);
        for (int i = 0; i < populationSize; i++)
            population.add(ExprTree.random(maxDepth, rng));

        ExprTree best = population.get(0);
        double bestFit = fitness(best, data);

        for (int gen = 0; gen < generations; gen++) {
            List<ExprTree> next = new ArrayList<>(populationSize);
            next.add(best);  // elitism

            while (next.size() < populationSize) {
                ExprTree p1 = tournament(population, data);
                ExprTree child;
                if (rng.nextDouble() < crossoverRate) {
                    ExprTree p2 = tournament(population, data);
                    child = subtreeCrossover(p1, p2);
                } else {
                    child = p1;
                }
                if (rng.nextDouble() < mutationRate) child = pointMutate(child);
                // Depth limit: if too deep, replace with a random simpler tree
                if (child.depth() > maxDepth) child = ExprTree.random(maxDepth / 2, rng);
                next.add(child);
            }
            population = next;

            for (ExprTree t : population) {
                double f = fitness(t, data);
                if (f > bestFit) { bestFit = f; best = t; }
            }
        }
        return best;
    }

    double fitness(ExprTree tree, List<DataPoint> data) {
        double mse = 0;
        for (DataPoint p : data) {
            double pred = tree.eval(p.x());
            if (Double.isNaN(pred) || Double.isInfinite(pred)) return -1e9;
            double err = pred - p.y();
            mse += err * err;
        }
        return -(mse / data.size());  // negate: higher = better
    }

    private ExprTree tournament(List<ExprTree> pop, List<DataPoint> data) {
        ExprTree best = null; double bestF = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < tournamentSize; i++) {
            ExprTree c = pop.get(rng.nextInt(pop.size()));
            double f = fitness(c, data);
            if (f > bestF) { bestF = f; best = c; }
        }
        return best;
    }

    /** Swap a random subtree from p2 into a random position in p1. */
    private ExprTree subtreeCrossover(ExprTree p1, ExprTree p2) {
        ExprTree donor = randomSubtree(p2);
        return replaceRandomSubtree(p1, donor);
    }

    private ExprTree pointMutate(ExprTree tree) {
        return replaceRandomSubtree(tree, ExprTree.random(maxDepth / 2, rng));
    }

    private ExprTree randomSubtree(ExprTree tree) {
        List<ExprTree> nodes = new ArrayList<>();
        collectNodes(tree, nodes);
        return nodes.get(rng.nextInt(nodes.size()));
    }

    private void collectNodes(ExprTree tree, List<ExprTree> out) {
        out.add(tree);
        if (tree instanceof ExprTree.BinOp b) {
            collectNodes(b.left(), out);
            collectNodes(b.right(), out);
        }
    }

    private ExprTree replaceRandomSubtree(ExprTree tree, ExprTree replacement) {
        if (!(tree instanceof ExprTree.BinOp b)) return replacement;
        if (rng.nextBoolean()) {
            return new ExprTree.BinOp(b.op(), replaceRandomSubtree(b.left(), replacement), b.right());
        } else {
            return new ExprTree.BinOp(b.op(), b.left(), replaceRandomSubtree(b.right(), replacement));
        }
    }
}

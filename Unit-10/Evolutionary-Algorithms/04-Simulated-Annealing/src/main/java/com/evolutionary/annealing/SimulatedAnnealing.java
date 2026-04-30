package com.evolutionary.annealing;

import java.util.Random;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.UnaryOperator;

/**
 * Simulated Annealing (SA) — a single-solution metaheuristic inspired by
 * the annealing process in metallurgy.
 *
 * <p>SA escapes local optima by occasionally accepting worse solutions with
 * a probability that decreases as "temperature" falls. At high temperature
 * the search is nearly random (exploration); at low temperature it behaves
 * like hill climbing (exploitation).
 *
 * <p>Acceptance probability for a worse move:
 * <pre>
 *   P(accept) = exp(-ΔE / T)
 * </pre>
 * where ΔE = cost(new) - cost(current) > 0 and T is the current temperature.
 *
 * @param <S> solution type
 */
public class SimulatedAnnealing<S> {

    public record Config(double initialTemp, double finalTemp,
                          double coolingRate, int iterationsPerTemp) {}

    public record Result<S>(S solution, double cost, int iterations) {}

    private final Config              config;
    private final ToDoubleFunction<S> costFunction;   // lower = better
    private final UnaryOperator<S>    neighbour;      // generates a nearby solution
    private final Random              rng;

    public SimulatedAnnealing(Config config,
                               ToDoubleFunction<S> costFunction,
                               UnaryOperator<S>    neighbour,
                               long seed) {
        this.config       = config;
        this.costFunction = costFunction;
        this.neighbour    = neighbour;
        this.rng          = new Random(seed);
    }

    public Result<S> solve(S initialSolution) {
        S current = initialSolution;
        double currentCost = costFunction.applyAsDouble(current);

        S best = current;
        double bestCost = currentCost;

        double temp = config.initialTemp();
        int totalIterations = 0;

        while (temp > config.finalTemp()) {
            for (int i = 0; i < config.iterationsPerTemp(); i++) {
                S candidate = neighbour.apply(current);
                double candidateCost = costFunction.applyAsDouble(candidate);
                double delta = candidateCost - currentCost;

                if (delta < 0 || rng.nextDouble() < Math.exp(-delta / temp)) {
                    current = candidate;
                    currentCost = candidateCost;
                    if (currentCost < bestCost) { best = current; bestCost = currentCost; }
                }
                totalIterations++;
            }
            temp *= config.coolingRate();
        }
        return new Result<>(best, bestCost, totalIterations);
    }
}

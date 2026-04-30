package com.evolutionary.multiobjective;

import java.util.Comparator;
import java.util.List;

/**
 * Cloud service deployment planner.
 *
 * <p>A platform team must deploy N microservices across cloud regions.
 * Each deployment configuration encodes, for every service, a "quality level"
 * from 0 (cheapest/lowest reliability) to 1 (most expensive/highest reliability).
 *
 * <p>Two objectives to minimise simultaneously:
 * <ul>
 *   <li><b>Cost</b>       — Σ cost_model(quality[i]) — higher quality costs more</li>
 *   <li><b>Unreliability</b> — 1 − Π reliability(quality[i]) — probability that at
 *                              least one service fails</li>
 * </ul>
 *
 * <p>These objectives are antagonistic: spending more money buys reliability,
 * but a budget-conscious team must trade off between the two. NSGA-II produces
 * the Pareto front of all trade-off deployments — teams can pick their preferred
 * cost/reliability operating point.
 */
public class Main {

    static final int NUM_SERVICES = 6;

    static double cost(double[] genes) {
        double total = 0;
        for (double q : genes) total += 10 * q * q + q;  // convex cost curve
        return total;
    }

    static double unreliability(double[] genes) {
        double systemReliability = 1.0;
        for (double q : genes) {
            double reliability = 0.5 + 0.5 * q;  // scales from 50% to 100%
            systemReliability *= reliability;
        }
        return 1.0 - systemReliability;
    }

    public static void main(String[] args) {
        double[] lower = new double[NUM_SERVICES];
        double[] upper = new double[NUM_SERVICES];
        for (int i = 0; i < NUM_SERVICES; i++) { lower[i] = 0.0; upper[i] = 1.0; }

        NSGA2.Config config = new NSGA2.Config(
            100,           // populationSize
            200,           // generations
            0.9,           // crossoverRate
            0.1,           // mutationRate
            0.05,          // mutationSigma
            lower, upper,
            42L
        );

        NSGA2 nsga2 = new NSGA2(config, genes -> new double[]{ cost(genes), unreliability(genes) });
        List<Solution> paretoFront = nsga2.run();

        paretoFront.sort(Comparator.comparingDouble(s -> s.objectives()[0]));

        System.out.println("=== NSGA-II — Cloud Deployment Planner ===\n");
        System.out.printf("%-6s  %-12s  %-14s%n", "Front#", "Cost ($)", "Unreliability");
        System.out.println("-".repeat(36));
        for (int i = 0; i < paretoFront.size(); i++) {
            double[] obj = paretoFront.get(i).objectives();
            System.out.printf("%-6d  %-12.2f  %-14.4f%n", i + 1, obj[0], obj[1]);
        }

        System.out.printf("%nPareto front size: %d solutions%n", paretoFront.size());

        Solution cheapest = paretoFront.stream()
            .min(Comparator.comparingDouble(s -> s.objectives()[0])).orElseThrow();
        Solution mostReliable = paretoFront.stream()
            .min(Comparator.comparingDouble(s -> s.objectives()[1])).orElseThrow();

        System.out.printf("%nCheapest:       cost=%.2f  unreliability=%.4f%n",
            cheapest.objectives()[0], cheapest.objectives()[1]);
        System.out.printf("Most reliable:  cost=%.2f  unreliability=%.4f%n",
            mostReliable.objectives()[0], mostReliable.objectives()[1]);
    }
}

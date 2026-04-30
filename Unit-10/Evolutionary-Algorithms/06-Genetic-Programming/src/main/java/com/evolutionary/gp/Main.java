package com.evolutionary.gp;

import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates Genetic Programming on a symbolic regression problem.
 *
 * A pricing team observes sales data: for a given discount percentage x,
 * the revenue change y follows some unknown formula. GP discovers a formula
 * that fits the data by evolving expression trees.
 *
 * Hidden formula: y = x² - 2x + 1  (quadratic, i.e. (x-1)²)
 */
public class Main {

    static List<GPEngine.DataPoint> generateData() {
        List<GPEngine.DataPoint> data = new ArrayList<>();
        for (double x = -3; x <= 3; x += 0.5) {
            double y = x * x - 2 * x + 1;  // (x-1)²
            data.add(new GPEngine.DataPoint(x, y));
        }
        return data;
    }

    public static void main(String[] args) {
        System.out.println("=== Genetic Programming — Formula Discoverer ===\n");
        System.out.println("Target formula (hidden): y = x² - 2x + 1");
        System.out.println("GP must discover this from data points.\n");

        List<GPEngine.DataPoint> data = generateData();
        System.out.println("Training data (x → y):");
        data.forEach(p -> System.out.printf("  x=%5.1f  y=%6.2f%n", p.x(), p.y()));

        GPEngine gp = new GPEngine(
            /*populationSize*/ 200,
            /*maxDepth*/       5,
            /*crossoverRate*/  0.85,
            /*mutationRate*/   0.10,
            /*tournamentSize*/ 7,
            /*seed*/           42L
        );

        ExprTree best = gp.evolve(data, 100);

        double mse = -gp.fitness(best, data);
        System.out.println("\nBest formula found: " + best.toFormula());
        System.out.printf("Mean Squared Error:  %.4f%n", mse);
        System.out.printf("Tree depth:          %d%n%n", best.depth());

        System.out.println("Predictions vs actuals:");
        System.out.printf("  %-8s  %-10s  %-10s  %s%n", "x", "actual", "predicted", "error");
        for (GPEngine.DataPoint p : data) {
            double pred = best.eval(p.x());
            System.out.printf("  %-8.1f  %-10.3f  %-10.3f  %.3f%n",
                p.x(), p.y(), pred, Math.abs(pred - p.y()));
        }
    }
}

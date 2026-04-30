package com.evolutionary.continuous;

/**
 * Demonstrates real-valued GA on a hyperparameter tuning problem.
 *
 * An ML model's validation accuracy depends on:
 *   - learning rate (0.0001 – 0.1)
 *   - dropout rate  (0.0 – 0.5)
 *   - L2 regularisation weight (0.00001 – 0.01)
 *
 * The "true" optimum is pre-defined; the GA should find it without knowing its location.
 * A response-surface model simulates how accuracy varies with the hyperparameters.
 */
public class Main {

    // Simulated optimal hyperparameters
    static final double OPT_LR      = 0.003;
    static final double OPT_DROPOUT = 0.2;
    static final double OPT_L2      = 0.0001;

    public static void main(String[] args) {
        System.out.println("=== Continuous GA — Hyperparameter Tuner ===\n");
        System.out.println("Tuning: learning_rate, dropout, l2_weight");
        System.out.printf("Known optimum: lr=%.4f  dropout=%.2f  l2=%.5f%n%n",
            OPT_LR, OPT_DROPOUT, OPT_L2);

        // 3D real-valued search space
        double[] lo = {0.0001, 0.0, 0.00001};
        double[] hi = {0.1,    0.5, 0.01};

        ContinuousGA ga = new ContinuousGA(
            /*populationSize*/ 80,
            /*dimensions*/     3,
            /*lowerBound*/     lo,
            /*upperBound*/     hi,
            /*mutationRate*/   0.3,
            /*mutationSigma*/  0.01,
            /*tournamentSize*/ 5,
            /*seed*/           99L
        );

        ContinuousGA.Individual best = ga.evolve(Main::simulatedAccuracy, 300);

        System.out.println("Best hyperparameters found:");
        double[] g = best.genes();
        System.out.printf("  learning_rate: %.5f  (optimum %.4f)%n", g[0], OPT_LR);
        System.out.printf("  dropout:       %.5f  (optimum %.2f)%n",  g[1], OPT_DROPOUT);
        System.out.printf("  l2_weight:     %.6f (optimum %.5f)%n",  g[2], OPT_L2);
        System.out.printf("  Simulated accuracy: %.4f%n", best.fitness());
        System.out.printf("  Peak accuracy:      %.4f%n", simulatedAccuracy(new double[]{OPT_LR, OPT_DROPOUT, OPT_L2}));

        // Show the Rastrigin function (classic EA benchmark)
        System.out.println("\n--- Benchmark: Rastrigin Function (minimum at origin) ---");
        double[] rLo = {-5.12, -5.12, -5.12, -5.12, -5.12};
        double[] rHi = { 5.12,  5.12,  5.12,  5.12,  5.12};
        ContinuousGA rastriginGA = new ContinuousGA(100, 5, rLo, rHi, 0.4, 0.5, 5, 7L);
        ContinuousGA.Individual rastriginBest = rastriginGA.evolve(Main::negRastrigin, 500);
        System.out.printf("  Best x: %s%n", rastriginBest);
        System.out.printf("  Rastrigin value: %.4f (optimum = 0)%n", -rastriginBest.fitness());
    }

    /**
     * Simulates validation accuracy as a function of hyperparameters.
     * Peaks near (OPT_LR, OPT_DROPOUT, OPT_L2); falls off in all directions.
     */
    static double simulatedAccuracy(double[] genes) {
        double lr = genes[0], drop = genes[1], l2 = genes[2];
        // Gaussian bump centred at optimum, scaled to [0, 1]
        double dLr   = (lr   - OPT_LR)      / 0.01;
        double dDrop = (drop - OPT_DROPOUT) / 0.05;
        double dL2   = (l2   - OPT_L2)      / 0.0005;
        return 0.95 * Math.exp(-(dLr*dLr + dDrop*dDrop + dL2*dL2) / 2.0);
    }

    /** Negative Rastrigin: maximise this to minimise Rastrigin. */
    static double negRastrigin(double[] x) {
        int n = x.length;
        double sum = 10.0 * n;
        for (double xi : x) sum += xi * xi - 10.0 * Math.cos(2 * Math.PI * xi);
        return -sum;  // negate because GA maximises
    }
}

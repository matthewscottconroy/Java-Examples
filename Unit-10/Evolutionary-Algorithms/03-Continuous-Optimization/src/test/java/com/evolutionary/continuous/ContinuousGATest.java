package com.evolutionary.continuous;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContinuousGATest {

    @Test
    @DisplayName("GA minimises a simple sphere function (x1²+x2²)")
    void sphereFunction() {
        double[] lo = {-5, -5}, hi = {5, 5};
        ContinuousGA ga = new ContinuousGA(50, 2, lo, hi, 0.3, 0.5, 5, 1L);
        // Maximise -sphere, i.e. minimise sphere; optimum at (0,0) with value 0
        ContinuousGA.Individual best = ga.evolve(x -> -(x[0]*x[0] + x[1]*x[1]), 300);
        assertEquals(0.0, -best.fitness(), 0.05, "Should find near-zero sphere value");
    }

    @Test
    @DisplayName("Fitness of best individual is non-decreasing across longer runs")
    void longerRunNotWorse() {
        double[] lo = {-5, -5, -5}, hi = {5, 5, 5};
        ContinuousGA gaShort = new ContinuousGA(50, 3, lo, hi, 0.3, 0.5, 5, 2L);
        ContinuousGA gaLong  = new ContinuousGA(50, 3, lo, hi, 0.3, 0.5, 5, 2L);
        double shortFit = gaShort.evolve(x -> -(x[0]*x[0]+x[1]*x[1]+x[2]*x[2]), 20).fitness();
        double longFit  = gaLong .evolve(x -> -(x[0]*x[0]+x[1]*x[1]+x[2]*x[2]), 300).fitness();
        assertTrue(longFit >= shortFit);
    }

    @Test
    @DisplayName("GA stays within bounds")
    void staysInBounds() {
        double[] lo = {1, 2, 3}, hi = {4, 5, 6};
        ContinuousGA ga = new ContinuousGA(50, 3, lo, hi, 1.0, 10.0, 3, 5L);
        ContinuousGA.Individual best = ga.evolve(x -> x[0]+x[1]+x[2], 100);
        double[] g = best.genes();
        for (int d = 0; d < 3; d++) {
            assertTrue(g[d] >= lo[d], "Gene " + d + " below lower bound");
            assertTrue(g[d] <= hi[d], "Gene " + d + " above upper bound");
        }
    }

    @Test
    @DisplayName("Hyperparameter tuner achieves ≥90% of peak accuracy")
    void hyperparameterTuner() {
        double[] lo = {0.0001, 0.0, 0.00001};
        double[] hi = {0.1,    0.5, 0.01};
        ContinuousGA ga = new ContinuousGA(80, 3, lo, hi, 0.3, 0.01, 5, 99L);
        ContinuousGA.Individual best = ga.evolve(Main::simulatedAccuracy, 300);
        double peak = Main.simulatedAccuracy(new double[]{Main.OPT_LR, Main.OPT_DROPOUT, Main.OPT_L2});
        assertTrue(best.fitness() >= 0.90 * peak,
            "GA should find ≥90% of peak accuracy; got " + best.fitness());
    }

    @Test
    @DisplayName("Rastrigin GA gets value below 1.0 (near global minimum)")
    void rastriginNearOptimum() {
        double[] lo = {-5.12, -5.12}, hi = {5.12, 5.12};
        ContinuousGA ga = new ContinuousGA(100, 2, lo, hi, 0.4, 0.5, 5, 7L);
        ContinuousGA.Individual best = ga.evolve(
            x -> -(x[0]*x[0] - 10*Math.cos(2*Math.PI*x[0]) +
                   x[1]*x[1] - 10*Math.cos(2*Math.PI*x[1]) + 20), 500);
        assertTrue(-best.fitness() < 1.0, "Rastrigin value should be near 0");
    }
}

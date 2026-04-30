package com.evolutionary.annealing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class SimulatedAnnealingTest {

    @Test
    @DisplayName("SA reduces cost below a random starting solution")
    void reducesCost() {
        Random rng = new Random(1);
        JobSchedule initial = JobSchedule.random(Main.JOBS, rng);
        double initialCost = initial.weightedCompletionTime();

        SimulatedAnnealing.Config config = new SimulatedAnnealing.Config(
            1000.0, 0.01, 0.99, 30);
        SimulatedAnnealing<JobSchedule> sa = new SimulatedAnnealing<>(
            config, JobSchedule::weightedCompletionTime,
            s -> s.swapNeighbour(new Random()), 42L);

        SimulatedAnnealing.Result<JobSchedule> result = sa.solve(initial);
        assertTrue(result.cost() <= initialCost,
            "SA should not produce a solution worse than the start");
    }

    @Test
    @DisplayName("SA beats a random ordering on the job scheduling problem")
    void beatRandom() {
        // Average random cost over many trials
        Random rng = new Random(0);
        double avgRandom = 0;
        int trials = 20;
        for (int i = 0; i < trials; i++)
            avgRandom += JobSchedule.random(Main.JOBS, rng).weightedCompletionTime();
        avgRandom /= trials;

        JobSchedule start = JobSchedule.random(Main.JOBS, new Random(5));
        SimulatedAnnealing.Config config = new SimulatedAnnealing.Config(
            5000.0, 0.01, 0.995, 50);
        SimulatedAnnealing<JobSchedule> sa = new SimulatedAnnealing<>(
            config, JobSchedule::weightedCompletionTime,
            s -> s.swapNeighbour(new Random()), 42L);

        double saCost = sa.solve(start).cost();
        assertTrue(saCost < avgRandom,
            "SA result (" + saCost + ") should beat average random (" + avgRandom + ")");
    }

    @Test
    @DisplayName("SA on 1D numeric optimisation converges near the minimum")
    void numericOptimisation() {
        // Minimise (x - 3.7)^2 over [-10, 10]
        SimulatedAnnealing.Config config = new SimulatedAnnealing.Config(
            100.0, 0.0001, 0.99, 20);
        Random r = new Random(0);
        SimulatedAnnealing<double[]> sa = new SimulatedAnnealing<>(
            config,
            x -> (x[0] - 3.7) * (x[0] - 3.7),
            x -> { double[] n = x.clone(); n[0] += (r.nextDouble()-0.5)*2; return n; },
            7L
        );
        SimulatedAnnealing.Result<double[]> result = sa.solve(new double[]{0.0});
        assertEquals(3.7, result.solution()[0], 0.2,
            "SA should find x near 3.7 (minimum of (x-3.7)^2)");
    }

    @Test
    @DisplayName("JobSchedule swapNeighbour produces a valid permutation")
    void swapNeighbourIsPermutation() {
        JobSchedule s = JobSchedule.random(Main.JOBS, new Random(1));
        JobSchedule nb = s.swapNeighbour(new Random(2));
        boolean[] seen = new boolean[Main.JOBS.length];
        for (int idx : nb.order()) {
            assertFalse(seen[idx], "Duplicate city " + idx);
            seen[idx] = true;
        }
        for (boolean b : seen) assertTrue(b);
    }
}

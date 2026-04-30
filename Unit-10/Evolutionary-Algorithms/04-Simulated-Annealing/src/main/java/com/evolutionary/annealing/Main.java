package com.evolutionary.annealing;

import java.util.Random;

/**
 * Demonstrates SA on a weighted job scheduling problem.
 *
 * A factory has 10 jobs to schedule on a single machine. Each job has
 * a processing time (hours) and a weight (customer priority). The goal
 * is to sequence them to minimise total weighted completion time —
 * high-priority customers finish their jobs as early as possible.
 */
public class Main {

    static final JobSchedule.Job[] JOBS = {
        new JobSchedule.Job("Invoice-batch",    3, 10),
        new JobSchedule.Job("Report-gen",       5,  6),
        new JobSchedule.Job("Email-blast",      2,  8),
        new JobSchedule.Job("DB-backup",        8,  2),
        new JobSchedule.Job("Analytics-run",    4,  7),
        new JobSchedule.Job("Model-training",   6,  4),
        new JobSchedule.Job("Data-migration",   7,  3),
        new JobSchedule.Job("Cache-warmup",     1,  9),
        new JobSchedule.Job("Audit-log",        3,  5),
        new JobSchedule.Job("Nightly-cleanup",  2,  1),
    };

    public static void main(String[] args) {
        System.out.println("=== Simulated Annealing — Job Scheduler ===\n");

        Random rng = new Random(0);
        JobSchedule initial = JobSchedule.random(JOBS, rng);
        System.out.printf("Initial schedule cost: %.0f%n", initial.weightedCompletionTime());
        System.out.println("Initial order: " + initial);

        // Greedy baseline: SPT/W (shortest processing time ÷ weight)
        int[] sptw = java.util.stream.IntStream.range(0, JOBS.length)
            .boxed()
            .sorted((a, b) -> Double.compare(
                (double) JOBS[a].processingTime() / JOBS[a].weight(),
                (double) JOBS[b].processingTime() / JOBS[b].weight()))
            .mapToInt(Integer::intValue).toArray();
        JobSchedule sptwSchedule = new JobSchedule(sptw, JOBS);
        System.out.printf("SPT/W greedy cost:    %.0f%n", sptwSchedule.weightedCompletionTime());

        // SA
        SimulatedAnnealing.Config config = new SimulatedAnnealing.Config(
            /*initialTemp*/        5000.0,
            /*finalTemp*/          0.01,
            /*coolingRate*/        0.995,
            /*iterationsPerTemp*/  50
        );

        SimulatedAnnealing<JobSchedule> sa = new SimulatedAnnealing<>(
            config,
            JobSchedule::weightedCompletionTime,
            s -> s.swapNeighbour(new Random()),
            42L
        );

        SimulatedAnnealing.Result<JobSchedule> result = sa.solve(initial);

        System.out.printf("%nSA result:%n");
        System.out.printf("  Best cost:   %.0f%n", result.cost());
        System.out.printf("  Iterations:  %,d%n", result.iterations());
        System.out.printf("  Improvement over initial:  %.1f%%%n",
            (initial.weightedCompletionTime() - result.cost()) / initial.weightedCompletionTime() * 100);
        System.out.printf("  vs SPT/W greedy:           %.1f%%%n",
            (sptwSchedule.weightedCompletionTime() - result.cost()) / sptwSchedule.weightedCompletionTime() * 100);
        System.out.println("  Order: " + result.solution());
    }
}

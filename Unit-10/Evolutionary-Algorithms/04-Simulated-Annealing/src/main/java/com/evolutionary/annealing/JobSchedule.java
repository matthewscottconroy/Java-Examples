package com.evolutionary.annealing;

import java.util.Arrays;
import java.util.Random;

/**
 * A schedule of jobs on a single machine.
 *
 * The chromosome is a permutation of job indices. The cost is the total
 * weighted completion time: each job has a weight (importance) and a
 * processing time; the scheduler wants to minimise Σ weight[j] × completion_time[j].
 *
 * This is the classic 1|r_j|Σw_jC_j scheduling problem. The optimal policy
 * for unweighted jobs is shortest-processing-time (SPT), but with weights
 * it becomes NP-hard — making it ideal for SA.
 */
public class JobSchedule {

    public record Job(String name, int processingTime, int weight) {}

    private final int[]   order;   // permutation of job indices
    private final Job[]   jobs;

    public JobSchedule(int[] order, Job[] jobs) {
        this.order = order;
        this.jobs  = jobs;
    }

    public static JobSchedule random(Job[] jobs, Random rng) {
        int n = jobs.length;
        int[] order = new int[n];
        for (int i = 0; i < n; i++) order[i] = i;
        for (int i = n - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = order[i]; order[i] = order[j]; order[j] = tmp;
        }
        return new JobSchedule(order, jobs);
    }

    /** Total weighted completion time (the objective to minimise). */
    public double weightedCompletionTime() {
        double total = 0, time = 0;
        for (int idx : order) {
            time  += jobs[idx].processingTime();
            total += jobs[idx].weight() * time;
        }
        return total;
    }

    /** Neighbour: swap two randomly chosen jobs. */
    public JobSchedule swapNeighbour(Random rng) {
        int[] newOrder = order.clone();
        int i = rng.nextInt(order.length), j = rng.nextInt(order.length);
        int tmp = newOrder[i]; newOrder[i] = newOrder[j]; newOrder[j] = tmp;
        return new JobSchedule(newOrder, jobs);
    }

    public int[] order() { return order; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int idx : order) sb.append(jobs[idx].name()).append(" → ");
        return sb.length() > 3 ? sb.substring(0, sb.length() - 3) : sb.toString();
    }
}

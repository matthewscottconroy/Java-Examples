package com.algorithms.sorting.heapsort;

import java.util.List;
import java.util.Random;

/**
 * Demonstrates heap sort via a task scheduler.
 *
 * Tasks arrive in arbitrary order. The heap always extracts the highest-priority
 * (lowest-numbered) task next — exactly what an OS process scheduler or a
 * hospital triage system needs.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Heap Sort — Task Scheduler ===\n");

        // Tasks arrive in no particular order
        List<Task> incoming = List.of(
            new Task(3, "Send report",      "Weekly summary email to stakeholders"),
            new Task(1, "Fix prod bug",     "NullPointerException in payment service"),
            new Task(5, "Update docs",      "Refresh API reference page"),
            new Task(2, "Deploy hotfix",    "Security patch for auth endpoint"),
            new Task(4, "Code review",      "Review PR #412 from Alice"),
            new Task(1, "Page oncall",      "Disk usage at 98% on db-primary"),
            new Task(3, "Sprint planning",  "Estimate tickets for next sprint"),
            new Task(2, "Rollback release", "v2.3.1 causing latency spikes")
        );

        System.out.println("Tasks as received:");
        incoming.forEach(t -> System.out.println("  " + t));

        // Sort by priority using the heap
        List<Task> ordered = HeapSort.sort(incoming);

        System.out.println("\nExecution order (highest priority first):");
        int step = 1;
        for (Task t : ordered) {
            System.out.printf("  %2d. %s%n", step++, t);
        }

        // ------- Performance comparison -------
        System.out.println("\n--- Performance on random integers ---");
        Random rng = new Random(7);
        int[] sizes = {10_000, 100_000, 500_000};
        for (int n : sizes) {
            List<Integer> data = rng.ints(n, 0, 1_000_000)
                                     .boxed().toList();
            long start = System.currentTimeMillis();
            List<Integer> result = HeapSort.sort(data);
            long ms = System.currentTimeMillis() - start;
            // verify order
            boolean ok = true;
            for (int i = 1; i < result.size(); i++) {
                if (result.get(i) < result.get(i - 1)) { ok = false; break; }
            }
            System.out.printf("  n=%7d  time=%4d ms  sorted=%s%n", n, ms, ok);
        }
    }
}

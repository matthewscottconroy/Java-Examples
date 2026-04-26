package com.concurrency.forkjoin;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * Parallel streams — when to use them and what to watch out for.
 *
 * <p><strong>Good fit:</strong>
 * <ul>
 *   <li>CPU-bound, stateless operations on large data sets</li>
 *   <li>Operations where split and merge overhead is small relative to the work</li>
 * </ul>
 *
 * <p><strong>Pitfalls:</strong>
 * <ul>
 *   <li>Shared mutable state — concurrent writes cause races and incorrect results</li>
 *   <li>Small data — overhead of splitting/joining often exceeds the benefit</li>
 *   <li>Blocking operations — occupies a ForkJoinPool thread, reducing parallelism</li>
 *   <li>Ordering guarantees — parallel streams may produce output in any order
 *       unless {@code forEachOrdered} is used (at the cost of limiting parallelism)</li>
 * </ul>
 *
 * <p>By default, parallel streams use {@link ForkJoinPool#commonPool()}.
 * Submit to a custom pool via pool.submit(() -&gt; stream.parallel()…).
 */
public class ParallelStreamDemo {

    public static void showSumComparison() {
        System.out.println("-- sequential vs parallel sum of 10M longs --");

        long N = 10_000_000L;
        long expected = N * (N + 1) / 2;

        long t1 = System.currentTimeMillis();
        long seqSum = LongStream.rangeClosed(1, N).sum();
        long t2 = System.currentTimeMillis();
        long parSum = LongStream.rangeClosed(1, N).parallel().sum();
        long t3 = System.currentTimeMillis();

        System.out.println("  expected:   " + expected);
        System.out.println("  sequential: " + seqSum + "  (" + (t2-t1) + " ms)");
        System.out.println("  parallel:   " + parSum + "  (" + (t3-t2) + " ms)");
    }

    public static void showSharedMutableStatePitfall() {
        System.out.println("\n-- PITFALL: shared mutable state in parallel stream --");
        // Bad: parallel writes to a plain int[] cause races.
        int[] badCounter = {0};
        IntStream.range(0, 10_000).parallel().forEach(i -> badCounter[0]++);
        System.out.println("  bad  counter: " + badCounter[0] + "  (expected 10000, likely wrong)");

        // Good: use a thread-safe accumulator.
        LongAdder goodCounter = new LongAdder();
        IntStream.range(0, 10_000).parallel().forEach(i -> goodCounter.increment());
        System.out.println("  good counter: " + goodCounter.sum());

        // Better: use a reduction — no shared state at all.
        long reduced = IntStream.range(0, 10_000).parallel().asLongStream().sum();
        System.out.println("  reduced:      " + reduced);
    }

    public static void showOrderingBehaviour() {
        System.out.println("\n-- Ordering: forEachOrdered vs forEach --");
        List<Integer> data = List.of(1, 2, 3, 4, 5, 6, 7, 8);

        System.out.print("  parallel forEach (may be out of order): ");
        data.parallelStream().forEach(n -> System.out.print(n + " "));
        System.out.println();

        System.out.print("  parallel forEachOrdered (always in order, less parallel): ");
        data.parallelStream().forEachOrdered(n -> System.out.print(n + " "));
        System.out.println();
    }

    public static void showCustomPool() throws Exception {
        System.out.println("\n-- Custom ForkJoinPool to control parallelism --");
        ForkJoinPool pool = new ForkJoinPool(2);     // 2 threads instead of cpu count
        try {
            long result = pool.submit(() ->
                    LongStream.rangeClosed(1, 100).parallel().sum()
            ).get();
            System.out.println("  sum via 2-thread pool: " + result);
            System.out.println("  pool parallelism: " + pool.getParallelism());
        } finally {
            pool.shutdown();
        }
    }
}

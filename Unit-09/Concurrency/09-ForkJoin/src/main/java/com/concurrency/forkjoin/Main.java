package com.concurrency.forkjoin;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.LongStream;

public class Main {

    public static void main(String[] args) throws Exception {

        System.out.println("=== RecursiveTask<Long>: parallel array sum ===");
        int N = 1_000_000;
        long[] data = LongStream.rangeClosed(1, N).toArray();
        long expected = (long) N * (N + 1) / 2;

        // Use the common pool (shared across the JVM).
        ForkJoinPool pool = ForkJoinPool.commonPool();
        System.out.println("  commonPool parallelism: " + pool.getParallelism());

        long result = pool.invoke(new SumTask(data));
        System.out.println("  expected: " + expected);
        System.out.println("  actual:   " + result);
        System.out.println("  correct:  " + (result == expected));

        System.out.println("\n=== RecursiveAction: parallel normalise ===");
        double[] values = {1.0, 2.0, 3.0, 4.0, 5.0};
        System.out.println("  before: " + Arrays.toString(values));
        pool.invoke(new NormaliseAction(values, 0.5));
        System.out.println("  after (* 0.5): " + Arrays.toString(values));

        System.out.println("\n=== Parallel Streams ===");
        ParallelStreamDemo.showSumComparison();
        ParallelStreamDemo.showSharedMutableStatePitfall();
        ParallelStreamDemo.showOrderingBehaviour();
        ParallelStreamDemo.showCustomPool();
    }
}

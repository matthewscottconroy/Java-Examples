package com.concurrency.virtual;

public class Main {

    public static void main(String[] args) throws Exception {

        System.out.println("=== Virtual Threads (Java 21) ===");
        VirtualThreadDemo.showCreationApis();
        VirtualThreadDemo.showPerTaskExecutor();
        VirtualThreadDemo.showThroughputComparison();

        System.out.println("\n=== Cancellation & Interruption ===");
        CancellationDemo.showInterrupt();
        CancellationDemo.showFutureCancel();
        CancellationDemo.showTimeoutPattern();

        System.out.println("\n=== Summary: when to use each approach ===");
        System.out.println("Platform threads + fixed pool  → CPU-bound, small number of tasks");
        System.out.println("Platform threads + cached pool → many short I/O tasks (no backpressure)");
        System.out.println("Virtual threads (per-task)     → I/O-bound, high concurrency, thread-per-request");
        System.out.println("CompletableFuture pipelines    → async coordination without dedicated threads");
        System.out.println("ForkJoinPool / parallel stream → divide-and-conquer, CPU-bound data processing");
    }
}

package com.concurrency.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Demonstrates the standard thread pool flavours from {@link Executors}.
 *
 * <p><strong>Always shut down an executor when finished.</strong>
 * An executor owns threads; without shutdown() the JVM will not exit
 * (threads are user threads by default).
 *
 * <table border="1" cellpadding="4">
 *   <tr><th>Factory</th><th>Threads</th><th>Queue</th><th>Best for</th></tr>
 *   <tr><td>newFixedThreadPool(n)</td><td>n fixed</td><td>unbounded</td><td>CPU-bound work</td></tr>
 *   <tr><td>newCachedThreadPool()</td><td>0–∞</td><td>none (SynchronousQueue)</td><td>many short I/O tasks</td></tr>
 *   <tr><td>newSingleThreadExecutor()</td><td>1</td><td>unbounded</td><td>serialised tasks</td></tr>
 *   <tr><td>newWorkStealingPool()</td><td>≈cpu cores</td><td>per-thread deques</td><td>fork/join style</td></tr>
 * </table>
 */
public class PoolTypes {

    static Callable<String> task(String name, int sleepMs) {
        return () -> {
            String thread = Thread.currentThread().getName();
            System.out.printf("  %-20s started  on %s%n", name, thread);
            Thread.sleep(sleepMs);
            System.out.printf("  %-20s finished on %s%n", name, thread);
            return name + " done";
        };
    }

    static void submitAndAwait(ExecutorService exec, int taskCount, int sleepMs)
            throws InterruptedException, ExecutionException {
        List<Future<String>> futures = new ArrayList<>();
        for (int i = 1; i <= taskCount; i++) {
            futures.add(exec.submit(task("task-" + i, sleepMs)));
        }
        for (Future<String> f : futures) f.get();
    }

    public static void showFixed() throws Exception {
        System.out.println("-- newFixedThreadPool(2): 4 tasks, 2 threads --");
        ExecutorService exec = Executors.newFixedThreadPool(2);
        try {
            submitAndAwait(exec, 4, 30);
        } finally {
            exec.shutdown();
            exec.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    public static void showCached() throws Exception {
        System.out.println("\n-- newCachedThreadPool: 4 tasks, threads created on demand --");
        ExecutorService exec = Executors.newCachedThreadPool();
        try {
            submitAndAwait(exec, 4, 30);
        } finally {
            exec.shutdown();
            exec.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    public static void showSingle() throws Exception {
        System.out.println("\n-- newSingleThreadExecutor: tasks serialised --");
        ExecutorService exec = Executors.newSingleThreadExecutor();
        try {
            submitAndAwait(exec, 3, 20);
        } finally {
            exec.shutdown();
            exec.awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}

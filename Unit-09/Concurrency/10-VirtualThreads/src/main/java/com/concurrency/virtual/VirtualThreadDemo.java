package com.concurrency.virtual;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Virtual threads (Project Loom, finalized in Java 21 — JEP 444).
 *
 * <p><strong>Platform thread</strong>: a thin wrapper around an OS thread.
 * Creating thousands of them is expensive (~1 MB stack each).
 *
 * <p><strong>Virtual thread</strong>: scheduled by the JVM, not the OS.
 * Millions can exist simultaneously; each one mounts on a platform
 * "carrier" thread only while it is doing actual work.  When a virtual thread
 * blocks (I/O, sleep, lock), it <em>unmounts</em> and the carrier is freed to
 * run another virtual thread.
 *
 * <p>Key rules for virtual threads:
 * <ul>
 *   <li>Do <em>not</em> pool them — create one per task.  Pooling eliminates
 *       the benefit and reintroduces the cost.</li>
 *   <li>Avoid {@code synchronized} blocks that call blocking I/O — they pin
 *       the carrier thread and negate the benefit.  Use {@link java.util.concurrent.locks.ReentrantLock}
 *       instead.</li>
 *   <li>They are fully compatible with all existing concurrency APIs.</li>
 * </ul>
 */
public class VirtualThreadDemo {

    // -----------------------------------------------------------------------
    // Creation APIs
    // -----------------------------------------------------------------------
    public static void showCreationApis() throws Exception {
        System.out.println("-- Thread.ofVirtual().start() --");

        // Direct start — simplest form.
        Thread vt = Thread.ofVirtual().name("my-vthread").start(() ->
                System.out.println("  running on: " + Thread.currentThread()));
        vt.join();
        System.out.println("  isVirtual: " + vt.isVirtual());

        // Unstarted — useful if you want to start it later.
        Thread lazy = Thread.ofVirtual().name("lazy").unstarted(() ->
                System.out.println("  lazy virtual thread ran"));
        lazy.start();
        lazy.join();

        // Auto-numbered names via factory.
        var factory = Thread.ofVirtual().name("worker-", 1).factory();
        Thread t1 = factory.newThread(() -> System.out.println("  " + Thread.currentThread().getName()));
        Thread t2 = factory.newThread(() -> System.out.println("  " + Thread.currentThread().getName()));
        t1.start(); t2.start(); t1.join(); t2.join();
    }

    // -----------------------------------------------------------------------
    // newVirtualThreadPerTaskExecutor — thread-per-request without pool overhead
    // -----------------------------------------------------------------------
    public static void showPerTaskExecutor() throws Exception {
        System.out.println("\n-- newVirtualThreadPerTaskExecutor --");
        // This executor creates one virtual thread per submitted task.
        // Do NOT call shutdown() to wait — use awaitTermination.
        try (ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 1; i <= 5; i++) {
                final int id = i;
                exec.submit(() -> {
                    System.out.println("  task-" + id + " on " + Thread.currentThread());
                    Thread.sleep(Duration.ofMillis(20));
                    return null;
                });
            }
        }   // try-with-resources calls close() which calls shutdown() + awaitTermination
        System.out.println("  all tasks done");
    }

    // -----------------------------------------------------------------------
    // Throughput comparison: 10 000 tasks with a blocking sleep
    // -----------------------------------------------------------------------
    public static void showThroughputComparison() throws Exception {
        System.out.println("\n-- Throughput: 5000 blocking tasks --");
        final int TASKS = 5_000;
        final long SLEEP_MS = 50;

        long t1 = System.currentTimeMillis();
        try (ExecutorService pool = Executors.newFixedThreadPool(200)) {
            var futures = new java.util.ArrayList<java.util.concurrent.Future<?>>();
            for (int i = 0; i < TASKS; i++) {
                futures.add(pool.submit(() -> { Thread.sleep(SLEEP_MS); return null; }));
            }
            for (var f : futures) f.get();
        }
        long platformMs = System.currentTimeMillis() - t1;

        long t2 = System.currentTimeMillis();
        try (ExecutorService vExec = Executors.newVirtualThreadPerTaskExecutor()) {
            var futures = new java.util.ArrayList<java.util.concurrent.Future<?>>();
            for (int i = 0; i < TASKS; i++) {
                futures.add(vExec.submit(() -> { Thread.sleep(SLEEP_MS); return null; }));
            }
            for (var f : futures) f.get();
        }
        long virtualMs = System.currentTimeMillis() - t2;

        System.out.printf("  Platform threads (pool=200): %4d ms%n", platformMs);
        System.out.printf("  Virtual threads  (per-task): %4d ms%n", virtualMs);
        System.out.println("  (virtual threads complete all 5000 tasks ~concurrently)");
    }
}

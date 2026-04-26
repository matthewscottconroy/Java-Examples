package com.concurrency.executor;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Looks inside {@link ThreadPoolExecutor} — the class that backs all Executors
 * factory-method pools.
 *
 * <p>Key constructor parameters:
 * <pre>
 *   new ThreadPoolExecutor(
 *       corePoolSize,       // threads kept alive even if idle
 *       maximumPoolSize,    // hard cap on live threads
 *       keepAliveTime,      // idle threads above core survive this long
 *       unit,
 *       workQueue,          // unbounded vs. bounded affects backpressure
 *       threadFactory,      // how to name/configure new threads
 *       rejectionHandler    // what happens when queue is full and max threads busy
 *   )
 * </pre>
 *
 * <p>Rejection policies:
 * <ul>
 *   <li>AbortPolicy (default) — throws RejectedExecutionException</li>
 *   <li>CallerRunsPolicy     — caller thread runs the task (natural backpressure)</li>
 *   <li>DiscardPolicy        — silently drops the task</li>
 *   <li>DiscardOldestPolicy  — drops the oldest queued task, retries</li>
 * </ul>
 */
public class PoolInternals {

    // -----------------------------------------------------------------------
    // Custom ThreadFactory — names threads for easier debugging
    // -----------------------------------------------------------------------
    static ThreadFactory namedFactory(String prefix) {
        AtomicInteger count = new AtomicInteger(1);
        return runnable -> {
            Thread t = new Thread(runnable, prefix + "-" + count.getAndIncrement());
            t.setDaemon(false);
            return t;
        };
    }

    public static void showCallerRunsPolicy() throws Exception {
        System.out.println("-- CallerRunsPolicy: queue=1, max=2, 6 tasks --");
        // With a bounded queue of 1 and max 2 threads, tasks 4+ overflow.
        // CallerRunsPolicy makes the CALLING thread run the overflow task,
        // which slows the submitter and naturally limits throughput.
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                1, 2,
                1L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1),
                namedFactory("worker"),
                new ThreadPoolExecutor.CallerRunsPolicy());

        for (int i = 1; i <= 6; i++) {
            final int taskId = i;
            System.out.println("  submitting task-" + taskId
                    + " from " + Thread.currentThread().getName());
            pool.execute(() -> {
                System.out.println("  running  task-" + taskId
                        + " on " + Thread.currentThread().getName());
                try { Thread.sleep(30); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);
    }

    public static void showPoolStats() throws Exception {
        System.out.println("\n-- Pool statistics via ThreadPoolExecutor API --");
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                2, 4,
                10L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10),
                namedFactory("stat"));

        for (int i = 0; i < 6; i++) {
            pool.submit(() -> {
                try { Thread.sleep(50); } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        System.out.println("  active:     " + pool.getActiveCount());
        System.out.println("  pool size:  " + pool.getPoolSize());
        System.out.println("  queued:     " + pool.getQueue().size());
        System.out.println("  completed:  " + pool.getCompletedTaskCount());

        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("  completed after shutdown: " + pool.getCompletedTaskCount());
    }
}

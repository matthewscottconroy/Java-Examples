package com.concurrency.future;

import java.util.concurrent.*;

/**
 * Demonstrates Future&lt;V&gt; and FutureTask&lt;V&gt;.
 *
 * <p>A {@link Future} is a handle to an asynchronous result.  Key methods:
 * <ul>
 *   <li>{@code get()}               — blocks until done, returns result or throws</li>
 *   <li>{@code get(timeout, unit)}  — timed block; throws TimeoutException if slow</li>
 *   <li>{@code isDone()}            — non-blocking check</li>
 *   <li>{@code isCancelled()}       — true if cancelled before completion</li>
 *   <li>{@code cancel(mayInterrupt)} — attempts cancellation</li>
 * </ul>
 *
 * <p>{@link FutureTask} is a concrete {@code RunnableFuture}: it wraps a
 * Callable (or Runnable+result) and can be submitted to an Executor OR
 * run directly on a Thread — useful when you need both behaviours.
 */
public class FutureDemo {

    static void showFutureTask() throws Exception {
        System.out.println("-- FutureTask wrapping a Callable --");

        Callable<Integer> expensiveCalc = () -> {
            Thread.sleep(80);
            return 42;
        };

        FutureTask<Integer> task = new FutureTask<>(expensiveCalc);

        // FutureTask is also Runnable, so it can run on a plain Thread.
        Thread t = new Thread(task, "calc-thread");
        t.start();

        System.out.println("task.isDone() before result: " + task.isDone());
        int result = task.get();    // blocks until the thread finishes
        System.out.println("task.isDone() after get():   " + task.isDone());
        System.out.println("result = " + result);
    }

    static void showTimeout() throws Exception {
        System.out.println("\n-- get(timeout) / TimeoutException --");

        FutureTask<String> slow = new FutureTask<>(() -> {
            Thread.sleep(500);
            return "done";
        });
        new Thread(slow, "slow-thread").start();

        try {
            slow.get(60, TimeUnit.MILLISECONDS);    // too short
        } catch (TimeoutException e) {
            System.out.println("Timed out waiting for slow task");
            slow.cancel(true);  // interrupt the thread to clean up
            System.out.println("Cancelled: " + slow.isCancelled());
        }
    }

    static void showCancellation() throws Exception {
        System.out.println("\n-- cancel() before task starts --");

        FutureTask<String> lazy = new FutureTask<>(() -> {
            Thread.sleep(200);
            System.out.println("lazy task ran (should not appear)");
            return "result";
        });

        lazy.cancel(false);     // cancel before starting
        System.out.println("isCancelled: " + lazy.isCancelled());
        System.out.println("isDone:      " + lazy.isDone());   // cancelled → done

        try {
            lazy.get();
        } catch (CancellationException e) {
            System.out.println("get() on cancelled task threw CancellationException");
        }
    }

    static void showExecutorWithFuture() throws Exception {
        System.out.println("\n-- submit() via ExecutorService returns Future --");

        ExecutorService exec = Executors.newSingleThreadExecutor();
        try {
            Future<String> f = exec.submit(TaskComparison.fetchTask("page-1"));
            System.out.println("Submitted; polling isDone…");
            while (!f.isDone()) {
                System.out.println("  not yet…");
                Thread.sleep(10);
            }
            System.out.println("Result: " + f.get());

            // Callable that throws — get() wraps it in ExecutionException.
            Future<String> bad = exec.submit(TaskComparison.fetchTask("bad"));
            try {
                bad.get();
            } catch (ExecutionException e) {
                System.out.println("ExecutionException cause: " + e.getCause().getMessage());
            }
        } finally {
            exec.shutdown();
        }
    }
}

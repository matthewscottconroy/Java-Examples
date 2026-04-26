package com.concurrency.virtual;

import java.util.concurrent.*;

/**
 * Cooperative cancellation via interruption — works the same for both
 * platform and virtual threads.
 *
 * <p>Java's cancellation model: the requester sets a flag; the target
 * checks the flag and stops voluntarily.  There is no forcible kill.
 *
 * <p>Two ways to check for cancellation inside a task:
 * <ol>
 *   <li>Call a blocking operation — it throws {@link InterruptedException}
 *       when the interrupt flag is set.</li>
 *   <li>Poll {@code Thread.currentThread().isInterrupted()} at safe points
 *       in CPU-bound loops.</li>
 * </ol>
 *
 * <p><strong>Never swallow InterruptedException silently.</strong>
 * Re-throw it, or restore the flag with {@code Thread.currentThread().interrupt()}.
 */
public class CancellationDemo {

    // -----------------------------------------------------------------------
    // Task that responds to interruption at two points:
    //   a) inside Thread.sleep() — throws InterruptedException
    //   b) the isInterrupted() poll between iterations
    // -----------------------------------------------------------------------
    static Callable<String> cancellableTask(String name) {
        return () -> {
            try {
                for (int i = 0; i < 20; i++) {
                    if (Thread.currentThread().isInterrupted()) {
                        System.out.println("  " + name + ": interrupt flag set — stopping at step " + i);
                        return "cancelled at step " + i;
                    }
                    System.out.println("  " + name + ": step " + i);
                    Thread.sleep(25);   // throws InterruptedException if interrupted here
                }
                return "completed";
            } catch (InterruptedException e) {
                // Restore flag so callers up the chain can see it.
                Thread.currentThread().interrupt();
                System.out.println("  " + name + ": interrupted during sleep");
                return "interrupted";
            }
        };
    }

    public static void showInterrupt() throws Exception {
        System.out.println("-- Interrupt a virtual thread --");
        Thread vt = Thread.ofVirtual().name("cancellable").start(() -> {
            try { cancellableTask("vthread").call(); }
            catch (Exception e) { System.out.println("  unexpected: " + e); }
        });
        Thread.sleep(70);
        vt.interrupt();
        vt.join();
        System.out.println("  thread state after join: " + vt.getState());
    }

    public static void showFutureCancel() throws Exception {
        System.out.println("\n-- Future.cancel(true) propagates interrupt --");
        try (ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<String> future = exec.submit(cancellableTask("future-task"));
            Thread.sleep(60);
            boolean cancelled = future.cancel(true);  // sends interrupt to the running thread
            System.out.println("  cancel() returned: " + cancelled);
            try {
                future.get();
            } catch (CancellationException e) {
                System.out.println("  get() threw CancellationException — task was cancelled");
            }
        }
    }

    public static void showTimeoutPattern() throws Exception {
        System.out.println("\n-- Timeout: get(timeout) + cancel --");
        try (ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<String> future = exec.submit(cancellableTask("timeout-task"));
            try {
                String result = future.get(90, TimeUnit.MILLISECONDS);
                System.out.println("  result: " + result);
            } catch (TimeoutException e) {
                System.out.println("  timed out — cancelling");
                future.cancel(true);
            }
        }
    }
}

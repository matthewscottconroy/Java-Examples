package com.concurrency.sync;

import java.util.concurrent.CountDownLatch;

/**
 * Demonstrates a data race on a plain {@code int} counter.
 *
 * <p>{@code count++} is a <em>read-modify-write</em> compound action.
 * Without synchronization, two threads can interleave their reads and writes,
 * causing increments to be lost.  The final count is non-deterministically
 * less than the expected value.
 *
 * <p>This is a <strong>race condition</strong>: a correctness bug whose
 * outcome depends on the relative timing of threads.  It will not reliably
 * reproduce on every run, but with enough threads and iterations it almost
 * always appears.
 */
public class RaceCondition {

    static int unsafeCount = 0;     // shared mutable state — no protection

    public static int demonstrate(int threads, int incrementsPerThread)
            throws InterruptedException {

        unsafeCount = 0;
        CountDownLatch ready  = new CountDownLatch(threads);
        CountDownLatch start  = new CountDownLatch(1);
        CountDownLatch done   = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                ready.countDown();
                try { start.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                for (int j = 0; j < incrementsPerThread; j++) {
                    unsafeCount++;  // NOT atomic: read, add, write — three distinct steps
                }
                done.countDown();
            }).start();
        }

        ready.await();      // wait until all threads are ready
        start.countDown();  // release them all at once to maximize contention
        done.await();

        return unsafeCount;
    }
}

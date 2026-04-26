package com.concurrency.sync;

import java.util.concurrent.CountDownLatch;

/**
 * Two correct solutions to the race in {@link RaceCondition}.
 *
 * <p><strong>synchronized</strong> — gives each thread exclusive ownership of
 * the intrinsic lock (monitor) on {@code this} before executing the method body.
 * Every other thread trying to enter <em>any</em> synchronized method on the
 * same object blocks until the lock is released.
 *
 * <p><strong>volatile</strong> — ensures every read sees the most recently
 * written value (visibility) but does <em>not</em> make compound actions
 * (read-modify-write) atomic.  Correct only for single-writer scenarios, or
 * when a single flag is written once and read by many.
 *
 * <p>For high-performance counters see {@link java.util.concurrent.atomic.AtomicInteger}
 * (example 07) or {@link java.util.concurrent.atomic.LongAdder}.
 */
public class SafeCounter {

    // -----------------------------------------------------------------------
    // synchronized — mutual exclusion on the intrinsic lock
    // -----------------------------------------------------------------------
    static class SynchronizedCounter {
        private int count = 0;

        // The lock is acquired on entry and released on exit (including exceptions).
        public synchronized void increment() { count++; }
        public synchronized int  get()       { return count; }

        // Alternatively, synchronize on an explicit block (finer-grained scope):
        // private final Object lock = new Object();
        // public void increment() { synchronized (lock) { count++; } }
    }

    // -----------------------------------------------------------------------
    // volatile flag — visibility guarantee for a stop signal (single writer)
    // -----------------------------------------------------------------------
    static class Worker implements Runnable {
        private volatile boolean running = true;    // visible to all threads immediately

        public void stop() { running = false; }     // single writer — volatile is enough

        @Override
        public void run() {
            int steps = 0;
            while (running) {   // reads the up-to-date value on every check
                steps++;
                Thread.yield();
            }
            System.out.println("  Worker stopped after " + steps + " steps");
        }
    }

    public static int countSafe(int threads, int incrementsPerThread)
            throws InterruptedException {

        SynchronizedCounter counter = new SynchronizedCounter();
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done  = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                ready.countDown();
                try { start.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                for (int j = 0; j < incrementsPerThread; j++) {
                    counter.increment();    // atomic under the intrinsic lock
                }
                done.countDown();
            }).start();
        }

        ready.await();
        start.countDown();
        done.await();
        return counter.get();
    }
}

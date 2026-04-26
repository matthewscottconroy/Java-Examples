package com.concurrency.locks;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link ReentrantLock} compared to the {@code synchronized} keyword.
 *
 * <p>Advantages over synchronized:
 * <ul>
 *   <li>{@code tryLock()} — non-blocking attempt; prevents deadlocks</li>
 *   <li>{@code tryLock(time, unit)} — timed attempt</li>
 *   <li>{@code lockInterruptibly()} — blocks but responds to interrupt</li>
 *   <li>Fairness option — threads acquire in FIFO order (at a throughput cost)</li>
 *   <li>Multiple {@link java.util.concurrent.locks.Condition} objects per lock</li>
 * </ul>
 *
 * <p><strong>Always release in a {@code finally} block</strong> — a lock not
 * released blocks every thread that later tries to acquire it.
 */
public class ReentrantLockDemo {

    // -----------------------------------------------------------------------
    // Basic usage — identical semantics to synchronized, but explicit
    // -----------------------------------------------------------------------
    static class Counter {
        private final ReentrantLock lock = new ReentrantLock();
        private int count = 0;

        public void increment() {
            lock.lock();
            try {
                count++;
            } finally {
                lock.unlock();  // always in finally — never skipped by an exception
            }
        }

        public int get() {
            lock.lock();
            try { return count; } finally { lock.unlock(); }
        }
    }

    // -----------------------------------------------------------------------
    // tryLock() — avoids blocking; useful for deadlock avoidance
    // -----------------------------------------------------------------------
    static void showTryLock() throws InterruptedException {
        System.out.println("-- tryLock() --");
        ReentrantLock lockA = new ReentrantLock();
        ReentrantLock lockB = new ReentrantLock();

        // Thread 1 holds lockA, tries for lockB with a timeout.
        Thread t1 = new Thread(() -> {
            lockA.lock();
            try {
                System.out.println("  t1: holds lockA, trying lockB…");
                try {
                    boolean got = lockB.tryLock(50, TimeUnit.MILLISECONDS);
                    if (got) {
                        try { System.out.println("  t1: acquired lockB"); }
                        finally { lockB.unlock(); }
                    } else {
                        System.out.println("  t1: timed out on lockB — backing off (no deadlock)");
                    }
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            } finally { lockA.unlock(); }
        }, "t1");

        // Thread 2 holds lockB (for longer) to cause the timeout.
        Thread t2 = new Thread(() -> {
            lockB.lock();
            try {
                System.out.println("  t2: holding lockB for 100 ms");
                try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            } finally { lockB.unlock(); }
        }, "t2");

        t2.start(); Thread.sleep(10); t1.start();
        t1.join(); t2.join();
    }

    // -----------------------------------------------------------------------
    // Reentrancy — same thread can acquire the lock it already holds
    // -----------------------------------------------------------------------
    static void showReentrancy() {
        System.out.println("\n-- Reentrancy --");
        ReentrantLock lock = new ReentrantLock();
        lock.lock();
        System.out.println("  held count after lock(): " + lock.getHoldCount());
        lock.lock();    // same thread acquires again — does not deadlock
        System.out.println("  held count after lock() again: " + lock.getHoldCount());
        lock.unlock();
        System.out.println("  held count after first unlock(): " + lock.getHoldCount());
        lock.unlock();
        System.out.println("  held count after second unlock(): " + lock.getHoldCount());
    }
}

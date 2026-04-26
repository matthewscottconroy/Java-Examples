package com.concurrency.threads;

import java.util.concurrent.locks.LockSupport;

/**
 * Demonstrates the six thread lifecycle states.
 *
 * <pre>
 *  NEW ──► RUNNABLE ──► TERMINATED
 *              │
 *         ┌───┴───────────┐
 *         ▼               ▼
 *      BLOCKED        WAITING / TIMED_WAITING
 * </pre>
 *
 * NEW            — created with new Thread(), not yet started
 * RUNNABLE       — executing or ready; includes "running" and "ready to run"
 * BLOCKED        — waiting to acquire a monitor held by another thread
 * WAITING        — waiting indefinitely (Object.wait, Thread.join, LockSupport.park)
 * TIMED_WAITING  — waiting with a timeout (Thread.sleep, Object.wait(ms), etc.)
 * TERMINATED     — run() returned or threw an uncaught exception
 */
public class ThreadStates {

    private static final Object LOCK = new Object();

    public static void demonstrate() throws InterruptedException {

        // --- NEW ---
        Thread t = new Thread(() -> {
            try { Thread.sleep(200); } catch (InterruptedException ignored) {}
        }, "demo-thread");
        System.out.println("After new Thread():   " + t.getState());   // NEW

        t.start();
        Thread.sleep(10);
        System.out.println("After start():        " + t.getState());   // RUNNABLE or TIMED_WAITING

        // --- TIMED_WAITING (sleeping) ---
        // The thread above is sleeping; give OS time to schedule it into sleep.
        Thread.sleep(20);
        System.out.println("While sleeping:       " + t.getState());   // TIMED_WAITING

        t.join();
        System.out.println("After run() returns:  " + t.getState());   // TERMINATED

        // --- BLOCKED ---
        // A thread is BLOCKED when it's trying to acquire a monitor already held.
        Object monitor = new Object();
        synchronized (monitor) {                    // we hold the monitor
            Thread blocker = new Thread(() -> {
                synchronized (monitor) {}           // this thread tries to acquire it → BLOCKED
            }, "blocker");
            blocker.start();
            Thread.sleep(20);                       // give the thread time to get stuck
            System.out.println("Waiting for monitor:  " + blocker.getState());  // BLOCKED
        }   // we release the monitor here; blocker can proceed to TERMINATED

        // --- WAITING (indefinite) ---
        Thread parker = new Thread(LockSupport::park, "parker");
        parker.start();
        Thread.sleep(20);
        System.out.println("LockSupport.parked:   " + parker.getState());  // WAITING
        LockSupport.unpark(parker);
        parker.join();
    }
}

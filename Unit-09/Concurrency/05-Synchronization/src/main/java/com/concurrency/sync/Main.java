package com.concurrency.sync;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        final int THREADS = 8, ITERS = 10_000;
        final int expected = THREADS * ITERS;

        // ----------------------------------------------------------------
        // Race condition: lost updates without synchronization
        // ----------------------------------------------------------------
        System.out.println("=== Race Condition ===");
        System.out.println("Expected: " + expected);
        int raceResult = RaceCondition.demonstrate(THREADS, ITERS);
        System.out.println("Actual (unsafe):  " + raceResult
                + (raceResult == expected ? " (got lucky — run again)" : " ← LOST UPDATES"));

        // ----------------------------------------------------------------
        // Fix 1: synchronized
        // ----------------------------------------------------------------
        System.out.println("\n=== synchronized — mutual exclusion ===");
        int safeResult = SafeCounter.countSafe(THREADS, ITERS);
        System.out.println("Expected: " + expected);
        System.out.println("Actual (safe):    " + safeResult
                + (safeResult == expected ? " ✓" : " ✗ BUG"));

        // ----------------------------------------------------------------
        // Fix 2: volatile for a visibility-only stop flag
        // ----------------------------------------------------------------
        System.out.println("\n=== volatile — visibility for a stop flag ===");
        SafeCounter.Worker w = new SafeCounter.Worker();
        Thread wThread = new Thread(w, "volatile-worker");
        wThread.start();
        Thread.sleep(30);
        w.stop();           // volatile write — immediately visible to the worker
        wThread.join();

        // ----------------------------------------------------------------
        // wait / notify producer-consumer
        // ----------------------------------------------------------------
        System.out.println("\n=== wait / notifyAll — bounded buffer ===");
        WaitNotify.demonstrate();
    }
}

package com.concurrency.threads;

/**
 * Three ways to define a thread's task; only one way to start it.
 *
 * <p><strong>Prefer Runnable over extending Thread.</strong>
 * Extending Thread ties the task to the threading mechanism — you can't
 * re-submit the same task to a thread pool, for example.  Runnable is a
 * pure functional interface; it separates the <em>what</em> from the <em>how</em>.
 */
public class RunnerComparison {

    // -----------------------------------------------------------------------
    // Approach 1: extend Thread (avoid in new code)
    // Works, but the task is coupled to the thread machinery.
    // -----------------------------------------------------------------------
    static class CounterThread extends Thread {
        private final String label;
        CounterThread(String label) { this.label = label; }

        @Override
        public void run() {
            for (int i = 1; i <= 3; i++) {
                System.out.println(label + " (extends Thread): " + i);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Approach 2: implement Runnable, pass to Thread constructor (preferred)
    // The task is reusable — the same instance can be handed to a pool later.
    // -----------------------------------------------------------------------
    static class CounterRunnable implements Runnable {
        private final String label;
        CounterRunnable(String label) { this.label = label; }

        @Override
        public void run() {
            for (int i = 1; i <= 3; i++) {
                System.out.println(label + " (implements Runnable): " + i);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Approach 3: lambda — Runnable is a @FunctionalInterface (Java 8+)
    // Most concise; ideal when the task fits in a few lines.
    // -----------------------------------------------------------------------
    static Runnable lambdaTask(String label) {
        return () -> {
            for (int i = 1; i <= 3; i++) {
                System.out.println(label + " (lambda Runnable): " + i);
            }
        };
    }

    public static void demonstrate() throws InterruptedException {
        Thread t1 = new CounterThread("T1");
        Thread t2 = new Thread(new CounterRunnable("T2"));
        Thread t3 = new Thread(lambdaTask("T3"), "lambda-thread");

        // All three are started the same way regardless of how the task was defined.
        t1.start(); t2.start(); t3.start();
        t1.join();  t2.join();  t3.join();
    }
}

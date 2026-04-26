package com.concurrency.threads;

/**
 * Demonstrates interrupt(), join(), sleep(), and daemon threads.
 *
 * <p>Java's cancellation model is <em>cooperative</em>: calling interrupt()
 * sets a flag on the target thread; the target must check the flag and decide
 * what to do.  Blocking methods (sleep, wait, join, I/O) throw
 * {@link InterruptedException} when the flag is set while they are blocking.
 *
 * <p>Two rules for handling InterruptedException:
 * <ol>
 *   <li>Never silently swallow it — either re-throw or restore the flag.</li>
 *   <li>If you catch it and cannot re-throw, call
 *       {@code Thread.currentThread().interrupt()} to restore the status.</li>
 * </ol>
 */
public class InterruptDemo {

    // -----------------------------------------------------------------------
    // Long-running task that responds to interruption correctly
    // -----------------------------------------------------------------------
    static Runnable interruptibleTask(String name) {
        return () -> {
            System.out.println(name + ": starting");
            try {
                for (int i = 0; i < 10; i++) {
                    // Check the flag before each unit of work.
                    if (Thread.currentThread().isInterrupted()) {
                        System.out.println(name + ": saw interrupt flag, stopping");
                        return;
                    }
                    System.out.println(name + ": working step " + i);
                    Thread.sleep(40);       // InterruptedException clears the flag
                }
            } catch (InterruptedException e) {
                // sleep() threw because interrupt() was called while we were sleeping.
                // Restore the interrupt status — callers up the stack may need to see it.
                Thread.currentThread().interrupt();
                System.out.println(name + ": interrupted during sleep — restoring status");
            }
        };
    }

    // -----------------------------------------------------------------------
    // Daemon thread demo
    // A daemon thread does NOT prevent the JVM from exiting.
    // When all user threads finish, the JVM shuts down (daemon threads die with it).
    // -----------------------------------------------------------------------
    static Thread makeDaemon() {
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    System.out.println("daemon: still alive...");
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }, "daemon-thread");
        t.setDaemon(true);   // must be set BEFORE start()
        return t;
    }

    public static void demonstrate() throws InterruptedException {

        // --- interrupt() during sleep ---
        System.out.println("-- interrupt during sleep --");
        Thread sleeper = new Thread(interruptibleTask("sleeper"), "sleeper");
        sleeper.start();
        Thread.sleep(80);       // let it do a couple of steps
        sleeper.interrupt();    // interrupt while it is sleeping
        sleeper.join();

        // --- interrupt() between steps ---
        System.out.println("\n-- interrupt between steps --");
        Thread worker = new Thread(interruptibleTask("worker"), "worker");
        worker.start();
        Thread.sleep(10);       // let it barely start, catch it between steps
        worker.interrupt();
        worker.join();

        // --- join() waiting for completion ---
        System.out.println("\n-- join() ---");
        Thread quick = new Thread(() -> {
            try { Thread.sleep(60); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            System.out.println("quick: done");
        }, "quick");
        long before = System.currentTimeMillis();
        quick.start();
        quick.join();           // main blocks here until 'quick' finishes
        System.out.println("main: join() returned after ~" + (System.currentTimeMillis() - before) + " ms");

        // --- daemon thread ---
        System.out.println("\n-- daemon thread ---");
        Thread daemon = makeDaemon();
        daemon.start();
        Thread.sleep(150);      // let daemon print a couple of times
        // When main() returns, the JVM exits and kills the daemon.
        System.out.println("main: exiting (daemon will be killed by JVM shutdown)");
    }
}

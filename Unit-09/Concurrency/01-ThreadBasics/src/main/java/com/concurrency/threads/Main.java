package com.concurrency.threads;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        System.out.println("=== Thread Lifecycle States ===");
        ThreadStates.demonstrate();

        System.out.println("\n=== Thread vs Runnable vs Lambda ===");
        RunnerComparison.demonstrate();

        System.out.println("\n=== Interrupt, Join, Daemon ===");
        InterruptDemo.demonstrate();

        // ThreadLocal: each thread sees its own copy of the variable.
        System.out.println("\n=== ThreadLocal ===");
        ThreadLocal<String> identity = new ThreadLocal<>();

        Runnable showIdentity = () -> {
            identity.set("I am " + Thread.currentThread().getName());
            try { Thread.sleep(20); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            // Each thread reads its own value, not another thread's.
            System.out.println(Thread.currentThread().getName() + " → " + identity.get());
            identity.remove();  // always clean up in long-running threads to avoid memory leaks
        };

        Thread ta = new Thread(showIdentity, "thread-alpha");
        Thread tb = new Thread(showIdentity, "thread-beta");
        ta.start(); tb.start();
        ta.join();  tb.join();

        System.out.println("\n=== Thread metadata ===");
        Thread main = Thread.currentThread();
        System.out.println("name:     " + main.getName());
        System.out.println("id:       " + main.threadId());
        System.out.println("daemon:   " + main.isDaemon());
        System.out.println("priority: " + main.getPriority()
                + "  (range 1–10, default " + Thread.NORM_PRIORITY + ")");
        System.out.println("virtual:  " + main.isVirtual());   // false for platform threads
    }
}

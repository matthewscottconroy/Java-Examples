package com.concurrency.future;

public class Main {

    public static void main(String[] args) throws Exception {

        System.out.println("=== Runnable vs Callable ===");
        // Runnable: submit to a Thread, no result.
        Thread t = new Thread(TaskComparison.logTask("hello from runnable"), "r-thread");
        t.start(); t.join();

        // Callable: must run through FutureTask or ExecutorService.
        System.out.println("(Callable used below via FutureTask and ExecutorService)");

        System.out.println("\n=== FutureTask ===");
        FutureDemo.showFutureTask();
        FutureDemo.showTimeout();
        FutureDemo.showCancellation();
        FutureDemo.showExecutorWithFuture();
    }
}

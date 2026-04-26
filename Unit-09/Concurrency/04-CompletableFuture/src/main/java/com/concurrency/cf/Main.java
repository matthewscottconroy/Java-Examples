package com.concurrency.cf;

import java.util.concurrent.CompletableFuture;

public class Main {

    public static void main(String[] args) throws Exception {

        System.out.println("=== Creation ===");
        // supplyAsync: runs a Supplier in the ForkJoinPool.commonPool() by default.
        CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> "hello");

        // runAsync: Runnable, no return value.
        CompletableFuture<Void> r = CompletableFuture.runAsync(() ->
                System.out.println("  runAsync on " + Thread.currentThread().getName()));
        r.get();

        // completedFuture: already-done, useful for testing or providing defaults.
        CompletableFuture<Integer> done = CompletableFuture.completedFuture(42);
        System.out.println("  completedFuture.get() = " + done.get());
        System.out.println("  isDone: " + done.isDone());

        System.out.println("\n=== Chaining (thenApply / thenCompose) ===");
        Pipeline.demonstrate();

        System.out.println("\n=== Combining (thenCombine / allOf / anyOf) ===");
        Combining.demonstrate();

        System.out.println("\n=== Error Handling ===");
        ErrorHandling.demonstrate();
    }
}

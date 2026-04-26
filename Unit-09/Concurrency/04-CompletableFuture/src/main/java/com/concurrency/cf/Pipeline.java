package com.concurrency.cf;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Demonstrates CompletableFuture chaining — building async pipelines without
 * explicit thread management or callbacks.
 *
 * <p>Key chaining methods:
 * <ul>
 *   <li>{@code thenApply(fn)}   — transform the result (like Stream.map)</li>
 *   <li>{@code thenAccept(fn)}  — consume the result, return void stage</li>
 *   <li>{@code thenRun(fn)}     — run after completion, ignores result</li>
 *   <li>{@code thenCompose(fn)} — flat-map: fn returns a new CompletableFuture
 *                                  (avoids CompletableFuture&lt;CompletableFuture&lt;V&gt;&gt;)</li>
 * </ul>
 *
 * <p>Async variants ({@code thenApplyAsync}, etc.) offload the continuation
 * to a different thread.  Without "Async", the continuation may run on the
 * completing thread or the caller, depending on timing.
 */
public class Pipeline {

    // Simulates fetching a value asynchronously.
    static CompletableFuture<String> fetchAsync(String url) {
        return CompletableFuture.supplyAsync(() -> {
            sleep(40);
            return "raw:" + url;
        });
    }

    // Simulates a second async step.
    static CompletableFuture<Integer> parseAsync(String raw) {
        return CompletableFuture.supplyAsync(() -> {
            sleep(20);
            return raw.length();
        });
    }

    public static void demonstrate() throws Exception {
        System.out.println("-- thenApply / thenAccept / thenRun --");

        CompletableFuture.supplyAsync(() -> {
                    sleep(20);
                    return 6;
                })
                .thenApply(n -> n * n)              // 36
                .thenApply(n -> "result=" + n)      // "result=36"
                .thenAccept(s -> System.out.println("  thenAccept: " + s))
                .thenRun(() -> System.out.println("  thenRun: pipeline complete"))
                .get();

        System.out.println("\n-- thenCompose (flat-map) --");
        // thenApply(fn) where fn returns CompletableFuture gives CF<CF<V>>.
        // thenCompose(fn) unwraps one level, giving CF<V>.
        String result = fetchAsync("example.com")
                .thenCompose(Pipeline::parseAsync)  // flat-maps to CF<Integer>
                .thenApply(len -> "length=" + len)
                .get();
        System.out.println("  " + result);

        System.out.println("\n-- Async variant with custom executor --");
        ExecutorService custom = Executors.newFixedThreadPool(2,
                r -> new Thread(r, "custom-pool"));
        CompletableFuture.supplyAsync(() -> "hello", custom)
                .thenApplyAsync(String::toUpperCase, custom)
                .thenAcceptAsync(s -> System.out.println("  on " + Thread.currentThread().getName() + ": " + s), custom)
                .get();
        custom.shutdown();
    }

    static void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}

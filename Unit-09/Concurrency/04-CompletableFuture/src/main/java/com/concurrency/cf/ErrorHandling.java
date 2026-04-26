package com.concurrency.cf;

import java.util.concurrent.CompletableFuture;

/**
 * Error handling in CompletableFuture pipelines.
 *
 * <table border="1" cellpadding="4">
 *   <tr><th>Method</th><th>Runs when</th><th>Can recover</th></tr>
 *   <tr><td>exceptionally(fn)</td><td>exception only</td><td>yes — fn returns replacement value</td></tr>
 *   <tr><td>handle(fn)</td><td>always (result or exception)</td><td>yes — fn maps both paths</td></tr>
 *   <tr><td>whenComplete(fn)</td><td>always (side-effect)</td><td>no — cannot change the result</td></tr>
 * </table>
 *
 * <p>Exceptions in CompletableFuture are wrapped in {@link java.util.concurrent.CompletionException}
 * when rethrown from {@code join()}, or {@link java.util.concurrent.ExecutionException}
 * from {@code get()}.
 */
public class ErrorHandling {

    static CompletableFuture<Integer> failing() {
        return CompletableFuture.supplyAsync(() -> {
            throw new RuntimeException("simulated failure");
        });
    }

    static CompletableFuture<Integer> succeeding(int value) {
        return CompletableFuture.supplyAsync(() -> value);
    }

    public static void demonstrate() throws Exception {
        System.out.println("-- exceptionally: recover from exception --");
        int recovered = failing()
                .exceptionally(ex -> {
                    System.out.println("  exceptionally caught: " + ex.getMessage());
                    return -1;          // substitute value
                })
                .get();
        System.out.println("  recovered value: " + recovered);

        System.out.println("\n-- handle: always runs, sees result OR exception --");
        // success path
        String ok = succeeding(42)
                .handle((result, ex) -> ex != null ? "error:" + ex.getMessage() : "ok:" + result)
                .get();
        System.out.println("  success path: " + ok);

        // failure path
        String err = failing()
                .handle((result, ex) -> ex != null ? "error:" + ex.getMessage() : "ok:" + result)
                .get();
        System.out.println("  failure path: " + err);

        System.out.println("\n-- whenComplete: side-effect, does not change result --");
        try {
            failing()
                    .whenComplete((result, ex) -> {
                        if (ex != null) System.out.println("  whenComplete saw exception: " + ex.getMessage());
                        else            System.out.println("  whenComplete saw result: " + result);
                    })
                    .get();     // still throws — whenComplete did not recover it
        } catch (Exception e) {
            System.out.println("  get() still threw: " + e.getCause().getMessage());
        }

        System.out.println("\n-- chaining after exceptionally --");
        // Recovery feeds back into the pipeline.
        int finalResult = failing()
                .exceptionally(ex -> 0)
                .thenApply(n -> n + 100)
                .get();
        System.out.println("  final after recovery + transform: " + finalResult);
    }
}

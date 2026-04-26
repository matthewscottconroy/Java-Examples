package com.concurrency.cf;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Combining multiple CompletableFutures.
 *
 * <ul>
 *   <li>{@code thenCombine(other, fn)} — wait for exactly two futures, merge results</li>
 *   <li>{@code allOf(futures...)}      — wait for ALL; result is CF&lt;Void&gt;</li>
 *   <li>{@code anyOf(futures...)}      — complete when the FIRST finishes; result is CF&lt;Object&gt;</li>
 * </ul>
 */
public class Combining {

    static CompletableFuture<Integer> slowInt(int value, int delayMs) {
        return CompletableFuture.supplyAsync(() -> {
            Pipeline.sleep(delayMs);
            System.out.println("  produced " + value);
            return value;
        });
    }

    public static void demonstrate() throws Exception {
        System.out.println("-- thenCombine: merge two independent futures --");
        // Both futures run concurrently; thenCombine waits for both.
        CompletableFuture<Integer> f1 = slowInt(10, 50);
        CompletableFuture<Integer> f2 = slowInt(20, 30);
        int combined = f1.thenCombine(f2, Integer::sum).get();
        System.out.println("  combined sum = " + combined);

        System.out.println("\n-- allOf: wait for a list of futures --");
        List<CompletableFuture<Integer>> futures = List.of(
                slowInt(1, 40), slowInt(2, 20), slowInt(3, 60));

        // allOf returns CF<Void>; to collect results, join each future after allOf.
        CompletableFuture<Void> all = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0]));
        all.get();
        List<Integer> results = futures.stream()
                .map(CompletableFuture::join)   // join() = get() without checked exception
                .collect(Collectors.toList());
        System.out.println("  all results: " + results);

        System.out.println("\n-- anyOf: first to finish wins --");
        CompletableFuture<Object> first = CompletableFuture.anyOf(
                slowInt(100, 80),
                slowInt(200, 20),   // this will win
                slowInt(300, 60));
        System.out.println("  first result: " + first.get());
        // Note: the other futures keep running in the background.
        Pipeline.sleep(100);   // let the output settle
    }
}

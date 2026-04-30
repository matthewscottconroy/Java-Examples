package com.functional.async;

import java.util.concurrent.CompletableFuture;

/**
 * Demonstrates CompletableFuture with an async dashboard that fetches
 * weather, stock price, and news in parallel.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Async Dashboard (CompletableFuture) ===\n");

        // Sequential baseline: 300 + 400 + 250 = ~950 ms
        System.out.println("Sequential fetch:");
        long t0 = System.currentTimeMillis();
        String weather  = RemoteServices.fetchWeather("London").get();
        double stocks   = RemoteServices.fetchPortfolioValue("u001").get();
        String headline = RemoteServices.fetchHeadline().get();
        System.out.printf("  Done in %d ms%n%n", System.currentTimeMillis() - t0);

        // Parallel with allOf: all three run concurrently — max ~400 ms
        System.out.println("Parallel fetch (allOf):");
        t0 = System.currentTimeMillis();

        CompletableFuture<String> weatherFuture   = RemoteServices.fetchWeather("London");
        CompletableFuture<Double> stocksFuture     = RemoteServices.fetchPortfolioValue("u001");
        CompletableFuture<String> headlineFuture   = RemoteServices.fetchHeadline();

        CompletableFuture<DashboardData> dashboard =
                CompletableFuture.allOf(weatherFuture, stocksFuture, headlineFuture)
                        .thenApply(v -> new DashboardData(
                                weatherFuture.join(),
                                stocksFuture.join(),
                                headlineFuture.join()));

        DashboardData data = dashboard.get();
        System.out.printf("  Done in %d ms%n", System.currentTimeMillis() - t0);
        System.out.println("  " + data);

        // thenApply — transform the result of a future
        System.out.println("\nthenApply (transform result):");
        String summary = RemoteServices.fetchWeather("Paris")
                .thenApply(w -> "Weather update: " + w)
                .get();
        System.out.println("  " + summary);

        // thenCompose — chain two dependent futures (flatMap for futures)
        System.out.println("\nthenCompose (chain dependent futures):");
        CompletableFuture<String> chained = RemoteServices.fetchPortfolioValue("u002")
                .thenCompose(value -> CompletableFuture.supplyAsync(
                        () -> String.format("Portfolio $%.2f — %s", value,
                                value > 25_000 ? "BUY signal" : "HOLD signal")));
        System.out.println("  " + chained.get());

        // exceptionally — recover from failure
        System.out.println("\nexceptionally (error recovery):");
        String safe = CompletableFuture.<String>supplyAsync(() -> {
                    throw new RuntimeException("API timeout");
                })
                .exceptionally(ex -> "Cached weather: Overcast, 15°C")
                .get();
        System.out.println("  " + safe);
    }
}

package com.functional.async;

import java.util.concurrent.CompletableFuture;

/**
 * Simulated remote API calls, each sleeping to mimic network latency.
 *
 * <p>In production these would use an HTTP client; here they just
 * {@link Thread#sleep(long)} to demonstrate that the three calls
 * can run concurrently.
 */
public final class RemoteServices {

    private RemoteServices() {}

    /** Simulates a 300 ms weather API call. */
    public static CompletableFuture<String> fetchWeather(String city) {
        return CompletableFuture.supplyAsync(() -> {
            sleep(300);
            return "Partly cloudy, 18°C in " + city;
        });
    }

    /** Simulates a 400 ms stock-price API call. */
    public static CompletableFuture<Double> fetchPortfolioValue(String userId) {
        return CompletableFuture.supplyAsync(() -> {
            sleep(400);
            return 24_387.50 + userId.hashCode() % 1000;
        });
    }

    /** Simulates a 250 ms news API call. */
    public static CompletableFuture<String> fetchHeadline() {
        return CompletableFuture.supplyAsync(() -> {
            sleep(250);
            return "Central bank holds rates steady for third consecutive meeting";
        });
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}

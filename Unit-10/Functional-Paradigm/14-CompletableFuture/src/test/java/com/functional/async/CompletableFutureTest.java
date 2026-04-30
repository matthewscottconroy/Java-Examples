package com.functional.async;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class CompletableFutureTest {

    @Test
    @DisplayName("fetchWeather completes with a non-empty string")
    void fetchWeatherReturnsString() throws Exception {
        String result = RemoteServices.fetchWeather("London").get();
        assertNotNull(result);
        assertFalse(result.isBlank());
        assertTrue(result.contains("London"));
    }

    @Test
    @DisplayName("fetchPortfolioValue returns a positive number")
    void fetchPortfolioReturnsPositive() throws Exception {
        double value = RemoteServices.fetchPortfolioValue("u001").get();
        assertTrue(value > 0);
    }

    @Test
    @DisplayName("thenApply transforms the future's value")
    void thenApplyTransforms() throws Exception {
        String result = CompletableFuture.supplyAsync(() -> "hello")
                .thenApply(String::toUpperCase)
                .get();
        assertEquals("HELLO", result);
    }

    @Test
    @DisplayName("thenCompose chains dependent futures without nesting")
    void thenComposeChains() throws Exception {
        String result = CompletableFuture.supplyAsync(() -> "user")
                .thenCompose(id -> CompletableFuture.supplyAsync(() -> "Profile of " + id))
                .get();
        assertEquals("Profile of user", result);
    }

    @Test
    @DisplayName("allOf waits for all futures before proceeding")
    void allOfWaitsForAll() throws Exception {
        CompletableFuture<String>  a = RemoteServices.fetchWeather("Paris");
        CompletableFuture<String>  b = RemoteServices.fetchHeadline();
        CompletableFuture<Void> all  = CompletableFuture.allOf(a, b);
        all.get();
        assertTrue(a.isDone());
        assertTrue(b.isDone());
    }

    @Test
    @DisplayName("exceptionally provides a fallback on failure")
    void exceptionallyFallback() throws Exception {
        String result = CompletableFuture.<String>supplyAsync(() -> {
                    throw new RuntimeException("network error");
                })
                .exceptionally(ex -> "fallback")
                .get();
        assertEquals("fallback", result);
    }

    @Test
    @DisplayName("Three parallel futures complete in less time than sequential sum")
    void parallelIsFasterThanSequential() throws Exception {
        long t0  = System.currentTimeMillis();
        CompletableFuture<String> w = RemoteServices.fetchWeather("Berlin");
        CompletableFuture<Double> s = RemoteServices.fetchPortfolioValue("u001");
        CompletableFuture<String> h = RemoteServices.fetchHeadline();
        CompletableFuture.allOf(w, s, h).get();
        long parallel = System.currentTimeMillis() - t0;

        // Sequential would be at least 300+400+250 = 950ms; parallel should be ≤ 500ms
        assertTrue(parallel < 900,
                "Parallel should be faster than sequential sum; took " + parallel + " ms");
    }
}

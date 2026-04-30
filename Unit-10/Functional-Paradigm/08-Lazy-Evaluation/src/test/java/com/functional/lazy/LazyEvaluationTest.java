package com.functional.lazy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class LazyEvaluationTest {

    @Test
    @DisplayName("Supplier is not called until get() is invoked")
    void supplierNotCalledEagerly() {
        AtomicInteger callCount = new AtomicInteger(0);
        Lazy<String> lazy = Lazy.of(() -> {
            callCount.incrementAndGet();
            return "hello";
        });
        assertEquals(0, callCount.get(), "Supplier should not be called on construction");
    }

    @Test
    @DisplayName("get() calls the supplier exactly once")
    void supplierCalledOnce() {
        AtomicInteger callCount = new AtomicInteger(0);
        Lazy<String> lazy = Lazy.of(() -> {
            callCount.incrementAndGet();
            return "hello";
        });
        lazy.get();
        lazy.get();
        lazy.get();
        assertEquals(1, callCount.get(), "Supplier should be called exactly once");
    }

    @Test
    @DisplayName("get() returns the same value on every call")
    void alwaysReturnsSameValue() {
        Lazy<String> lazy = Lazy.of(() -> "constant");
        assertSame(lazy.get(), lazy.get());
    }

    @Test
    @DisplayName("isComputed() is false before and true after get()")
    void isComputedTracksState() {
        Lazy<Integer> lazy = Lazy.of(() -> 42);
        assertFalse(lazy.isComputed());
        lazy.get();
        assertTrue(lazy.isComputed());
    }

    @Test
    @DisplayName("AppConfig does not load any section at construction")
    void appConfigNothingLoadedAtConstruction() {
        AppConfig config = new AppConfig();
        assertFalse(config.isDatabaseUrlLoaded());
        assertFalse(config.isApiKeyLoaded());
        assertFalse(config.isFeatureFlagsLoaded());
    }

    @Test
    @DisplayName("Accessing DB URL marks only that section as loaded")
    void accessingDbUrlLoadsOnlyDb() {
        AppConfig config = new AppConfig();
        config.getDatabaseUrl();
        assertTrue(config.isDatabaseUrlLoaded());
        assertFalse(config.isApiKeyLoaded());
        assertFalse(config.isFeatureFlagsLoaded());
    }
}

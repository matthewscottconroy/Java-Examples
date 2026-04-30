package com.patterns.proxy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Proxy pattern — Caching Database Proxy.
 */
class ProxyTest {

    private ProductRepository makeProxy() {
        return new CachingProductProxy(new DatabaseProductRepository());
    }

    @Test
    @DisplayName("findBySku returns the correct product for a known SKU")
    void findKnownSku() {
        Optional<String> result = makeProxy().findBySku("SKU-001");
        assertTrue(result.isPresent());
        assertTrue(result.get().contains("Keyboard"));
    }

    @Test
    @DisplayName("findBySku returns empty for an unknown SKU")
    void findUnknownSku() {
        assertTrue(makeProxy().findBySku("UNKNOWN").isEmpty());
    }

    @Test
    @DisplayName("Second call for same SKU is served from cache (hit count increases)")
    void cachingReducesDbCalls() {
        CachingProductProxy proxy = new CachingProductProxy(new DatabaseProductRepository());

        proxy.findBySku("SKU-002");
        proxy.findBySku("SKU-002"); // should be a cache hit

        assertEquals(1, proxy.getCacheHits());
        assertEquals(1, proxy.getCacheMisses());
    }

    @Test
    @DisplayName("count() is cached after the first call")
    void countIsCached() {
        CachingProductProxy proxy = new CachingProductProxy(new DatabaseProductRepository());

        int first  = proxy.count();
        int second = proxy.count();

        assertEquals(first, second);
        assertEquals(1, proxy.getCacheHits()); // second count was a hit
    }

    @Test
    @DisplayName("Client code uses only the ProductRepository interface")
    void clientUsesInterface() {
        // This test proves the proxy is transparent — the variable is typed as ProductRepository
        ProductRepository repo = new CachingProductProxy(new DatabaseProductRepository());
        assertDoesNotThrow(() -> repo.findBySku("SKU-003"));
    }
}

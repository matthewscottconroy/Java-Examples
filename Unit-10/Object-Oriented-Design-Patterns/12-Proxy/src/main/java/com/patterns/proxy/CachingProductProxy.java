package com.patterns.proxy;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Proxy — intercepts calls to the real repository and serves cached results
 * for repeated queries.
 *
 * <p>The first call for any SKU hits the real database and stores the result.
 * All subsequent calls for the same SKU are returned from the cache instantly,
 * with no database round-trip.
 *
 * <p>The proxy implements the same {@link ProductRepository} interface as the
 * real subject, so the client code is completely unaware of the caching layer.
 *
 * <p><b>Pattern roles:</b>
 * <pre>
 *   ProductRepository          — Subject
 *   DatabaseProductRepository  — Real Subject
 *   CachingProductProxy        — Proxy
 * </pre>
 */
public class CachingProductProxy implements ProductRepository {

    private final ProductRepository      real;
    private final Map<String, Optional<String>> cache = new HashMap<>();
    private Integer cachedCount = null;

    private int cacheHits   = 0;
    private int cacheMisses = 0;

    /**
     * @param real the real repository to delegate cache misses to
     */
    public CachingProductProxy(ProductRepository real) {
        this.real = real;
    }

    @Override
    public Optional<String> findBySku(String sku) {
        if (cache.containsKey(sku)) {
            cacheHits++;
            System.out.println("  [Cache HIT]  SKU=" + sku);
            return cache.get(sku);
        }
        cacheMisses++;
        System.out.println("  [Cache MISS] SKU=" + sku + " — forwarding to database");
        Optional<String> result = real.findBySku(sku);
        cache.put(sku, result);
        return result;
    }

    @Override
    public int count() {
        if (cachedCount != null) {
            cacheHits++;
            System.out.println("  [Cache HIT]  count");
            return cachedCount;
        }
        cacheMisses++;
        System.out.println("  [Cache MISS] count — forwarding to database");
        cachedCount = real.count();
        return cachedCount;
    }

    /** @return number of requests served from cache */
    public int getCacheHits()   { return cacheHits; }

    /** @return number of requests forwarded to the real database */
    public int getCacheMisses() { return cacheMisses; }
}

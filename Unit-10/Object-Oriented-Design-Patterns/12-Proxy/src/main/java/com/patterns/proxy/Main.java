package com.patterns.proxy;

/**
 * Demonstrates the Proxy pattern with a caching database proxy.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Caching Database Proxy (Proxy Pattern) ===\n");

        ProductRepository real  = new DatabaseProductRepository();
        CachingProductProxy proxy = new CachingProductProxy(real);

        // Client works with the proxy exactly as it would with the real repository
        System.out.println("--- First lookups (cache cold) ---");
        System.out.println("Found: " + proxy.findBySku("SKU-001").orElse("not found"));
        System.out.println("Found: " + proxy.findBySku("SKU-002").orElse("not found"));
        System.out.println("Count: " + proxy.count());

        System.out.println("\n--- Repeated lookups (cache warm) ---");
        System.out.println("Found: " + proxy.findBySku("SKU-001").orElse("not found"));
        System.out.println("Found: " + proxy.findBySku("SKU-001").orElse("not found"));
        System.out.println("Count: " + proxy.count());

        System.out.printf("%nCache hits: %d | Cache misses: %d%n",
                proxy.getCacheHits(), proxy.getCacheMisses());
    }
}

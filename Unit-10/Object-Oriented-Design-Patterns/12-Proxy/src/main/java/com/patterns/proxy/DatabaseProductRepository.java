package com.patterns.proxy;

import java.util.Map;
import java.util.Optional;

/**
 * Real Subject — the actual database-backed product repository.
 *
 * <p>Each call simulates a slow database round-trip. In production this would
 * execute a SQL query; here we sleep briefly to represent I/O latency.
 */
public class DatabaseProductRepository implements ProductRepository {

    private final Map<String, String> db = Map.of(
            "SKU-001", "Wireless Keyboard — $49.99",
            "SKU-002", "USB-C Hub 7-port — $39.99",
            "SKU-003", "Mechanical Mouse — $59.99"
    );

    @Override
    public Optional<String> findBySku(String sku) {
        simulateLatency();
        System.out.println("  [DB] Executing query: SELECT * FROM products WHERE sku='" + sku + "'");
        return Optional.ofNullable(db.get(sku));
    }

    @Override
    public int count() {
        simulateLatency();
        System.out.println("  [DB] Executing query: SELECT COUNT(*) FROM products");
        return db.size();
    }

    private void simulateLatency() {
        try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}

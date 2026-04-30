package com.functional.collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class CollectorsTest {

    private List<Order> orders;

    @BeforeEach
    void setUp() {
        orders = List.of(
            new Order(1, "Alice", "North", "Laptop", "Electronics", 1000.0, true),
            new Order(2, "Bob",   "South", "Chair",  "Furniture",    400.0, false),
            new Order(3, "Alice", "North", "Mouse",  "Electronics",   30.0, true),
            new Order(4, "Bob",   "North", "Desk",   "Furniture",    700.0, true)
        );
    }

    @Test
    @DisplayName("groupingBy produces one entry per distinct key")
    void groupingByCategory() {
        Map<String, List<Order>> result = orders.stream()
                .collect(Collectors.groupingBy(Order::category));
        assertTrue(result.containsKey("Electronics"));
        assertTrue(result.containsKey("Furniture"));
        assertEquals(2, result.get("Electronics").size());
    }

    @Test
    @DisplayName("groupingBy with summingDouble totals per group")
    void groupingBySumming() {
        Map<String, Double> totals = orders.stream()
                .collect(Collectors.groupingBy(Order::region,
                         Collectors.summingDouble(Order::total)));
        assertEquals(1730.0, totals.get("North"), 0.01); // 1000 + 30 + 700
        assertEquals(400.0,  totals.get("South"), 0.01);
    }

    @Test
    @DisplayName("partitioningBy splits into true/false groups")
    void partitioningByShipped() {
        Map<Boolean, List<Order>> result = orders.stream()
                .collect(Collectors.partitioningBy(Order::shipped));
        assertEquals(3, result.get(true).size());
        assertEquals(1, result.get(false).size());
    }

    @Test
    @DisplayName("counting downstream collector counts per group")
    void countingPerCustomer() {
        Map<String, Long> counts = orders.stream()
                .collect(Collectors.groupingBy(Order::customer, Collectors.counting()));
        assertEquals(2L, counts.get("Alice"));
        assertEquals(2L, counts.get("Bob"));
    }

    @Test
    @DisplayName("joining concatenates strings with delimiter")
    void joining() {
        String result = orders.stream()
                .map(Order::customer)
                .distinct()
                .sorted()
                .collect(Collectors.joining(", "));
        assertEquals("Alice, Bob", result);
    }

    @Test
    @DisplayName("summarizingDouble computes min, max, average in one pass")
    void summarizingDouble() {
        DoubleSummaryStatistics stats = orders.stream()
                .collect(Collectors.summarizingDouble(Order::total));
        assertEquals(4, stats.getCount());
        assertEquals(30.0,  stats.getMin(),     0.01);
        assertEquals(1000.0, stats.getMax(),    0.01);
        assertEquals(532.5, stats.getAverage(), 0.01); // (1000+400+30+700)/4
    }
}

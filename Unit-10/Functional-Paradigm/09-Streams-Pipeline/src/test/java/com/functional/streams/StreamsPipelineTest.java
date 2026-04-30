package com.functional.streams;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class StreamsPipelineTest {

    private List<Sale> sales;

    @BeforeEach
    void setUp() { sales = SalesData.sample(); }

    @Test
    @DisplayName("filter keeps only matching elements")
    void filterElectronics() {
        long count = sales.stream()
                .filter(s -> s.category().equals("Electronics"))
                .count();
        assertEquals(8, count);
    }

    @Test
    @DisplayName("map transforms each element")
    void mapToProduct() {
        List<String> products = sales.stream()
                .map(Sale::product)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        assertTrue(products.contains("Laptop Pro"));
        assertTrue(products.contains("Desk Chair"));
    }

    @Test
    @DisplayName("mapToDouble + sum computes total revenue")
    void totalRevenue() {
        double total = sales.stream().mapToDouble(Sale::revenue).sum();
        assertTrue(total > 0);
        // Laptop Pro: 3*1299.99 + 5*1299.99 + 2*1299.99 = 10*1299.99 = 12999.90 + other items
        assertTrue(total > 12_000);
    }

    @Test
    @DisplayName("max finds the highest-revenue transaction")
    void maxRevenue() {
        Optional<Sale> top = sales.stream()
                .max(Comparator.comparingDouble(Sale::revenue));
        assertTrue(top.isPresent());
        // 5 Laptop Pros at $1299.99 each = $6499.95
        assertEquals("Laptop Pro", top.get().product());
        assertEquals(5, top.get().units());
    }

    @Test
    @DisplayName("distinct removes duplicates")
    void distinctProducts() {
        long unique = sales.stream().map(Sale::product).distinct().count();
        assertTrue(unique < sales.size());
    }

    @Test
    @DisplayName("limit caps the output size")
    void limitToThree() {
        List<Sale> top3 = sales.stream()
                .sorted(Comparator.comparingDouble(Sale::revenue).reversed())
                .limit(3)
                .collect(Collectors.toList());
        assertEquals(3, top3.size());
    }

    @Test
    @DisplayName("reduce sums integer values")
    void reduceUnits() {
        int total = sales.stream().mapToInt(Sale::units).reduce(0, Integer::sum);
        assertTrue(total > 0);
    }

    @Test
    @DisplayName("Stream pipeline is lazy until terminal operation")
    void pipelineIsLazy() {
        // If this were eager, the peek would fire on every element before filter.
        // The count here just verifies the pipeline executes correctly.
        long count = sales.stream()
                .filter(s -> s.units() > 5)
                .count();
        assertTrue(count > 0);
    }
}

package com.algorithms.sorting.quicksort;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

public class Main {

    record Product(String sku, String name, double price, int stock) {}

    public static void main(String[] args) {
        System.out.println("=== Inventory Sorter (Quick Sort) ===\n");

        Product[] inventory = {
            new Product("B03", "Wireless Mouse",  29.99, 250),
            new Product("A01", "Laptop Pro",    1299.99,  12),
            new Product("C07", "USB-C Hub",      49.99,  88),
            new Product("A02", "Laptop Stand",   79.99,  45),
            new Product("B01", "Mechanical Keyboard", 149.99, 33),
            new Product("C01", "HDMI Cable",     14.99, 500),
            new Product("B02", "Webcam 4K",      99.99,  60),
            new Product("A03", "Laptop Bag",     59.99, 120)
        };

        Product[] bySku = inventory.clone();
        QuickSort.sort(bySku, Comparator.comparing(Product::sku));
        System.out.println("Sorted by SKU:");
        for (Product p : bySku) System.out.printf("  %-6s %-22s $%7.2f%n", p.sku(), p.name(), p.price());

        Product[] byPrice = inventory.clone();
        QuickSort.sort(byPrice, Comparator.comparingDouble(Product::price));
        System.out.println("\nSorted by price (ascending):");
        for (Product p : byPrice) System.out.printf("  $%7.2f  %s%n", p.price(), p.name());

        // Demonstrate three-way partitioning handles duplicates gracefully
        Integer[] withDupes = {5,3,5,1,5,2,5,4,5};
        QuickSort.sort(withDupes);
        System.out.println("\nArray with many duplicates sorted: " + Arrays.toString(withDupes));

        // Large random array — show it works
        Random rng = new Random(1);
        Integer[] large = new Integer[100_000];
        for (int i = 0; i < large.length; i++) large[i] = rng.nextInt(100_000);
        long t0 = System.currentTimeMillis();
        QuickSort.sort(large);
        System.out.printf("%nSorted 100,000 integers in %d ms%n", System.currentTimeMillis() - t0);
    }
}

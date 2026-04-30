package com.algorithms.search.binary;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Demonstrates binary search variants on a software release history.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Binary Search — Version Finder ===\n");

        // Build a release history: v1.0.0 through v1.0.15, broken starting at v1.0.9
        List<Release> releases = new ArrayList<>();
        for (int patch = 0; patch <= 15; patch++) {
            releases.add(new Release(1, 0, patch, patch >= 9));
        }

        System.out.println("Release history:");
        releases.forEach(r -> System.out.println("  " + r));

        Release firstBad = VersionFinder.firstBroken(releases);
        System.out.println("\nFirst broken release: " + firstBad);

        // --- Basic search ---
        System.out.println("\n--- Basic search ---");
        List<String> names = List.of(
            "alice", "bob", "carol", "dave", "eve", "frank", "grace", "henry"
        );
        Comparator<String> cmp = Comparator.naturalOrder();
        System.out.println("Sorted names: " + names);
        System.out.println("search('carol') → index " + BinarySearch.search(names, "carol", cmp));
        System.out.println("search('zara')  → index " + BinarySearch.search(names, "zara",  cmp));

        // --- First/last with duplicates ---
        System.out.println("\n--- First/last with duplicates ---");
        List<Integer> dupes = List.of(1, 2, 2, 2, 3, 3, 4, 5, 5);
        Comparator<Integer> icmp = Integer::compare;
        System.out.println("List: " + dupes);
        System.out.println("searchFirst(2) → index " + BinarySearch.searchFirst(dupes, 2, icmp));
        System.out.println("searchLast(2)  → index " + BinarySearch.searchLast(dupes,  2, icmp));
        System.out.println("searchFirst(3) → index " + BinarySearch.searchFirst(dupes, 3, icmp));
        System.out.println("searchLast(3)  → index " + BinarySearch.searchLast(dupes,  3, icmp));

        // --- Upper/lower bounds ---
        System.out.println("\n--- Bounds (how many elements ≤ target?) ---");
        System.out.println("lowerBound(2) → " + BinarySearch.lowerBound(dupes, 2, icmp) + " (first index ≥ 2)");
        System.out.println("upperBound(2) → " + BinarySearch.upperBound(dupes, 2, icmp) + " (first index > 2)");
        System.out.println("Count of 2s   → " +
            (BinarySearch.upperBound(dupes, 2, icmp) - BinarySearch.lowerBound(dupes, 2, icmp)));

        // --- Performance ---
        System.out.println("\n--- Performance on 10M element list ---");
        int n = 10_000_000;
        List<Integer> big = new ArrayList<>(n);
        for (int i = 0; i < n; i++) big.add(i * 2);  // even numbers 0..19999998
        long start = System.currentTimeMillis();
        int idx = BinarySearch.search(big, 9_999_998, icmp);
        long ms = System.currentTimeMillis() - start;
        System.out.printf("search(9_999_998) in 10M elements → index %d  (%d ms, ~%d comparisons max)%n",
            idx, ms, (int) Math.ceil(Math.log(n) / Math.log(2)));
    }
}

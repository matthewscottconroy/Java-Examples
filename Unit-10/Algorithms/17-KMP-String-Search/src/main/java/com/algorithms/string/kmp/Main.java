package com.algorithms.string.kmp;

import java.util.Arrays;
import java.util.List;

/**
 * Demonstrates KMP on a log file pattern-search scenario.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== KMP String Search — Log Pattern Finder ===\n");

        // Simulate a log file
        String log = "2024-01-15 ERROR database connection refused\n"
                   + "2024-01-15 INFO  request processed successfully\n"
                   + "2024-01-15 ERROR null pointer exception in UserService\n"
                   + "2024-01-15 WARN  response time exceeded threshold\n"
                   + "2024-01-15 ERROR database connection refused\n"
                   + "2024-01-15 INFO  cache hit ratio: 0.87\n"
                   + "2024-01-15 ERROR timeout waiting for lock\n";

        System.out.println("Log file:");
        System.out.println(log);

        // Search for ERROR entries
        String pattern = "ERROR";
        List<Integer> errorPositions = KMP.search(log, pattern);
        System.out.println("\"" + pattern + "\" found at positions: " + errorPositions);
        System.out.println("Total ERROR entries: " + errorPositions.size());

        // Search for specific error message
        System.out.println();
        String specific = "database connection refused";
        List<Integer> dbErrors = KMP.search(log, specific);
        System.out.println("\"" + specific + "\" found at positions: " + dbErrors);
        System.out.println("Occurrences: " + dbErrors.size());

        // The failure function
        System.out.println("\n--- Failure Function (Prefix Table) ---");
        String[] patterns = {"AABAAB", "ABCABD", "AAAAAA", "ABCDEF"};
        for (String p : patterns) {
            System.out.printf("  %-10s  failure: %s%n", p,
                Arrays.toString(KMP.buildFailureFunction(p)));
        }

        // Performance comparison setup
        System.out.println("\n--- Performance: KMP vs naive on worst case ---");
        // Worst case for naïve: "AAAA...AAAB" searching for "AAAB"
        int n = 100_000;
        String worstText    = "A".repeat(n) + "B";
        String worstPattern = "A".repeat(100) + "B";

        long start = System.nanoTime();
        List<Integer> kmpResult = KMP.search(worstText, worstPattern);
        long kmpMs = (System.nanoTime() - start) / 1_000_000;

        start = System.nanoTime();
        // Naive: built-in indexOf only finds first, simulate all-matches
        int count = 0;
        int idx = 0;
        while ((idx = worstText.indexOf(worstPattern, idx)) != -1) { count++; idx++; }
        long naiveMs = (System.nanoTime() - start) / 1_000_000;

        System.out.printf("  Text: %d 'A's + 'B', Pattern: 100 'A's + 'B'%n", n);
        System.out.printf("  KMP:   %d ms, found %d match(es)%n", kmpMs, kmpResult.size());
        System.out.printf("  Naive: %d ms, found %d match(es)%n", naiveMs, count);
    }
}

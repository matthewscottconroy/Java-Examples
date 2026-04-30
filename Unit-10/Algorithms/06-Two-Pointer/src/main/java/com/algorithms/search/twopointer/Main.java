package com.algorithms.search.twopointer;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Demonstrates two-pointer and sliding window techniques on real-world scenarios.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Two-Pointer and Sliding Window ===\n");

        // --- Two-sum: find two API response times that add up to a target latency ---
        System.out.println("--- Two-Sum: Matching API Latencies ---");
        int[] latencies = {12, 25, 38, 47, 63, 81, 95, 102, 117, 134};
        int target = 145;
        System.out.println("Sorted latencies (ms): " + Arrays.toString(latencies));
        System.out.println("Find two that sum to " + target + " ms:");
        Optional<int[]> pair = TwoPointer.twoSum(latencies, target);
        pair.ifPresentOrElse(
            p -> System.out.printf("  → latencies[%d]=%d + latencies[%d]=%d = %d%n",
                p[0], latencies[p[0]], p[1], latencies[p[1]], target),
            () -> System.out.println("  → No pair found")
        );

        // --- Sliding window: longest burst of network traffic within bandwidth cap ---
        System.out.println("\n--- Sliding Window: Bandwidth Monitor ---");
        int[] traffic = {15, 40, 30, 25, 10, 60, 20, 35, 45, 5};  // Mbps per second
        int cap = 100;
        System.out.println("Traffic (Mbps/s): " + Arrays.toString(traffic));
        System.out.println("Bandwidth cap: " + cap + " Mbps");
        int longest = TwoPointer.longestSubarrayWithSumAtMost(traffic, cap);
        System.out.println("Longest burst window ≤ " + cap + " Mbps total: " + longest + " seconds");

        // --- Three-sum: find sensor readings that cancel out ---
        System.out.println("\n--- Three-Sum: Sensor Calibration ---");
        int[] readings = {-5, -3, -1, 0, 1, 2, 3, 4, 5};
        System.out.println("Calibration offsets: " + Arrays.toString(readings));
        System.out.println("Triplets that sum to zero (balanced calibration):");
        List<int[]> triplets = TwoPointer.threeSum(readings);
        triplets.forEach(t -> System.out.printf("  [%d, %d, %d]%n", t[0], t[1], t[2]));

        // --- Min subarray: minimum delivery window to cover demand ---
        System.out.println("\n--- Minimum Subarray: Delivery Planning ---");
        int[] supply = {3, 1, 4, 7, 2, 8, 5, 6};
        int demand = 20;
        System.out.println("Daily supply units: " + Arrays.toString(supply));
        System.out.println("Required total: " + demand);
        int minDays = TwoPointer.minSubarrayLength(supply, demand);
        System.out.println("Minimum consecutive days to meet demand: " + minDays);
    }
}

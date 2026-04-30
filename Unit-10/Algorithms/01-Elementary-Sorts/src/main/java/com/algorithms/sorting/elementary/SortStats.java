package com.algorithms.sorting.elementary;

/** Tracks comparisons and swaps performed by a sorting algorithm. */
public class SortStats {
    private long comparisons;
    private long swaps;
    private final String algorithmName;

    public SortStats(String algorithmName) { this.algorithmName = algorithmName; }

    public void comparison() { comparisons++; }
    public void swap()       { swaps++; }

    public long comparisons() { return comparisons; }
    public long swaps()       { return swaps; }

    @Override
    public String toString() {
        return String.format("%-18s comparisons=%,6d  swaps=%,6d",
                algorithmName, comparisons, swaps);
    }
}

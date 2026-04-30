package com.algorithms.sorting.elementary;

import java.util.Arrays;
import java.util.Random;

/**
 * Demonstrates the three elementary sorts on a library book catalogue,
 * showing operation counts for random, sorted, and reverse-sorted inputs.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Library Book Sorter (Elementary Sorts) ===\n");

        String[] titles = {
            "Zen and the Art of Motorcycle Maintenance",
            "A Brief History of Time",
            "One Hundred Years of Solitude",
            "The Great Gatsby",
            "Moby-Dick",
            "War and Peace",
            "Crime and Punishment",
            "To Kill a Mockingbird",
            "Brave New World",
            "Don Quixote"
        };

        System.out.println("Sorting 10 book titles — operation counts:\n");
        runAll(titles.clone(), "random input");

        Arrays.sort(titles);
        runAll(titles.clone(), "already sorted");

        String[] reversed = titles.clone();
        for (int i = 0, j = reversed.length - 1; i < j; i++, j--) {
            String t = reversed[i]; reversed[i] = reversed[j]; reversed[j] = t;
        }
        runAll(reversed.clone(), "reverse sorted");

        // Show a larger comparison
        System.out.println("\nScaling to n=1000 random integers:\n");
        Random rng = new Random(42);
        Integer[] big = new Integer[1000];
        for (int i = 0; i < big.length; i++) big[i] = rng.nextInt(10_000);

        System.out.println(ElementarySorts.bubbleSort(big.clone()));
        System.out.println(ElementarySorts.selectionSort(big.clone()));
        System.out.println(ElementarySorts.insertionSort(big.clone()));
    }

    private static void runAll(String[] base, String label) {
        System.out.println("  " + label + ":");
        System.out.println("  " + ElementarySorts.bubbleSort(base.clone()));
        System.out.println("  " + ElementarySorts.selectionSort(base.clone()));
        System.out.println("  " + ElementarySorts.insertionSort(base.clone()));
        System.out.println();
    }
}

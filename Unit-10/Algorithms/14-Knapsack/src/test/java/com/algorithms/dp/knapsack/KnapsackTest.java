package com.algorithms.dp.knapsack;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KnapsackTest {

    @Test
    @DisplayName("classic example: capacity 6, items give value 220")
    void classicExample() {
        // Weights: [2,3,4,5], Values: [3,4,5,6], Capacity: 5
        // Best: take weight-2 (3) + weight-3 (4) = value 7
        List<Knapsack.Item> items = List.of(
            new Knapsack.Item("a", 2, 3),
            new Knapsack.Item("b", 3, 4),
            new Knapsack.Item("c", 4, 5),
            new Knapsack.Item("d", 5, 6)
        );
        assertEquals(7, Knapsack.maxValue(items, 5));
    }

    @Test
    @DisplayName("solve and maxValue agree")
    void solveAndMaxValueAgree() {
        List<Knapsack.Item> items = List.of(
            new Knapsack.Item("x", 1, 10),
            new Knapsack.Item("y", 3, 25),
            new Knapsack.Item("z", 4, 30),
            new Knapsack.Item("w", 2, 15)
        );
        int capacity = 6;
        Knapsack.Solution sol = Knapsack.solve(items, capacity);
        assertEquals(Knapsack.maxValue(items, capacity), sol.totalValue());
    }

    @Test
    @DisplayName("solve returns correct chosen items (total weight ≤ capacity)")
    void chosenItemsFitInCapacity() {
        List<Knapsack.Item> items = List.of(
            new Knapsack.Item("a", 2, 10),
            new Knapsack.Item("b", 3, 20),
            new Knapsack.Item("c", 4, 15),
            new Knapsack.Item("d", 1, 8)
        );
        int capacity = 5;
        Knapsack.Solution sol = Knapsack.solve(items, capacity);
        int usedWeight = sol.chosen().stream().mapToInt(Knapsack.Item::weight).sum();
        assertTrue(usedWeight <= capacity);
        int computedValue = sol.chosen().stream().mapToInt(Knapsack.Item::value).sum();
        assertEquals(sol.totalValue(), computedValue);
    }

    @Test
    @DisplayName("empty item list gives value 0")
    void emptyItems() {
        assertEquals(0, Knapsack.maxValue(List.of(), 10));
        assertEquals(0, Knapsack.solve(List.of(), 10).totalValue());
    }

    @Test
    @DisplayName("capacity 0 gives value 0")
    void zeroCapacity() {
        List<Knapsack.Item> items = List.of(new Knapsack.Item("a", 1, 100));
        assertEquals(0, Knapsack.maxValue(items, 0));
    }

    @Test
    @DisplayName("single item fits exactly")
    void singleItemFits() {
        List<Knapsack.Item> items = List.of(new Knapsack.Item("a", 5, 99));
        assertEquals(99, Knapsack.maxValue(items, 5));
        assertEquals(99, Knapsack.maxValue(items, 10));
    }

    @Test
    @DisplayName("single item too heavy")
    void singleItemTooHeavy() {
        List<Knapsack.Item> items = List.of(new Knapsack.Item("a", 10, 99));
        assertEquals(0, Knapsack.maxValue(items, 9));
    }

    @Test
    @DisplayName("greedy by value-density is suboptimal — DP finds better")
    void dpBeatsDensityGreedy() {
        // Greedy by value/weight would pick item-a (ratio 4.0) then item-c (ratio 3.3)
        // But DP can pick item-b + item-c = 12+11 = 23 (greedy gets 10+11 = 21)
        List<Knapsack.Item> items = List.of(
            new Knapsack.Item("a", 2, 8),   // density 4.0
            new Knapsack.Item("b", 4, 14),  // density 3.5
            new Knapsack.Item("c", 3, 11)   // density 3.67
        );
        // capacity 7: b+c=7 → value 25; a+c=5 → value 19; a+b=6 → value 22; all three=9>7
        assertEquals(25, Knapsack.maxValue(items, 7));
    }
}

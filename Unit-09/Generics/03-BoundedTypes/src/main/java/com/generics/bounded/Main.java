package com.generics.bounded;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        // ----------------------------------------------------------------
        // PART 1: Upper bound — <T extends Number>
        // ----------------------------------------------------------------
        System.out.println("=== NumberBox<T extends Number> ===");

        NumberBox<Integer> intBox    = new NumberBox<>(7);
        NumberBox<Double>  doubleBox = new NumberBox<>(3.5);
        NumberBox<Long>    longBox   = new NumberBox<>(1_000_000L);

        System.out.println(intBox    + "  doubled = " + intBox.doubled());
        System.out.println(doubleBox + "  doubled = " + doubleBox.doubled());
        System.out.println(longBox   + "  doubled = " + longBox.doubled());
        System.out.println("intBox > doubleBox? " + intBox.isGreaterThan(doubleBox));

        // The bound rejects non-Number types at compile time:
        //   NumberBox<String> bad = new NumberBox<>("text");  // compile error

        // ----------------------------------------------------------------
        // PART 2: Recursive bound — <T extends Comparable<T>>
        // ----------------------------------------------------------------
        System.out.println("\n=== Algorithms<T extends Comparable<T>> ===");

        List<Integer> ints    = List.of(4, 1, 8, 2, 7, 3);
        List<String>  strings = List.of("mango", "apple", "fig", "cherry");
        List<Double>  doubles = List.of(2.7, 1.4, 3.1, 0.6);

        System.out.println("Integers → min=" + Algorithms.min(ints)    + "  max=" + Algorithms.max(ints));
        System.out.println("Strings  → min=" + Algorithms.min(strings) + "  max=" + Algorithms.max(strings));
        System.out.println("Doubles  → min=" + Algorithms.min(doubles) + "  max=" + Algorithms.max(doubles));

        // Binary search (list must be sorted first)
        List<Integer> sorted = List.of(1, 3, 5, 7, 9, 11);
        int idx = Algorithms.binarySearch(sorted, 7);
        System.out.println("binarySearch(sorted, 7) → index " + idx);
        int missing = Algorithms.binarySearch(sorted, 6);
        System.out.println("binarySearch(sorted, 6) → " + missing + " (negative = not found)");

        // ----------------------------------------------------------------
        // PART 3: Multiple bounds — <T extends Number & Comparable<T>>
        // ----------------------------------------------------------------
        System.out.println("\n=== MultiBounded<T extends Number & Comparable<T>> ===");

        // Integer satisfies both Number and Comparable<Integer>
        System.out.println("clamp(15,  0, 10)    = " + MultiBounded.clamp(15, 0, 10));
        System.out.println("clamp(5,   0, 10)    = " + MultiBounded.clamp(5, 0, 10));
        System.out.println("clamp(-3,  0, 10)    = " + MultiBounded.clamp(-3, 0, 10));

        // Double satisfies both bounds too
        System.out.println("clamp(3.7, 0.0, 3.5) = " + MultiBounded.clamp(3.7, 0.0, 3.5));
        System.out.println("sum(3, 4)             = " + MultiBounded.sum(3, 4));

        // Bound order matters: class must come first.
        // <T extends Comparable<T> & Number> would be a compile error if Number were a class
        // that appeared after an interface. (In practice Number IS an abstract class.)
    }
}

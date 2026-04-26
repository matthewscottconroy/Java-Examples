package com.streams.terminal;

import java.util.*;
import java.util.stream.*;

/**
 * Terminal operations — they trigger execution and produce a result or side effect.
 *
 * <p>A stream can have at most ONE terminal operation; after it fires, the stream
 * is consumed and cannot be reused.
 *
 * <p>Short-circuiting terminals (findFirst, findAny, anyMatch, allMatch, noneMatch,
 * limit) can stop the pipeline before all elements are processed — important for
 * infinite streams and performance.
 */
public class Main {

    record Product(String name, String category, double price) {}

    static final List<Product> PRODUCTS = List.of(
        new Product("Laptop",   "Electronics", 999.99),
        new Product("Phone",    "Electronics", 699.00),
        new Product("Desk",     "Furniture",   349.50),
        new Product("Chair",    "Furniture",   199.00),
        new Product("Monitor",  "Electronics", 449.99),
        new Product("Lamp",     "Furniture",    59.99)
    );

    public static void main(String[] args) {
        forEachAndCount();
        minMaxAndSum();
        reduceExplained();
        matchingAndFinding();
        collectToList();
    }

    static void forEachAndCount() {
        System.out.println("=== forEach and count ===");

        // forEach — consume every element with a side effect.
        PRODUCTS.stream()
                .filter(p -> p.price() > 400)
                .forEach(p -> System.out.println("  " + p.name() + " $" + p.price()));

        long electronicsCount = PRODUCTS.stream()
            .filter(p -> p.category().equals("Electronics"))
            .count();
        System.out.println("  electronics count: " + electronicsCount);
    }

    static void minMaxAndSum() {
        System.out.println("\n=== min, max, sum, average ===");

        // min/max return Optional — the stream might be empty.
        Optional<Product> cheapest = PRODUCTS.stream()
            .min(Comparator.comparingDouble(Product::price));
        cheapest.ifPresent(p -> System.out.println("  cheapest: " + p.name() + " $" + p.price()));

        Optional<Product> priciest = PRODUCTS.stream()
            .max(Comparator.comparingDouble(Product::price));
        priciest.ifPresent(p -> System.out.println("  priciest: " + p.name() + " $" + p.price()));

        // mapToDouble + sum/average/summaryStatistics.
        double total = PRODUCTS.stream().mapToDouble(Product::price).sum();
        System.out.printf("  total:    $%.2f%n", total);

        OptionalDouble avg = PRODUCTS.stream().mapToDouble(Product::price).average();
        avg.ifPresent(a -> System.out.printf("  average:  $%.2f%n", a));

        DoubleSummaryStatistics stats = PRODUCTS.stream()
            .mapToDouble(Product::price)
            .summaryStatistics();
        System.out.printf("  stats: count=%d, min=%.2f, max=%.2f, sum=%.2f%n",
            stats.getCount(), stats.getMin(), stats.getMax(), stats.getSum());
    }

    static void reduceExplained() {
        System.out.println("\n=== reduce ===");

        // reduce(identity, accumulator) — starts from identity, folds left.
        // For addition, identity = 0.
        int sum = IntStream.rangeClosed(1, 10).reduce(0, Integer::sum);
        System.out.println("  sum 1..10 = " + sum);

        // Factorial via reduce — identity = 1L.
        long factorial = LongStream.rangeClosed(1, 10).reduce(1L, (a, b) -> a * b);
        System.out.println("  10! = " + factorial);

        // reduce(BinaryOperator) — no identity, returns Optional (empty stream case).
        Optional<String> longest = Stream.of("cat", "elephant", "dog", "rhinoceros")
            .reduce((a, b) -> a.length() >= b.length() ? a : b);
        System.out.println("  longest word: " + longest.orElse("none"));

        // reduce with combiner — required for parallel reduce over non-associative ops.
        // combiner merges partial results from parallel sub-streams.
        int sumParallel = Stream.of(1, 2, 3, 4, 5)
            .parallel()
            .reduce(0,
                (acc, n) -> acc + n,   // accumulator
                Integer::sum);         // combiner — merges sub-results
        System.out.println("  parallel sum: " + sumParallel);

        // Reduce to build a string (collector is better, but reduce illustrates folding).
        String joined = Stream.of("a", "b", "c", "d")
            .reduce("", (acc, s) -> acc.isEmpty() ? s : acc + "-" + s);
        System.out.println("  joined: " + joined);
    }

    static void matchingAndFinding() {
        System.out.println("\n=== anyMatch, allMatch, noneMatch, findFirst, findAny ===");

        // Short-circuit as soon as the answer is known.
        boolean anyExpensive = PRODUCTS.stream().anyMatch(p -> p.price() > 900);
        System.out.println("  any over $900:     " + anyExpensive);

        boolean allPositive = PRODUCTS.stream().allMatch(p -> p.price() > 0);
        System.out.println("  all price > 0:     " + allPositive);

        boolean noneNegative = PRODUCTS.stream().noneMatch(p -> p.price() < 0);
        System.out.println("  none negative:     " + noneNegative);

        // findFirst — deterministic; always returns the first in encounter order.
        Optional<Product> firstElec = PRODUCTS.stream()
            .filter(p -> p.category().equals("Electronics"))
            .findFirst();
        System.out.println("  first electronic:  " + firstElec.map(Product::name).orElse("none"));

        // findAny — may return any match; in practice returns first in sequential mode.
        // Preferred for parallel streams where "any" is cheaper than "first".
        Optional<Product> anyFurniture = PRODUCTS.stream()
            .filter(p -> p.category().equals("Furniture"))
            .findAny();
        System.out.println("  any furniture:     " + anyFurniture.map(Product::name).orElse("none"));

        // Short-circuit on infinite stream — essential.
        OptionalInt firstOver100 = IntStream.iterate(1, n -> n + 1)
            .filter(n -> n * n > 100)
            .findFirst();
        System.out.println("  first n where n²>100: " + firstOver100.getAsInt());
    }

    static void collectToList() {
        System.out.println("\n=== collect (basic forms) ===");

        // toList() — Java 16+ shorthand; returns unmodifiable list.
        List<String> names = PRODUCTS.stream()
            .map(Product::name)
            .sorted()
            .toList();
        System.out.println("  names: " + names);

        // Collectors.toList() — returns a mutable list.
        List<Product> furniture = PRODUCTS.stream()
            .filter(p -> p.category().equals("Furniture"))
            .collect(Collectors.toList());
        furniture.add(new Product("Shelf", "Furniture", 89.99)); // mutable
        System.out.println("  furniture (with Shelf): " + furniture.stream().map(Product::name).toList());

        // Collectors.joining — concatenate strings.
        String csv = PRODUCTS.stream()
            .map(Product::name)
            .collect(Collectors.joining(", ", "[", "]"));
        System.out.println("  CSV: " + csv);

        // Collectors.toUnmodifiableList — explicit about immutability.
        List<String> immutable = PRODUCTS.stream()
            .map(Product::name)
            .collect(Collectors.toUnmodifiableList());
        System.out.println("  immutable count: " + immutable.size());
    }
}

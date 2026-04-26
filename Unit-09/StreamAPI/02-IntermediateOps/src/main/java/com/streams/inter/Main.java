package com.streams.inter;

import java.util.*;
import java.util.stream.*;

/**
 * Intermediate stream operations — all lazy, all return a new Stream.
 *
 * <p>Intermediate operations are not executed until a terminal operation is
 * called.  They form a pipeline; the JVM can fuse adjacent operations (e.g.,
 * filter + map) into a single pass over the data.
 *
 * <p>Stateless vs stateful:
 * <ul>
 *   <li>Stateless (filter, map, flatMap, peek) — each element is processed
 *       independently.  Safe for parallel streams.</li>
 *   <li>Stateful (distinct, sorted, limit, skip) — must see other elements
 *       before producing output.  Can force a sequential barrier in parallel
 *       streams.</li>
 * </ul>
 */
public class Main {

    record Person(String name, String city, int age) {}

    public static void main(String[] args) {
        filterAndMap();
        flatMap();
        distinctAndSorted();
        peekLimitSkip();
        takeWhileAndDropWhile();
        combinedPipeline();
    }

    static void filterAndMap() {
        System.out.println("=== filter and map ===");

        List<Integer> nums = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        List<Integer> evenSquares = nums.stream()
            .filter(n -> n % 2 == 0)          // keep even numbers
            .map(n -> n * n)                   // square them
            .toList();
        System.out.println("  even squares: " + evenSquares);

        // mapToInt — box-free path, enables sum/average/etc.
        List<String> words = List.of("apple", "banana", "cherry", "date");
        int totalLength = words.stream()
            .mapToInt(String::length)
            .sum();
        System.out.println("  total char count: " + totalLength);

        // mapToObj / boxed — back from primitive stream.
        IntStream.rangeClosed(1, 5)
                 .mapToObj(n -> n + " squared = " + (n * n))
                 .forEach(s -> System.out.println("  " + s));
    }

    static void flatMap() {
        System.out.println("\n=== flatMap ===");

        // flatMap replaces each element with a stream of elements, then
        // concatenates all those streams into one.
        List<List<Integer>> nested = List.of(
            List.of(1, 2, 3),
            List.of(4, 5),
            List.of(6, 7, 8, 9)
        );
        List<Integer> flat = nested.stream()
            .flatMap(Collection::stream)
            .toList();
        System.out.println("  flattened: " + flat);

        // Splitting sentences into words.
        List<String> sentences = List.of("the quick brown fox", "jumps over the lazy dog");
        List<String> uniqueWords = sentences.stream()
            .flatMap(s -> Arrays.stream(s.split(" ")))
            .distinct()
            .sorted()
            .toList();
        System.out.println("  unique words: " + uniqueWords);

        // flatMapToInt — flatten to a primitive stream.
        List<int[]> arrays = List.of(new int[]{1, 2}, new int[]{3, 4}, new int[]{5});
        int sum = Arrays.stream(arrays.toArray(new int[0][]))
            .flatMapToInt(Arrays::stream)
            .sum();
        System.out.println("  sum of nested arrays: " + sum);
    }

    static void distinctAndSorted() {
        System.out.println("\n=== distinct and sorted ===");

        List<Integer> withDups = List.of(3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5);
        List<Integer> unique = withDups.stream().distinct().toList();
        System.out.println("  distinct: " + unique);

        // sorted() — natural order.  Requires Comparable elements.
        List<Integer> sorted = withDups.stream().distinct().sorted().toList();
        System.out.println("  distinct + sorted: " + sorted);

        // sorted(Comparator) — custom order.
        List<String> names = List.of("Charlie", "Alice", "Dave", "Bob");
        List<String> byLength = names.stream()
            .sorted(Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder()))
            .toList();
        System.out.println("  sorted by length then alpha: " + byLength);

        // Sorted persons by age descending.
        List<Person> people = List.of(
            new Person("Ana", "NYC", 30),
            new Person("Ben", "LA", 25),
            new Person("Cal", "NYC", 35)
        );
        people.stream()
              .sorted(Comparator.comparingInt(Person::age).reversed())
              .map(p -> p.name() + "(" + p.age() + ")")
              .forEach(s -> System.out.println("  " + s));
    }

    static void peekLimitSkip() {
        System.out.println("\n=== peek, limit, skip ===");

        // peek — side effect without consuming; invaluable for debugging.
        // IMPORTANT: never rely on peek for business logic; it may not fire
        // for every element in all circumstances (short-circuiting, parallel).
        List<Integer> result = Stream.iterate(1, n -> n + 1)
            .peek(n -> System.out.print("  gen:" + n + " "))
            .filter(n -> n % 2 == 0)
            .peek(n -> System.out.print("pass:" + n + " "))
            .limit(3)
            .toList();
        System.out.println("\n  result: " + result);

        // skip(n) — discard the first n elements.
        List<Integer> paged = IntStream.rangeClosed(1, 20)
            .boxed()
            .skip(10)   // skip first page
            .limit(5)   // take second page (items 11-15)
            .toList();
        System.out.println("  page 2 (items 11-15): " + paged);
    }

    static void takeWhileAndDropWhile() {
        System.out.println("\n=== takeWhile and dropWhile (Java 9+) ===");

        // takeWhile — take elements while predicate holds; stop at first failure.
        // Unlike filter, it does NOT scan the rest of the stream after stopping.
        List<Integer> ascending = List.of(1, 2, 3, 4, 5, 3, 6, 7);
        List<Integer> taken = ascending.stream()
            .takeWhile(n -> n <= 4)
            .toList();
        System.out.println("  takeWhile(<= 4): " + taken);   // [1, 2, 3, 4]

        // dropWhile — skip elements while predicate holds; take the rest.
        List<Integer> dropped = ascending.stream()
            .dropWhile(n -> n <= 4)
            .toList();
        System.out.println("  dropWhile(<= 4): " + dropped); // [5, 3, 6, 7]

        // Most useful on ordered sequences (sorted data, log lines, etc.).
        List<String> log = List.of(
            "DEBUG init", "DEBUG loading", "INFO  server started", "WARN  disk 80%", "ERROR out of memory"
        );
        List<String> afterStart = log.stream()
            .dropWhile(l -> l.startsWith("DEBUG"))
            .toList();
        System.out.println("  log after DEBUG phase: " + afterStart);
    }

    static void combinedPipeline() {
        System.out.println("\n=== Combined pipeline ===");

        List<Person> people = List.of(
            new Person("Ana",     "NYC", 30),
            new Person("Ben",     "LA",  25),
            new Person("Cal",     "NYC", 35),
            new Person("Diana",   "LA",  28),
            new Person("Ethan",   "NYC", 22),
            new Person("Fiona",   "LA",  40)
        );

        // "Names of NYC residents over 25, sorted alphabetically."
        List<String> result = people.stream()
            .filter(p -> p.city().equals("NYC"))
            .filter(p -> p.age() > 25)
            .sorted(Comparator.comparing(Person::name))
            .map(Person::name)
            .toList();
        System.out.println("  NYC residents over 25: " + result);
    }
}

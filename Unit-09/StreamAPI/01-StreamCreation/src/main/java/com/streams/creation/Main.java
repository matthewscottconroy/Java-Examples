package com.streams.creation;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * All the ways to create a Stream.
 *
 * <p>A Stream is a lazy sequence of elements supporting aggregate operations.
 * "Lazy" means intermediate operations (filter, map, …) do not execute until
 * a terminal operation (collect, forEach, count, …) is called.  The pipeline
 * is assembled first, then run once end-to-end.
 *
 * <p>Sources fall into four categories:
 * <ol>
 *   <li>Collections and arrays (most common day-to-day).
 *   <li>Factory methods: {@code Stream.of}, {@code Stream.iterate},
 *       {@code Stream.generate}, {@code Stream.builder}.</li>
 *   <li>Primitive specialisations: {@code IntStream.range/rangeClosed},
 *       {@code LongStream.iterate}, {@code DoubleStream.of}.</li>
 *   <li>I/O: {@code Files.lines()}, {@code BufferedReader.lines()}.</li>
 * </ol>
 */
public class Main {

    public static void main(String[] args) throws Exception {
        fromCollectionsAndArrays();
        fromFactoryMethods();
        fromInfiniteStreams();
        fromPrimitiveStreams();
        fromIO();
        fromBuilder();
    }

    static void fromCollectionsAndArrays() {
        System.out.println("=== Collections and Arrays ===");

        // Any Collection has stream() and parallelStream().
        List<String> list = List.of("apple", "banana", "cherry");
        long count = list.stream().filter(s -> s.length() > 5).count();
        System.out.println("  words longer than 5: " + count);

        Set<Integer> set = new HashSet<>(Set.of(3, 1, 4, 7, 5, 9, 2, 6));
        System.out.print("  sorted set: ");
        set.stream().sorted().forEach(n -> System.out.print(n + " "));
        System.out.println();

        // Arrays.stream — works for objects and primitives.
        int[] primes = {2, 3, 5, 7, 11, 13};
        int sum = Arrays.stream(primes).sum();
        System.out.println("  sum of primes: " + sum);

        String[] words = {"foo", "bar", "baz"};
        Arrays.stream(words).map(String::toUpperCase).forEach(System.out::println);
    }

    static void fromFactoryMethods() {
        System.out.println("\n=== Factory Methods ===");

        // Stream.of — fixed known values; varargs overload.
        Stream<String> days = Stream.of("Mon", "Tue", "Wed", "Thu", "Fri");
        System.out.println("  weekdays: " + days.toList());

        // Stream.empty() — a stream with zero elements (avoids null returns).
        Stream<Object> empty = Stream.empty();
        System.out.println("  empty count: " + empty.count());

        // Stream.concat — merge two streams lazily.
        Stream<Integer> a = Stream.of(1, 2, 3);
        Stream<Integer> b = Stream.of(4, 5, 6);
        System.out.println("  concat: " + Stream.concat(a, b).toList());
    }

    static void fromInfiniteStreams() {
        System.out.println("\n=== Infinite Streams (always limit!) ===");

        // Stream.iterate — like a for-loop.
        // iterate(seed, hasNext, next) — Java 9+ bounded form.
        List<Integer> powers = Stream.iterate(1, n -> n < 1000, n -> n * 2)
            .toList();
        System.out.println("  powers of 2 < 1000: " + powers);

        // Classic two-arg form — infinite, so must limit.
        Stream.iterate(0, n -> n + 3)
              .limit(8)
              .forEach(n -> System.out.print(n + " "));
        System.out.println("  <- multiples of 3");

        // Stream.generate — supplies values from a Supplier (stateless or stateful).
        // Stateless example: constant.
        Stream.generate(() -> "ping")
              .limit(4)
              .forEach(s -> System.out.print(s + " "));
        System.out.println("  <- pings");

        // Stateful generate (use with care — not safe for parallel).
        int[] seq = {0};
        Stream.generate(() -> ++seq[0])
              .limit(6)
              .forEach(n -> System.out.print(n + " "));
        System.out.println("  <- natural numbers");
    }

    static void fromPrimitiveStreams() {
        System.out.println("\n=== Primitive Streams ===");

        // IntStream.range is [start, end) — exclusive upper bound.
        System.out.print("  range(0,5): ");
        IntStream.range(0, 5).forEach(n -> System.out.print(n + " "));
        System.out.println();

        // IntStream.rangeClosed is [start, end] — inclusive.
        System.out.print("  rangeClosed(1,5): ");
        IntStream.rangeClosed(1, 5).forEach(n -> System.out.print(n + " "));
        System.out.println();

        // chars() returns an IntStream of Unicode code points.
        "Hello".chars()
               .mapToObj(c -> String.valueOf((char) c))
               .forEach(c -> System.out.print(c + " "));
        System.out.println("  <- chars of \"Hello\"");

        // LongStream for large ranges.
        long sumTo100 = LongStream.rangeClosed(1, 100).sum();
        System.out.println("  sum 1..100 = " + sumTo100);
    }

    static void fromIO() throws Exception {
        System.out.println("\n=== I/O Sources ===");

        // Write a temp file to read back.
        Path tmp = Files.createTempFile("stream-demo-", ".txt");
        Files.writeString(tmp, "line one\nline two\nline three\n");

        // Files.lines() — lazy; each line is read on demand.
        // Must be used inside try-with-resources to close the underlying reader.
        try (Stream<String> lines = Files.lines(tmp)) {
            lines.map(String::toUpperCase)
                 .forEach(l -> System.out.println("  " + l));
        }

        // BufferedReader.lines() — same laziness, same close requirement.
        try (BufferedReader br = Files.newBufferedReader(tmp);
             Stream<String> lines = br.lines()) {
            long nonEmpty = lines.filter(Predicate.not(String::isBlank)).count();
            System.out.println("  non-empty lines: " + nonEmpty);
        }

        Files.deleteIfExists(tmp);
    }

    static void fromBuilder() {
        System.out.println("\n=== Stream.Builder ===");

        // Builder lets you add elements imperatively, then build once.
        // Useful when the elements come from a computation that isn't a simple
        // collection or iterator.
        Stream.Builder<String> builder = Stream.builder();
        builder.add("alpha");
        if (true) builder.add("beta");   // conditional add
        for (int i = 0; i < 3; i++) builder.add("item-" + i);

        Stream<String> built = builder.build();
        System.out.println("  built: " + built.toList());
    }
}

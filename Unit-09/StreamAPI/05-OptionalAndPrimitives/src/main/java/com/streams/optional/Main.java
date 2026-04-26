package com.streams.optional;

import java.util.*;
import java.util.stream.*;

/**
 * Optional and primitive stream specialisations.
 *
 * <p><strong>Optional</strong> — a container that may or may not hold a value.
 * It forces callers to handle the absent case explicitly, replacing the implicit
 * contract of "this method returns null on failure."  The rule: never call
 * {@code get()} without first calling {@code isPresent()} — or better, use the
 * functional API (ifPresent, map, flatMap, orElse, orElseGet, orElseThrow).
 *
 * <p><strong>Primitive streams</strong> (IntStream, LongStream, DoubleStream) —
 * avoid boxing overhead for numeric work.  They share the same lazy/terminal
 * structure as Stream<T> but add numeric operations: sum, average, range,
 * summaryStatistics, and conversions via mapToObj/boxed.
 */
public class Main {

    record User(String name, String email) {}

    public static void main(String[] args) {
        optionalBasics();
        optionalFunctionalApi();
        optionalInStreams();
        intStreamOperations();
        doubleStreamOperations();
        longStreamOperations();
        primitiveConversions();
    }

    static void optionalBasics() {
        System.out.println("=== Optional basics ===");

        Optional<String> present = Optional.of("hello");
        Optional<String> empty   = Optional.empty();

        // Never use get() without isPresent() — and even then, prefer the functional API.
        System.out.println("  present.isPresent(): " + present.isPresent());
        System.out.println("  empty.isEmpty():     " + empty.isEmpty());

        // ofNullable — wraps a potentially-null value safely.
        String maybeNull = null;
        Optional<String> wrapped = Optional.ofNullable(maybeNull);
        System.out.println("  ofNullable(null).isEmpty(): " + wrapped.isEmpty());

        // orElse — default value (always evaluated).
        String value = empty.orElse("default");
        System.out.println("  orElse: " + value);

        // orElseGet — default computed by Supplier (lazy — only called when empty).
        String computed = empty.orElseGet(() -> "computed-default");
        System.out.println("  orElseGet: " + computed);

        // orElseThrow — throw a specific exception when empty.
        try {
            empty.orElseThrow(() -> new IllegalStateException("value required"));
        } catch (IllegalStateException e) {
            System.out.println("  orElseThrow caught: " + e.getMessage());
        }
    }

    static void optionalFunctionalApi() {
        System.out.println("\n=== Optional functional API ===");

        Optional<String> name = Optional.of("  Alice  ");

        // map — transform the value if present; propagate empty otherwise.
        Optional<String> trimmed = name.map(String::trim);
        Optional<Integer> length = name.map(String::trim).map(String::length);
        System.out.println("  trimmed: " + trimmed.orElse("none"));
        System.out.println("  length:  " + length.orElse(-1));

        // filter — keep value only if predicate holds; otherwise empty.
        Optional<String> longName = name.map(String::trim).filter(s -> s.length() > 3);
        Optional<String> shortName = name.map(String::trim).filter(s -> s.length() > 10);
        System.out.println("  longName present: " + longName.isPresent());
        System.out.println("  shortName present: " + shortName.isPresent());

        // flatMap — for methods that themselves return Optional (avoids Optional<Optional<T>>).
        Optional<User> user = Optional.of(new User("Bob", "bob@example.com"));
        Optional<String> email = user.flatMap(u ->
            Optional.ofNullable(u.email()).filter(e -> e.contains("@")));
        System.out.println("  email: " + email.orElse("no valid email"));

        // ifPresent — side effect only when value exists.
        Optional.of("logged event").ifPresent(e -> System.out.println("  event: " + e));

        // ifPresentOrElse (Java 9+) — two-branch consumer.
        Optional.empty().ifPresentOrElse(
            v -> System.out.println("  got: " + v),
            () -> System.out.println("  nothing to process"));

        // or() (Java 9+) — supply an alternative Optional when empty.
        Optional<String> fallback = Optional.<String>empty()
            .or(() -> Optional.of("fallback-value"));
        System.out.println("  or fallback: " + fallback.orElseThrow());
    }

    static void optionalInStreams() {
        System.out.println("\n=== Optional in streams ===");

        // stream() (Java 9+) — converts Optional to a 0- or 1-element Stream.
        // This lets you flatMap a Stream<Optional<T>> into Stream<T>.
        List<Optional<String>> maybes = List.of(
            Optional.of("alpha"),
            Optional.empty(),
            Optional.of("beta"),
            Optional.empty(),
            Optional.of("gamma")
        );

        List<String> present = maybes.stream()
            .flatMap(Optional::stream)   // flatMap over the 0/1-element streams
            .toList();
        System.out.println("  present values: " + present);

        // Pattern: stream of lookups, keep only the hits.
        Map<String, Integer> scores = Map.of("Alice", 95, "Bob", 87, "Carol", 91);
        List<String> names = List.of("Alice", "Dave", "Carol", "Eve", "Bob");

        List<Integer> found = names.stream()
            .map(n -> Optional.ofNullable(scores.get(n)))
            .flatMap(Optional::stream)
            .sorted(Comparator.reverseOrder())
            .toList();
        System.out.println("  found scores (desc): " + found);
    }

    static void intStreamOperations() {
        System.out.println("\n=== IntStream ===");

        // Arithmetic operations unavailable on Stream<Integer>.
        IntSummaryStatistics stats = IntStream.rangeClosed(1, 10).summaryStatistics();
        System.out.printf("  1-10: sum=%d, avg=%.1f, min=%d, max=%d%n",
            stats.getSum(), stats.getAverage(), stats.getMin(), stats.getMax());

        // Generating index-element pairs.
        String[] fruits = {"apple", "banana", "cherry", "date"};
        IntStream.range(0, fruits.length)
                 .mapToObj(i -> i + ": " + fruits[i])
                 .forEach(s -> System.out.println("  " + s));

        // Triangular numbers: 1, 3, 6, 10, ...
        List<Integer> triangular = IntStream.rangeClosed(1, 8)
            .map(n -> n * (n + 1) / 2)
            .boxed()
            .toList();
        System.out.println("  triangular: " + triangular);
    }

    static void doubleStreamOperations() {
        System.out.println("\n=== DoubleStream ===");

        double[] weights = {65.5, 72.1, 88.0, 55.9, 93.4};
        DoubleSummaryStatistics ws = Arrays.stream(weights).summaryStatistics();
        System.out.printf("  weights: avg=%.2f, min=%.1f, max=%.1f%n",
            ws.getAverage(), ws.getMin(), ws.getMax());

        // Simulate normalising to [0, 1].
        double min = ws.getMin(), max = ws.getMax();
        double[] normalised = Arrays.stream(weights)
            .map(w -> (w - min) / (max - min))
            .toArray();
        System.out.print("  normalised: ");
        Arrays.stream(normalised).mapToObj("%.3f"::formatted)
              .forEach(s -> System.out.print(s + " "));
        System.out.println();

        // DoubleStream.iterate for a geometric sequence.
        DoubleStream.iterate(1.0, d -> d * 1.5)
                    .limit(7)
                    .mapToObj("%.2f"::formatted)
                    .forEach(s -> System.out.print(s + " "));
        System.out.println("  <- geometric(1.5)");
    }

    static void longStreamOperations() {
        System.out.println("\n=== LongStream ===");

        // Large range — would overflow int.
        long sumBig = LongStream.rangeClosed(1L, 1_000_000L).sum();
        System.out.println("  sum 1..1,000,000 = " + sumBig);

        // Powers of two (long needed for values > 2^31).
        List<Long> powers = LongStream.iterate(1L, n -> n * 2)
            .limit(20)
            .boxed()
            .toList();
        System.out.println("  first 20 powers of 2 (last): " + powers.getLast());

        // File-size simulation: sum bytes across 'files'.
        long[] fileSizes = LongStream.iterate(1024, s -> s * 2).limit(10).toArray();
        long totalBytes = Arrays.stream(fileSizes).sum();
        System.out.printf("  total of 10 doubling files: %,d bytes%n", totalBytes);
    }

    static void primitiveConversions() {
        System.out.println("\n=== Primitive <-> Object conversions ===");

        // Stream<T> → primitive stream.
        List<String> words = List.of("hello", "world", "java", "streams");
        int totalChars = words.stream().mapToInt(String::length).sum();
        System.out.println("  total chars: " + totalChars);

        // Primitive stream → Stream<T> via mapToObj or boxed().
        List<Integer> boxed = IntStream.range(1, 6).boxed().toList();
        System.out.println("  boxed:       " + boxed);

        // asLongStream / asDoubleStream — widen without boxing.
        double avg = IntStream.of(10, 20, 30, 40).asDoubleStream().average().orElse(0);
        System.out.println("  avg of ints as doubles: " + avg);

        // Primitive OptionalInt/Long/Double — same functional API.
        OptionalInt first = IntStream.range(1, 100).filter(n -> n * n > 50).findFirst();
        System.out.println("  first n where n²>50: " + first.orElse(-1));
    }
}

package com.functional.parallel;

import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

/**
 * Demonstrates parallel streams with a web server log analyser.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Log Analyser (Parallel Streams) ===\n");

        final int LOG_SIZE = 100_000;
        List<LogEntry> logs = LogGenerator.generate(LOG_SIZE, 42L);

        System.out.printf("Analysing %,d log entries…%n%n", LOG_SIZE);

        // Sequential baseline
        long t0 = System.currentTimeMillis();
        long seqErrors = logs.stream().filter(LogEntry::isError).count();
        long seqTime   = System.currentTimeMillis() - t0;

        // Parallel — identical pipeline, one word changed
        t0 = System.currentTimeMillis();
        long parErrors = logs.parallelStream().filter(LogEntry::isError).count();
        long parTime   = System.currentTimeMillis() - t0;

        System.out.printf("Error count (sequential):  %,d  in %d ms%n", seqErrors, seqTime);
        System.out.printf("Error count (parallel):    %,d  in %d ms%n%n", parErrors, parTime);

        // Results must be identical
        System.out.println("Results match: " + (seqErrors == parErrors));

        // Aggregation using parallel stream
        OptionalDouble avgDuration = logs.parallelStream()
                .mapToLong(LogEntry::durationMs)
                .average();
        avgDuration.ifPresent(avg -> System.out.printf("%nAverage response time: %.1f ms%n", avg));

        long slowRequests = logs.parallelStream()
                .filter(LogEntry::isSlowRequest)
                .count();
        System.out.printf("Slow requests (>500 ms):   %,d  (%.1f%%)%n",
                slowRequests, 100.0 * slowRequests / LOG_SIZE);

        // groupingBy is safe in parallel
        Map<String, Long> countByLevel = logs.parallelStream()
                .collect(Collectors.groupingBy(LogEntry::level, Collectors.counting()));
        System.out.println("\nEntries by log level:");
        countByLevel.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(e -> System.out.printf("  %-6s %,d%n", e.getKey(), e.getValue()));
    }
}

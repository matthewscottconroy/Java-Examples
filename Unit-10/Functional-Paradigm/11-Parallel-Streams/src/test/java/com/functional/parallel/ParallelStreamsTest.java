package com.functional.parallel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ParallelStreamsTest {

    private List<LogEntry> logs;

    @BeforeEach
    void setUp() {
        logs = LogGenerator.generate(10_000, 99L);
    }

    @Test
    @DisplayName("Sequential and parallel streams produce identical counts")
    void seqAndParallelMatchOnCount() {
        long seq = logs.stream().filter(LogEntry::isError).count();
        long par = logs.parallelStream().filter(LogEntry::isError).count();
        assertEquals(seq, par);
    }

    @Test
    @DisplayName("Sequential and parallel streams produce identical sums")
    void seqAndParallelMatchOnSum() {
        long seq = logs.stream().mapToLong(LogEntry::durationMs).sum();
        long par = logs.parallelStream().mapToLong(LogEntry::durationMs).sum();
        assertEquals(seq, par);
    }

    @Test
    @DisplayName("groupingBy with parallelStream produces same groups as sequential")
    void groupingByLevelMatchesSeq() {
        Map<String, Long> seq = logs.stream()
                .collect(Collectors.groupingBy(LogEntry::level, Collectors.counting()));
        Map<String, Long> par = logs.parallelStream()
                .collect(Collectors.groupingBy(LogEntry::level, Collectors.counting()));
        assertEquals(seq, par);
    }

    @Test
    @DisplayName("All log levels present in the dataset")
    void allLevelsPresent() {
        long infoCount = logs.parallelStream()
                .filter(e -> e.level().equals("INFO")).count();
        long warnCount = logs.parallelStream()
                .filter(e -> e.level().equals("WARN")).count();
        long errCount  = logs.parallelStream()
                .filter(e -> e.level().equals("ERROR")).count();
        assertTrue(infoCount > 0);
        assertTrue(warnCount > 0);
        assertTrue(errCount  > 0);
    }

    @Test
    @DisplayName("Average response time is in a plausible range")
    void averageResponseTimeIsPlausible() {
        double avg = logs.parallelStream()
                .mapToLong(LogEntry::durationMs)
                .average()
                .orElseThrow();
        assertTrue(avg > 20 && avg < 1000);
    }
}

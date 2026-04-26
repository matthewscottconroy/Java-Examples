package com.streams.collect;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * The Collectors utility class — pre-built reduction strategies.
 *
 * <p>Collectors are the bridge between the functional stream pipeline and the
 * mutable data structures you ultimately need (maps, lists grouped by key, etc.).
 * They encapsulate three operations: supplier (create container), accumulator
 * (add element), and combiner (merge two containers for parallel execution).
 *
 * <p>Downstream collectors let you nest reductions: group first, then count,
 * average, or further group within each bucket.
 */
public class Main {

    record Employee(String name, String dept, double salary, String city) {}

    static final List<Employee> EMPLOYEES = List.of(
        new Employee("Alice",   "Eng",   95_000, "NYC"),
        new Employee("Bob",     "Eng",   88_000, "LA"),
        new Employee("Carol",   "HR",    65_000, "NYC"),
        new Employee("Dan",     "HR",    70_000, "LA"),
        new Employee("Eve",     "Eng",  110_000, "NYC"),
        new Employee("Frank",   "Sales", 72_000, "NYC"),
        new Employee("Grace",   "Sales", 68_000, "LA"),
        new Employee("Hank",    "Eng",   92_000, "LA")
    );

    public static void main(String[] args) {
        groupingBy();
        partitioningBy();
        joining();
        toMap();
        downstream();
        teeing();
    }

    static void groupingBy() {
        System.out.println("=== groupingBy ===");

        // Basic — group into List (default downstream collector).
        Map<String, List<Employee>> byDept = EMPLOYEES.stream()
            .collect(Collectors.groupingBy(Employee::dept));

        byDept.forEach((dept, emps) -> {
            List<String> names = emps.stream().map(Employee::name).toList();
            System.out.println("  " + dept + ": " + names);
        });

        // With a downstream collector — count per department.
        Map<String, Long> countByDept = EMPLOYEES.stream()
            .collect(Collectors.groupingBy(Employee::dept, Collectors.counting()));
        System.out.println("  count by dept: " + countByDept);

        // Average salary by department.
        Map<String, Double> avgSalary = EMPLOYEES.stream()
            .collect(Collectors.groupingBy(
                Employee::dept,
                Collectors.averagingDouble(Employee::salary)));
        avgSalary.forEach((dept, avg) ->
            System.out.printf("  %s avg salary: $%.0f%n", dept, avg));

        // Two-level grouping — first by dept, then by city.
        Map<String, Map<String, List<String>>> byDeptCity = EMPLOYEES.stream()
            .collect(Collectors.groupingBy(
                Employee::dept,
                Collectors.groupingBy(
                    Employee::city,
                    Collectors.mapping(Employee::name, Collectors.toList()))));
        byDeptCity.forEach((dept, cities) ->
            System.out.println("  " + dept + " -> " + cities));
    }

    static void partitioningBy() {
        System.out.println("\n=== partitioningBy ===");

        // partitioningBy always produces exactly two keys: true and false.
        Map<Boolean, List<Employee>> highLow = EMPLOYEES.stream()
            .collect(Collectors.partitioningBy(e -> e.salary() >= 90_000));

        System.out.println("  high earners (>=90k): " +
            highLow.get(true).stream().map(Employee::name).toList());
        System.out.println("  lower earners:        " +
            highLow.get(false).stream().map(Employee::name).toList());

        // With downstream — count in each partition.
        Map<Boolean, Long> partCount = EMPLOYEES.stream()
            .collect(Collectors.partitioningBy(
                e -> e.dept().equals("Eng"),
                Collectors.counting()));
        System.out.println("  eng / non-eng: " + partCount.get(true) + " / " + partCount.get(false));
    }

    static void joining() {
        System.out.println("\n=== joining ===");

        // Simple join.
        String allNames = EMPLOYEES.stream()
            .map(Employee::name)
            .collect(Collectors.joining(", "));
        System.out.println("  all names: " + allNames);

        // With prefix and suffix — CSV row, JSON array, etc.
        String json = EMPLOYEES.stream()
            .filter(e -> e.dept().equals("Eng"))
            .map(e -> "\"" + e.name() + "\"")
            .collect(Collectors.joining(", ", "[", "]"));
        System.out.println("  eng JSON:  " + json);
    }

    static void toMap() {
        System.out.println("\n=== toMap ===");

        // toMap(keyMapper, valueMapper) — throws on duplicate keys.
        Map<String, Double> salaryByName = EMPLOYEES.stream()
            .collect(Collectors.toMap(Employee::name, Employee::salary));
        System.out.println("  Alice's salary: $" + salaryByName.get("Alice"));

        // Merge function — resolves duplicate keys.
        // Keep the higher salary if two employees share a name (contrived, but shows the API).
        Map<String, Double> maxByDept = EMPLOYEES.stream()
            .collect(Collectors.toMap(
                Employee::dept,
                Employee::salary,
                Math::max));           // merge: keep the higher value
        System.out.println("  max salary by dept: " + maxByDept);

        // Controlling the map implementation.
        TreeMap<String, Employee> byNameTreeMap = EMPLOYEES.stream()
            .collect(Collectors.toMap(
                Employee::name,
                Function.identity(),
                (a, b) -> a,           // duplicate resolver (won't trigger here)
                TreeMap::new));        // sorted map
        System.out.println("  first entry (TreeMap): " + byNameTreeMap.firstKey());
    }

    static void downstream() {
        System.out.println("\n=== Downstream collectors: mapping, filtering, summarizing ===");

        // Collectors.mapping — transform elements before accumulating.
        Map<String, List<String>> namesByDept = EMPLOYEES.stream()
            .collect(Collectors.groupingBy(
                Employee::dept,
                Collectors.mapping(Employee::name, Collectors.toList())));
        System.out.println("  names by dept: " + namesByDept);

        // Collectors.filtering (Java 9+) — filter inside the downstream.
        // Different from stream.filter: unmatched depts still appear with empty lists.
        Map<String, List<Employee>> highEarnersByDept = EMPLOYEES.stream()
            .collect(Collectors.groupingBy(
                Employee::dept,
                Collectors.filtering(e -> e.salary() > 80_000, Collectors.toList())));
        highEarnersByDept.forEach((dept, emps) ->
            System.out.println("  " + dept + " >80k: " +
                emps.stream().map(Employee::name).toList()));

        // Collectors.summarizingDouble.
        Map<String, DoubleSummaryStatistics> statsByDept = EMPLOYEES.stream()
            .collect(Collectors.groupingBy(
                Employee::dept,
                Collectors.summarizingDouble(Employee::salary)));
        statsByDept.forEach((dept, s) ->
            System.out.printf("  %s: count=%d avg=%.0f%n", dept, s.getCount(), s.getAverage()));
    }

    static void teeing() {
        System.out.println("\n=== Collectors.teeing (Java 12+) ===");

        // teeing runs two collectors on the same stream simultaneously,
        // then merges their results with a BiFunction.
        // Useful when you need two aggregates without iterating the source twice.

        // Example: find both the min and max salary in one pass.
        record SalaryRange(double min, double max) {}

        SalaryRange range = EMPLOYEES.stream()
            .collect(Collectors.teeing(
                Collectors.minBy(Comparator.comparingDouble(Employee::salary)),
                Collectors.maxBy(Comparator.comparingDouble(Employee::salary)),
                (minOpt, maxOpt) -> new SalaryRange(
                    minOpt.map(Employee::salary).orElse(0.0),
                    maxOpt.map(Employee::salary).orElse(0.0))));
        System.out.printf("  salary range: $%.0f — $%.0f%n", range.min(), range.max());

        // Example: split into two lists in one pass.
        record Split(List<String> senior, List<String> junior) {}

        Split split = EMPLOYEES.stream()
            .collect(Collectors.teeing(
                Collectors.filtering(e -> e.salary() >= 90_000,
                    Collectors.mapping(Employee::name, Collectors.toList())),
                Collectors.filtering(e -> e.salary() < 90_000,
                    Collectors.mapping(Employee::name, Collectors.toList())),
                Split::new));
        System.out.println("  senior (>=90k): " + split.senior());
        System.out.println("  junior (<90k):  " + split.junior());
    }
}

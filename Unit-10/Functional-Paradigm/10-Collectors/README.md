# 10 — Collectors: The Order Report

## The Story

The operations team needs a weekly order report: orders grouped by category,
total revenue per region ranked highest to lowest, a count of shipped vs. pending,
the average order value, and a comma-separated product list for the newsletter.

Before `Collectors`, each of these was a separate loop with its own `Map` and
accumulator. With `Collectors`, each question is a one-liner passed to `collect`.

---

## What Collectors Do

`Stream.collect(Collector)` is the terminal operation that assembles stream
elements into a result container — a list, map, string, or statistics object.

`java.util.stream.Collectors` is the standard library of ready-made collectors.

---

## The Key Collectors

**`toList()`** — accumulate into a `List`:
```java
List<String> names = orders.stream().map(Order::customer).collect(Collectors.toList());
```

**`groupingBy(classifier)`** — partition into `Map<K, List<V>>`:
```java
Map<String, List<Order>> byCategory = orders.stream()
    .collect(Collectors.groupingBy(Order::category));
```

**`groupingBy(classifier, downstream)`** — group then aggregate within each group:
```java
Map<String, Double> revenueByRegion = orders.stream()
    .collect(Collectors.groupingBy(Order::region,
             Collectors.summingDouble(Order::total)));
```

**`partitioningBy(predicate)`** — two groups: `true` and `false`:
```java
Map<Boolean, List<Order>> shipped = orders.stream()
    .collect(Collectors.partitioningBy(Order::shipped));
```

**`counting()`** — count elements (used as a downstream collector):
```java
Map<String, Long> perCustomer = orders.stream()
    .collect(Collectors.groupingBy(Order::customer, Collectors.counting()));
```

**`summarizingDouble(mapper)`** — min, max, sum, average, count in one pass.

**`joining(delimiter)`** — concatenate strings:
```java
String list = items.stream().collect(Collectors.joining(", "));
```

---

## Composing Collectors

The power of collectors comes from composing them. `groupingBy` with a downstream
collector lets you group and then aggregate in a single expression. You can
nest multiple levels:

```java
// Region → Category → revenue
Map<String, Map<String, Double>> result = orders.stream()
    .collect(Collectors.groupingBy(Order::region,
             Collectors.groupingBy(Order::category,
             Collectors.summingDouble(Order::total))));
```

---

## Commands

```bash
mvn compile exec:java
mvn test
```

# 09 — Streams Pipeline: The Sales Dashboard

## The Story

The sales team wants answers: total revenue, top three deals, every unique
product alphabetically, the biggest single transaction. Before Streams, each
question was a for-loop with a mutable accumulator — readable enough alone,
but tedious to combine and impossible to parallelise without extra work.

With Streams, each question is a pipeline: a source, a sequence of intermediate
operations, and a terminal operation that produces the result.

```java
double total = sales.stream()
    .filter(s -> s.category().equals("Electronics"))
    .mapToDouble(Sale::revenue)
    .sum();
```

The pipeline reads like the question it answers.

---

## The Three Parts of a Pipeline

**Source** — creates the stream from an existing data structure:
`collection.stream()`, `Arrays.stream(arr)`, `Stream.of(...)`, `Files.lines(path)`

**Intermediate operations** — transform the stream lazily; each returns a new stream:

| Operation | What it does |
|-----------|-------------|
| `filter(Predicate)` | Keep elements matching the predicate |
| `map(Function)` | Transform each element |
| `mapToDouble/Int/Long` | Transform to a primitive stream |
| `flatMap(Function)` | Replace each element with a stream, flatten |
| `distinct()` | Remove duplicates |
| `sorted(Comparator)` | Sort |
| `limit(n)` | Keep at most n elements |
| `peek(Consumer)` | Side-effect without consuming (useful for debugging) |

**Terminal operations** — trigger evaluation and produce a result:

| Operation | What it produces |
|-----------|-----------------|
| `collect(Collector)` | A collection or other container |
| `count()` | Long count of elements |
| `sum()`, `average()`, `min()`, `max()` | Numeric aggregates (primitive streams) |
| `findFirst()`, `findAny()` | Optional of first/any element |
| `reduce(identity, accumulator)` | Fold all elements into one value |
| `forEach(Consumer)` | Side-effect on each element |

---

## Laziness

Intermediate operations build a *description* of the computation. No element is
processed until a terminal operation is called. This means the pipeline can
short-circuit — `findFirst()` stops as soon as it finds an answer, even if the
source has millions of elements.

---

## Commands

```bash
mvn compile exec:java
mvn test
```

# 11 — Parallel Streams: The Log Analyser

## The Story

A web server generates 100,000 log entries overnight. In the morning, the ops
team wants errors counted, slow requests identified, and a breakdown by log level.
Processing this sequentially works fine, but a machine with eight cores could do
it in roughly one eighth of the time if the work were divided across them.

Switching a stream to parallel is one word:

```java
// Sequential
logs.stream().filter(LogEntry::isError).count();

// Parallel — identical result, potentially faster
logs.parallelStream().filter(LogEntry::isError).count();
```

The framework splits the source, farms the work to the common ForkJoinPool, and
combines the results. The pipeline code is unchanged.

---

## When Parallel Helps

Parallel streams deliver real speedups when:

- The dataset is large (at minimum thousands of elements)
- Each element's processing is CPU-bound and takes significant time
- The operations are stateless and associative (filter, map, reduce, groupingBy)
- Order of the output doesn't matter, or ordering is restored afterwards

They *hurt* when:

- The dataset is small — thread coordination overhead dominates
- Operations have side effects or shared mutable state — race conditions
- I/O-bound work — all threads block together, no speedup
- Source cannot be split efficiently (e.g., `LinkedList`)

---

## Thread Safety Rules

**Safe in parallel:**
- Stateless lambdas: `filter(e -> e.isError())`
- Collectors: `groupingBy`, `counting`, `summingDouble`
- Reduction with associative, identity-based operations

**Unsafe in parallel:**
- Mutating an external list inside `forEach` — race condition
- Non-associative accumulation — result depends on thread scheduling

The golden rule: if the sequential version has no shared mutable state, the
parallel version is safe. If it does, make the state thread-safe or restructure
to avoid it.

---

## Commands

```bash
mvn compile exec:java
mvn test
```

# 15 — Recursion and Memoization: The File System

## The Story

A file system is naturally recursive: a directory contains files and other
directories, which may contain more directories. Any operation that spans the
whole tree — compute total size, list all paths, find large files — has the same
shape: handle the base case (a file), recurse into the children (a directory),
and combine results.

```java
public static long totalSize(FileNode node) {
    if (!node.isDirectory()) return node.sizeBytes();   // base case
    return node.children().stream()
            .mapToLong(FileSystem::totalSize)            // recursive case
            .sum();
}
```

The second story is Fibonacci: a textbook example of a function whose naive
recursive form has exponential time complexity because it recomputes the same
subproblems millions of times. Memoization fixes this by caching every result
the first time it's computed.

---

## Recursion in Functional Programming

Functional languages favour recursion over iteration because:

- It works naturally on recursive data structures (trees, lists)
- Each call is self-contained — no mutable loop variable
- It composes: `totalSize` calls `totalSize` without needing external state

Java's Stack frames limit depth (~thousands), so recursion suits moderate-depth
structures (file trees, JSON, XML). For large flat sequences, prefer streams.

---

## Memoization

Memoization is a caching technique specific to **pure functions**: because the
same input always produces the same output, you can store the result in a map and
return it instantly on future calls.

```java
Function<Integer, Long> fib = Memo.memoize(self -> n ->
    n <= 1 ? (long) n : self.apply(n - 1) + self.apply(n - 2));

fib.apply(50); // computed once: O(n) instead of O(2^n)
fib.apply(50); // returned from cache: O(1)
```

The Y-combinator trick lets the lambda refer to itself through the `self`
parameter — necessary because Java lambdas can't capture an as-yet-unassigned
variable.

---

## Time Complexity: Naive vs. Memoized Fibonacci

| n | Naive calls | Memoized calls |
|---|------------|---------------|
| 10 | 177 | 19 |
| 20 | 21,891 | 39 |
| 40 | ~2.3 billion | 79 |
| 50 | impractical | 99 |

Memoization turns exponential into linear at the cost of O(n) memory.

---

## Commands

```bash
mvn compile exec:java
mvn test
```

# 13 — Result Type: The CSV Parser

## The Story

You're importing a product catalogue from a CSV file. Some rows are valid; some
have a blank name, a negative price, or a non-numeric ID. You need to parse as
many as you can and report every error clearly — not crash on the first bad row.

Exceptions make this awkward: you'd need try/catch inside a loop, partial results
accumulating in a mutable list, and error messages collected in a separate
structure. The control flow fights the data flow.

The `Result<T>` type makes errors first-class values:

```java
Result<Product> r = CsvParser.parseRow("bad,Laptop,1000.00,5");
// r is Err("Invalid ID: 'bad'")
// Nothing throws. The error is just data flowing through the pipeline.
```

---

## What the Result Type Is

`Result<T>` is a sealed interface with exactly two implementations:

- `Success<T>` — carries the parsed value
- `Failure<T>` — carries an error message

It is Java's approximation of Haskell's `Either`, Scala's `Either`, and Rust's
`Result<T, E>`. The same idea exists in every functional language because it
solves a real problem: how to represent *the possibility of failure* without
resorting to null or exceptions.

---

## Composing Results

The power comes from `map` and `flatMap`:

```java
Result<Double> discounted = parseRow(row)    // Result<Product>
    .map(p -> p.price() * 0.90);             // Result<Double>
```

If `parseRow` returns `Err`, the `map` is skipped and the error propagates
unchanged. You never write:

```java
if (result.isOk()) { ... }
```

unless you're at the boundary where you need to act on the value.

**Chaining fallible steps with flatMap:**

```java
parseId(parts[0])
    .flatMap(id  -> parseName(parts[1])
    .flatMap(name -> parsePrice(parts[2])
    .map(price    -> new Product(id, name, price))));
```

Each step only runs if the previous succeeded. The first failure short-circuits
everything downstream.

---

## When to Use It

| Use Result | Use Optional | Use exceptions |
|-----------|-------------|---------------|
| Failure has a meaningful message | Absence is routine, message-free | Truly exceptional, unrecoverable |
| Errors should flow through pipelines | Represent "nothing found" | Violations of programming contract |
| Batch processing where all errors matter | API return values that may be absent | Infrastructure failures |

---

## Commands

```bash
mvn compile exec:java
mvn test
```

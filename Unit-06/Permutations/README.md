# Packages — A Maven Project Organized into Packages

This project demonstrates how to divide a Java application into **packages**,
each with a focused responsibility. It's structured as a proper Maven project
with unit tests.

---

## The Story

You're organizing a talent show. Four performers need to be scheduled in some
order. Simple question: **how many different orderings are there?**

The answer — `4! = 24` — comes from **permutation theory**, a corner of
mathematics with a surprising depth. Every rearrangement of a set is a
permutation, and the collection of all permutations of n items forms an
algebraic structure called the **symmetric group S_n**.

What makes this fun is that S_n doesn't care about *what* you're permuting.
Shuffling performers is structurally identical to shuffling numbers. This
equivalence — a bijection that preserves the "shuffle structure" — is called
an **isomorphism**.

This project makes all of that concrete and playful.

---

## Package Overview

```
com.examples.math      — Permutation algebra and structure maps
com.examples.format    — Terminal rendering and pretty output
com.examples.app       — Main program: the talent show problem
```

### `com.examples.math`

| Class | Purpose |
|---|---|
| `Permutation` | An immutable rearrangement of n positions. Supports composition, inversion, and cycle notation. |
| `Arrangement<T>` | A typed list with permutation operations. Counts and enumerates all orderings. |
| `StructureMap<T,U>` | A bijection between element sets that demonstrates the isomorphism property. |

### `com.examples.format`

| Class | Purpose |
|---|---|
| `AnsiColor` | ANSI escape code constants for colorful terminal output. |
| `TablePrinter` | Renders data grids as ASCII box-drawing tables. |
| `ArrangementRenderer` | Visually displays arrangements, permutation mappings, and structure maps. |

### `com.examples.app`

| Class | Purpose |
|---|---|
| `Main` | Walks through the talent show lineup problem from start to finish. |

---

## Building and Running

```bash
# Compile and run
mvn compile exec:java -Dexec.mainClass="com.examples.app.Main"

# Run all tests
mvn test

# Package into a runnable jar
mvn package
java -jar target/permutations-1.0-SNAPSHOT.jar

# Generate Javadocs (output: target/site/apidocs/index.html)
mvn javadoc:javadoc
```

---

## The Math, Briefly

A **permutation** of n items is a bijection `σ: {0,…,n−1} → {0,…,n−1}`.
Think of it as a lookup table: "position 0 now holds whatever was at index
`σ(0)`."

Two permutations can be **composed**: apply σ first, then τ. Composition is
associative, there's an identity permutation (do nothing), and every
permutation has an inverse (undo it). That makes the set of all permutations
a **group** — the symmetric group `S_n`, which has exactly `n!` elements.

A **structure map** `φ: A → B` between two sets is an isomorphism of their
symmetric groups when permuting then relabeling gives the same result as
relabeling then permuting:

```
φ(σ(arrangement_A))  ==  σ(φ(arrangement_A))
```

The insight: this holds for *any* bijection φ, because permutations act on
positions, not element values. It means `S_n` is truly abstract — it only
depends on `n`, never on what you're shuffling.

---

## Tests

Unit tests live in `src/test/java/com/examples/math/` and use **JUnit 5
(Jupiter)** — the current standard for Java unit testing.

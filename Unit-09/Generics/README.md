# Java Generics — Graduated Examples

Six self-contained Maven projects that introduce generics from first principles and build each concept on the last.

| # | Directory | Topics covered |
|---|-----------|----------------|
| 1 | `01-GenericBox` | Generic classes, type parameters, type safety vs. `Object`-based storage |
| 2 | `02-GenericMethods` | Generic methods, multiple type parameters (`Pair<K,V>`), type inference, type witnesses |
| 3 | `03-BoundedTypes` | Upper bounds (`extends Number`), recursive bounds (`Comparable<T>`), multiple bounds |
| 4 | `04-Wildcards` | Unbounded / upper / lower-bounded wildcards, PECS, invariance |
| 5 | `05-TypeErasure` | What erasure does at runtime, bridge methods, restrictions, type tokens |
| 6 | `06-Patterns` | Generic builder, self-bounded fluent generics, generic singleton factory |

## Running any example

```
cd 01-GenericBox
mvn compile exec:java
```

Each project compiles with Java 17 and requires no external dependencies beyond JUnit 5 (test scope only).

## Concept Map

```
01 → class Box<T>                     type parameter, diamond operator
02 → <T> T method(T arg)              generic methods, Pair<K,V>, inference
03 → <T extends Number>               upper bounds, recursive & multiple bounds
04 → List<? extends Number>           wildcards, PECS, invariance vs. covariance
05 → erasure, bridge methods          runtime view, restrictions, type tokens
06 → Builder<T extends Builder<T>>    patterns: builder, fluent, singleton factory
```

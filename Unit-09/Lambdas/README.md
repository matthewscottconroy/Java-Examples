# Java Lambdas — Graduated Examples

Four self-contained Maven projects that build from first-principles functional interfaces
to higher-order function patterns.

| # | Directory | Topics covered |
|---|-----------|----------------|
| 1 | `01-FunctionalInterfaces` | @FunctionalInterface, java.util.function, checked-exception wrappers |
| 2 | `02-MethodReferences` | All four reference kinds: static, bound, unbound, constructor |
| 3 | `03-CaptureAndComposition` | Capture rules, effectively final, andThen/compose, Predicate combinators |
| 4 | `04-FunctionPatterns` | Partial application, currying, memoization, pipeline builder |

## Running any example

```
cd 01-FunctionalInterfaces
mvn compile exec:java
```

## Mental model

```
01  Functional interface   one abstract method; a lambda IS an instance of one
02  Method reference       shorthand for a lambda that just calls an existing method
03  Composition            build complex behaviours from small, composable pieces
04  Higher-order functions functions that take or return functions — the functional toolkit
```

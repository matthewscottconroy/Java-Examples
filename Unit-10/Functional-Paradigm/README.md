# Functional Paradigm in Java

A collection of 15 Maven/Java 17 projects, each focused on one concept from the
functional programming paradigm as it applies to modern Java. Every project
includes a concrete real-world example, unit tests, and a README.

---

## Why functional programming?

Object-oriented programming organises behaviour around *objects* — things that
hold state and receive messages. Functional programming organises behaviour
around *functions* — things that transform values. In practice, Java programmers
use both, often in the same file.

The functional style becomes compelling when you notice the friction that
object-oriented code produces at scale:

**Mutation is hard to reason about.** When any method can change any field at
any time, tracing why a value is wrong requires understanding every code path
that could have touched it. Pure functions sidestep this entirely — their output
depends only on their input, so you can test them in isolation and trust them in
parallel.

**Shared state and threads are a constant hazard.** Immutable values need no
locks. Stateless functions need no synchronisation. Parallel streams and
`CompletableFuture` pipelines work correctly by default when the operations they
run are pure.

**Glue code accumulates.** Every callback, every anonymous adapter class, every
single-method interface introduces boilerplate that separates the intent ("sort
by last name") from the machinery ("define a Comparator, override compare, return
…"). Lambdas and method references collapse this gap.

**Error handling is tangled with control flow.** Try/catch blocks interrupt the
natural flow of data through a pipeline. The `Result` type makes errors
first-class values that compose the same way any other value does.

None of these ideas are exclusive to functional languages — they're available
in Java right now. The goal of this collection is to show what they look like in
practice.

---

## How to read this collection

Each module is independent. You don't need to start at 01. The groupings below
suggest a logical progression, but you can jump to whichever topic is most
relevant to you right now.

The best way to learn a concept is to run the demo (`mvn compile exec:java`),
read the README, and then read the source. The tests show the essential
behaviour in the smallest possible form — reading them first often gives you the
clearest picture of what each class does.

---

## Running any project

```bash
cd <module-directory>
mvn compile exec:java   # run the demo
mvn test                # run the unit tests
```

---

## Foundations

The building blocks of the functional style in Java.

| # | Module | Example | Core concept |
|---|--------|---------|--------------|
| 01 | [Lambda Expressions](01-Lambda-Expressions/) | Tax Rule Engine | Functions as first-class values; `@FunctionalInterface` |
| 02 | [Pure Functions](02-Pure-Functions/) | Unit Converter | Same input → same output; no side effects; immutability |
| 03 | [Higher-Order Functions](03-Higher-Order-Functions/) | Payroll Processor | Functions that take or return other functions |
| 04 | [Method References](04-Method-References/) | Contact Book | Four kinds of method reference; when to prefer them over lambdas |

---

## Building Blocks

Techniques for composing and specialising functions.

| # | Module | Example | Core concept |
|---|--------|---------|--------------|
| 05 | [Function Composition](05-Function-Composition/) | Validation Pipeline | `andThen`, `compose`; short-circuiting chains |
| 06 | [Currying & Partial Application](06-Currying-and-Partial-Application/) | Shipping Calculator | Fix some arguments early; return a function for the rest |
| 07 | [Closures](07-Closures/) | Counter Factory | Functions that capture and own their enclosing state |
| 08 | [Lazy Evaluation](08-Lazy-Evaluation/) | Configuration Loader | Defer computation until the result is needed; `Supplier<T>` |

---

## The Streams Model

Java's built-in functional data pipeline.

| # | Module | Example | Core concept |
|---|--------|---------|--------------|
| 09 | [Streams Pipeline](09-Streams-Pipeline/) | Sales Dashboard | `filter`, `map`, `flatMap`, `reduce`; lazy pipeline execution |
| 10 | [Collectors](10-Collectors/) | Order Report | `groupingBy`, `partitioningBy`, `joining`, `summarizingDouble` |
| 11 | [Parallel Streams](11-Parallel-Streams/) | Log Analyser | `parallelStream()`; when it helps and when it hurts |

---

## Functional Error Handling

Representing and composing failure as data instead of control flow.

| # | Module | Example | Core concept |
|---|--------|---------|--------------|
| 12 | [Optional](12-Optional/) | User Profile Lookup | `map`, `flatMap`, `orElse`, `filter`; making absence explicit |
| 13 | [Result Type](13-Result-Type/) | CSV Parser | `Result<T>` (Either); errors as values; monadic composition |

---

## Async

Non-blocking pipelines with functional composition.

| # | Module | Example | Core concept |
|---|--------|---------|--------------|
| 14 | [CompletableFuture](14-CompletableFuture/) | Async Dashboard | `supplyAsync`, `thenApply`, `thenCompose`, `allOf`, `exceptionally` |

---

## Recursion

Structural recursion and performance.

| # | Module | Example | Core concept |
|---|--------|---------|--------------|
| 15 | [Recursion & Memoization](15-Recursion-and-Memoization/) | File System | Recursive tree traversal; memoization for pure functions |

---

## Project conventions

- **Java 17** — records, sealed interfaces, switch expressions used throughout
- **JUnit Jupiter 5.10.2** — `@Test`, `@DisplayName`, `@BeforeEach`
- **Maven** — `mvn test` is the source of truth; no shared parent POM
- **No frameworks** — everything is standard library; no Spring, Guava, or Vavr

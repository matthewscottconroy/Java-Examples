# 04 — Builder: The Custom PC Builder

## The Story

You walk into a PC shop and order a computer. The clerk doesn't hand you a box
off the shelf — they **build it step by step**: choosing the CPU socket, seating
the RAM, mounting the storage, slotting in the GPU, routing the cables, installing
the cooler. At any step you can say "actually, give me 64 GB instead of 32," and
the build adapts without starting over.

A programmer without Builder would write:

```java
new Computer("i9-14900K", 32, 2000, "RTX 4090", "full-tower", true, true, false, "black");
```

What do those eight positional arguments mean? No idea. And if you want to leave
out the GPU, which `null` do you pass?

With Builder, the same construction is readable, self-documenting, and safe.

---

## The Problem It Solves

When a class has many optional parameters, telescoping constructors become
unreadable and error-prone. The Builder pattern:

- Constructs the object **incrementally** with named, self-documenting setters
- Separates **how** to build from **what** to build (subclassed builders pre-fill defaults)
- Validates inputs at construction time rather than at runtime
- Returns an **immutable** product — the `Computer` cannot be changed after `build()`

---

## Structure

```
ComputerBuilder         ← Builder (fluent setters, build())
  ├── GamingPCBuilder   ← Concrete Builder (pre-sets gaming defaults)
  └── OfficePCBuilder   ← Concrete Builder (pre-sets office defaults)

Computer                ← Product (package-private constructor — only Builder creates it)
```

*(No separate Director class — the client itself drives the build sequence,
which is the modern, idiomatic Builder.)*

---

## When to Use It

| Situation | Example |
|-----------|---------|
| Many optional constructor parameters | HTTP request builder, SQL query builder |
| Object construction needs validation | Must have CPU before building |
| Multiple representations from same process | Gaming vs. Office PC from shared builder |
| Immutable product with complex initialisation | `StringBuilder`, `Locale.Builder`, `URI.create()` |

---

## Project Layout

```
src/
├── main/java/com/patterns/builder/
│   ├── Computer.java         ← Product
│   ├── ComputerBuilder.java  ← Builder (fluent API)
│   ├── GamingPCBuilder.java  ← Concrete Builder
│   ├── OfficePCBuilder.java  ← Concrete Builder
│   └── Main.java             ← Demo
└── test/java/com/patterns/builder/
    └── ComputerBuilderTest.java
```

---

## Commands

```bash
mvn compile exec:java   # run the demo
mvn test                # run the unit tests
```

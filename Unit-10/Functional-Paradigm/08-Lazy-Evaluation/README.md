# 08 — Lazy Evaluation: The Configuration Loader

## The Story

At startup, your application could theoretically load the database URL, decrypt
the API key, and fetch the feature-flag map from a remote service — three slow,
expensive operations totalling 250ms. But most API endpoints only need one of
those three, and some need none.

Lazy evaluation defers the computation until the result is first requested:

```java
AppConfig config = new AppConfig();  // instant — nothing loaded

// 200ms later, a request arrives that needs the database:
String url = config.getDatabaseUrl();  // ← DB loaded NOW, on demand
```

A second call to `getDatabaseUrl()` returns the cached result instantly. The API
key and feature flags are never touched unless something asks for them.

---

## What Lazy Evaluation Is

In an **eagerly evaluated** language (Java's default), every expression is
evaluated when it appears. In **lazily evaluated** code, evaluation is deferred
until the value is actually needed.

In Java, laziness is explicit: you wrap the computation in a `Supplier<T>` and
evaluate it on demand.

```java
// Eager: runs immediately
String url = loadFromDatabase();

// Lazy: runs only when get() is called
Lazy<String> url = Lazy.of(() -> loadFromDatabase());
```

The `Lazy<T>` wrapper here memoizes the result — the supplier is called exactly
once, no matter how many times `get()` is called afterwards.

---

## Java's Built-in Lazy Tools

| Tool | How it's lazy |
|------|--------------|
| `Supplier<T>` | Defers computation; you call `get()` when ready |
| `Stream` | Intermediate operations build a pipeline; terminal operation triggers it |
| `Optional.or(Supplier)` | Fallback supplier only evaluated if Optional is empty |
| `CompletableFuture` | Computation runs asynchronously; result retrieved later |

Streams are the most visible example of built-in laziness: `filter` and `map`
don't process any elements until a terminal operation like `collect` or `findFirst`
triggers the pipeline.

---

## Commands

```bash
mvn compile exec:java
mvn test
```

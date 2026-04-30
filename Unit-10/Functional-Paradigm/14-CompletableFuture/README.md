# 14 — CompletableFuture: The Async Dashboard

## The Story

A user's dashboard shows current weather, portfolio value, and the top news
headline. Each piece of data comes from a different external API. Fetched
sequentially, the page takes 950ms to load (300 + 400 + 250ms). Fetched in
parallel, it takes ~400ms — the time of the slowest single call.

```java
CompletableFuture<String> weather   = RemoteServices.fetchWeather("London");
CompletableFuture<Double> portfolio = RemoteServices.fetchPortfolioValue("u001");
CompletableFuture<String> headline  = RemoteServices.fetchHeadline();

// All three launched; now wait for all three to finish
CompletableFuture.allOf(weather, portfolio, headline)
    .thenApply(v -> new DashboardData(
        weather.join(), portfolio.join(), headline.join()))
    .get();
```

The three calls run on the common ForkJoinPool while the calling thread is free.
`allOf` produces a `CompletableFuture<Void>` that completes when all three do.
`thenApply` assembles the results.

---

## The Key Methods

**Creating futures:**
```java
CompletableFuture.supplyAsync(() -> expensiveCall())   // runs on ForkJoinPool
CompletableFuture.completedFuture(value)               // already-complete future
```

**Transforming (non-blocking, returns a new future):**
```java
future.thenApply(v -> transform(v))       // like Stream.map
future.thenCompose(v -> anotherFuture(v)) // like Stream.flatMap; prevents nesting
future.thenCombine(other, (a, b) -> ...)  // combine two futures
```

**Waiting:**
```java
CompletableFuture.allOf(f1, f2, f3)       // complete when ALL complete
CompletableFuture.anyOf(f1, f2, f3)       // complete when ANY completes
future.get()                               // block until done (use sparingly)
future.join()                              // block, unchecked exception
```

**Error handling:**
```java
future.exceptionally(ex -> fallback)      // recover from failure
future.handle((value, ex) -> ...)         // handle both cases
```

---

## thenApply vs thenCompose

**`thenApply`** is for synchronous transformations — the function returns a plain
value:
```java
future.thenApply(String::toUpperCase)  // String → String
```

**`thenCompose`** is for asynchronous transformations — the function returns
another `CompletableFuture`. Without it, you'd get `CompletableFuture<CompletableFuture<T>>`:
```java
future.thenCompose(id -> fetchProfile(id))  // String → CompletableFuture<Profile>
                                            // result: CompletableFuture<Profile>
```

---

## Commands

```bash
mvn compile exec:java
mvn test
```

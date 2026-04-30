# 03 — Trampolining

Deep recursion without stack overflow. Instead of calling functions directly, return a thunk describing the next step; a tight driver loop executes the thunks one at a time.

## The problem

Java's call stack is finite (~8,000–10,000 frames by default). A naive recursive `factorial(100_000)` throws `StackOverflowError`. Trampolining converts unbounded recursion into a bounded loop.

## The trampoline

```java
sealed interface Bounce<A> permits Done, More {
    record Done<A>(A value)             implements Bounce<A> {}
    record More<A>(Supplier<Bounce<A>>) implements Bounce<A> {}

    static <A> A run(Bounce<A> b) {
        while (b instanceof More<A> m) b = m.thunk().get();
        return ((Done<A>) b).value();
    }
}
```

- `Done` — "I have the answer."
- `More` — "Here is a thunk; evaluate it to get the next step."

## Functions shown

| Function | Technique |
|----------|-----------|
| `factorial(n)` | Direct tail-call trampoline |
| `isEven(n)` / `isOdd(n)` | Mutually recursive, both trampolined |
| `fibonacci(n)` | Continuation-passing style (CPS) over the trampoline |
| `sumTo(n)` | Simple accumulator pattern |
| `factorialNaive(n)` | Shown to trigger `StackOverflowError` |

## Run

```bash
mvn exec:java   # demonstrates factorial(100 000) and fibonacci(30)
mvn test        # includes stack-safety test at depth 50 000
```

# 07 — Closures: The Counter Factory

## The Story

Your e-commerce system needs to assign unique order IDs starting at 1000,
invoice IDs starting at 9000 in steps of 10, and a separate sequence for
promotional codes. Each sequence is independent — advancing one must not affect
the others.

The natural solution is a factory that returns a *function with private state*:

```java
IntSupplier orderIds   = CounterFactory.counter(1000, 1);
IntSupplier invoiceIds = CounterFactory.counter(9000, 10);

orderIds.getAsInt();   // 1000
orderIds.getAsInt();   // 1001
invoiceIds.getAsInt(); // 9000  ← unaffected by orderIds
```

Each call to `counter` creates a new closure: a lambda that captures its own
`int[]` array. The closures are siblings — same code, separate state.

---

## What a Closure Is

A **closure** is a function that retains access to variables from the scope in
which it was created, even after that scope has exited.

```java
public static IntSupplier counter(int start, int step) {
    int[] state = { start };          // local variable in the factory
    return () -> {                    // lambda captures state[]
        int current = state[0];
        state[0] += step;
        return current;
    };
}
// The factory method returns. `state` is gone from the stack frame.
// But the lambda still holds a reference to it — that's the closure.
```

Java requires captured local variables to be *effectively final*. The array
trick works because the array reference is final; only its *contents* change.

---

## Closures vs Objects

A closure is functionally equivalent to a small object with a single method and
private fields. The choice is mostly stylistic:

| Closure | Object |
|---------|--------|
| `CounterFactory.counter(1000, 1)` | `new Counter(1000, 1)` |
| State captured implicitly | State stored in fields |
| Implements a functional interface | Implements a class |
| Lighter syntax for simple state machines | Better for complex state |

---

## Commands

```bash
mvn compile exec:java
mvn test
```

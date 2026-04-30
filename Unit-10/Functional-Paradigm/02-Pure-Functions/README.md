# 02 — Pure Functions and Immutability: The Unit Converter

## The Story

A unit converter is the ideal pure-function showcase because every conversion is
a mathematical fact. Metres to feet is always `value * 3.28084`. It doesn't
depend on the day, the user's session, a database, or any previous call. It
produces no side effects. Given the same number of metres, it always returns the
same number of feet.

That property — **same input, same output, no side effects** — is the definition
of a pure function. And the `Quantity` record that wraps each measurement is
immutable: conversion creates a new `Quantity`, never modifying the original.

---

## Pure Functions

A function is **pure** if:

1. Its return value depends *only* on its arguments — not on global state, the
   clock, a database, or any external resource.
2. It has *no side effects* — it does not write to disk, update a field, print
   to the console, or change anything visible outside itself.

```java
// Pure: same input → same output, no side effects
public static Quantity celsiusToFahrenheit(Quantity celsius) {
    return new Quantity(celsius.value() * 9.0 / 5.0 + 32.0, "°F");
}

// Impure: reads mutable state, result can differ on every call
public double currentTemperature() {
    return sensor.read(); // different every time
}
```

---

## Immutability

`Quantity` is a Java `record` — all fields are final and set at construction.
There are no setters. "Converting" a quantity means creating a new one:

```java
Quantity original = new Quantity(100.0, "°C");
Quantity converted = UnitConverter.celsiusToFahrenheit(original);
// original.value() is still 100.0
```

Immutability makes objects safe to share across threads, safe to use as map keys,
and easy to reason about — you never wonder "has this been mutated somewhere?"

---

## Why This Matters

Pure functions and immutable data are the foundation of the functional style:

| Property | Benefit |
|----------|---------|
| Deterministic | Trivially testable — no mocking, no setup |
| No side effects | Safe to run in parallel without synchronisation |
| Composable | Output of one can be fed directly into another |
| Cacheable | Result can be memoised because it never changes |

---

## Commands

```bash
mvn compile exec:java
mvn test
```

# 06 — Currying and Partial Application: The Shipping Calculator

## The Story

Shipping cost depends on three things: the carrier's rate, the parcel's weight,
and the distance. Written as one function that's correct but inflexible:

```java
double cost(double ratePerKgPerKm, double weightKg, double distanceKm) {
    return weightKg * distanceKm * ratePerKgPerKm;
}
```

Now your application needs to price dozens of shipments via the same carrier.
You're passing the rate every single call. Wouldn't it be cleaner to fix the
rate once and get back a function that just needs weight and distance?

```java
Function<Double, Function<Double, Double>> fedex = forCarrier(FEDEX_RATE);
fedex.apply(5.0).apply(300.0);  // weight, then distance
```

That's partial application. The carrier rate is baked in. The returned function
only needs the two remaining arguments.

---

## Currying

Currying transforms a multi-argument function into a chain of single-argument
functions. Named after mathematician Haskell Curry.

```
cost(rate, weight, distance)   ← three-argument function
↓ curried
rate → (weight → (distance → cost))   ← chain of unary functions
```

In Java:
```java
Function<Double, Function<Double, Double>> curriedCost(double rate) {
    return weight -> distance -> weight * distance * rate;
}
```

Each call returns the next function. You apply arguments one at a time.

---

## Partial Application

Partial application is broader: fix *some* arguments (not necessarily one at a
time) and return a function that accepts the rest.

```java
// Fix carrier and weight; distance is still free
Function<Double, Double> heavyParcel = forCarrierAndWeight(FEDEX_RATE, 15.0);
heavyParcel.apply(50.0);   // price to local destination
heavyParcel.apply(1200.0); // price to far destination
```

---

## Why This Matters

| Technique | Benefit |
|-----------|---------|
| Currying | Makes functions composable one argument at a time |
| Partial application | Specialises a general function for a specific context |
| Both | Eliminate repeated argument passing; create reusable pricers, formatters, or filters |

In Java's standard library, `Comparator.comparing(keyExtractor)` is partial
application — you fix the extraction logic and get back a `Comparator` ready
to receive the two objects it will compare.

---

## Commands

```bash
mvn compile exec:java
mvn test
```

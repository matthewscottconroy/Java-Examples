# 24 — Null Object: The Shopping Cart Discount

## The Story

An e-commerce site lets shoppers enter a promo code at checkout. If they have
one — say "SAVE20" — the system creates a `PercentageDiscount(20, "SAVE20")` and
applies it to the total. If they don't enter a code, most naive implementations
would store `null` and then check `if (discount != null) { total = discount.apply(total); }`
in the checkout flow.

That null-check spreads through the receipt printer, the analytics tracker, the
email confirmation, and every other place that touches the discount. Miss one and
you get a NullPointerException at 2 am on Black Friday.

The Null Object pattern eliminates the check entirely: when no code is entered,
the cart holds a `NullDiscount` — a real object that implements the same
`Discount` interface but simply returns the price unchanged. All callers treat it
identically to a real discount; none of them know the difference.

---

## The Problem It Solves

Null references require defensive checks at every call site. Those checks
duplicate policy ("what should happen when there's no discount?") and clutter
business logic. Forgetting one check causes a crash.

With Null Object, the "do nothing" behaviour is encapsulated in a real object.
Call sites are clean; the object model is complete.

---

## Structure

```
Discount            ← Interface (apply, description)
  ├── NullDiscount  ← Null Object (singleton; returns price unchanged)
  └── PercentageDiscount

ShoppingCart        ← Client; holds a Discount, never checks for null
```

---

## When to Use It

| Situation | Example |
|-----------|---------|
| Optional dependency that might not be set | Logger, discount, notification handler |
| Eliminate null checks from calling code | Default strategy, no-op event listener |
| Make "nothing" a first-class concept | Guest user, empty collection, null formatter |

Java's `Collections.emptyList()`, `Optional.empty()`, and `PrintStream.NULL_OUTPUT_STREAM`
are all Null Objects in the standard library.

---

## Commands

```bash
mvn compile exec:java   # run the demo
mvn test                # run the unit tests
```

# 09 — Decorator: The Coffee Shop

## The Story

You order a coffee. The barista starts with a shot of espresso ($2.00). You ask
for steamed milk — they wrap the espresso order with a milk layer ($0.50 more).
You want vanilla syrup — another layer ($0.75). You decide to splurge on whipped
cream — one more wrap ($0.60).

Your final drink: `Espresso, Steamed Milk, Vanilla Syrup, Whipped Cream — $3.85`.

The barista didn't make an `EspressoMilkVanillaWhipCoffee` class. They started
with a base drink and added capabilities one layer at a time. Each layer wraps
the previous one, adds its contribution, and delegates the rest.

This is the **Decorator** pattern.

---

## The Problem It Solves

You want to add behaviour to individual objects, not an entire class. Inheritance
would require a subclass for every possible combination — with four condiments that
is 2⁴ = 16 subclasses, and with ten condiments it becomes 1,024. Decorators solve
the combinatorial explosion: any condiment can wrap any other, in any order, any
number of times.

Java's own `BufferedInputStream`, `GZIPOutputStream`, and
`Collections.unmodifiableList()` are all decorators.

---

## Structure

```
Beverage             ← Component interface
  ├── Espresso       ← Concrete Component
  ├── HouseBlend     ← Concrete Component
  └── CondimentDecorator  ← Abstract Decorator (holds a Beverage reference)
        ├── SteamedMilk   ← Concrete Decorator
        ├── VanillaSyrup  ← Concrete Decorator
        ├── WhipCream     ← Concrete Decorator
        └── ExtraShot     ← Concrete Decorator
```

---

## When to Use It

| Situation | Example |
|-----------|---------|
| Add behaviour at runtime without subclassing | Coffee condiments, Java I/O streams |
| Combine behaviours in any order | Logging + caching + auth on an HTTP handler |
| Open/Closed principle: open for extension, closed for modification | Add a new condiment without touching existing code |

---

## Commands

```bash
mvn compile exec:java   # run the demo
mvn test                # run the unit tests
```

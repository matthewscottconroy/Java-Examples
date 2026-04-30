# 20 — Strategy: The Route Planner

## The Story

You open your navigation app and tap "Directions to the airport." A small toolbar
shows three buttons: **Fastest**, **Shortest**, **Eco**. Tap Fastest and the app
routes you onto the motorway — 42 km in 28 minutes. Tap Shortest and it threads
you through side streets — 32 km but 41 minutes. Tap Eco and it sends you along
the riverside parkway at a steady 60 km/h to minimise fuel burn.

The app's core logic — reading your GPS position, drawing the route on the map,
announcing turns — is identical in all three cases. What changes is the single
algorithm used to choose which roads to prefer.

That interchangeable algorithm is the **Strategy**.

---

## The Problem It Solves

Without Strategy you'd stuff every routing algorithm into one method:
`if (mode == FASTEST) … else if (mode == SHORTEST) …`. Adding "Avoid tolls" or
"Prefer scenic roads" means editing that method and re-testing everything.

With Strategy each algorithm lives in its own class implementing `RouteStrategy`.
The context (`NavigationApp`) holds a reference and delegates. Switching modes
at runtime means calling `setStrategy(new EcoFriendlyRouteStrategy())`. The app
is untouched; only the strategy changes.

---

## Structure

```
RouteStrategy           ← Strategy interface (calculate, name)
  ├── FastestRouteStrategy
  ├── ShortestRouteStrategy
  └── EcoFriendlyRouteStrategy

NavigationApp           ← Context (holds current strategy, delegates routing)
Route                   ← Value object returned by every strategy
```

---

## When to Use It

| Situation | Example |
|-----------|---------|
| Multiple algorithms for the same task | Sorting, compression, routing, pricing |
| Algorithm must be swappable at runtime | User-chosen sort order, payment method |
| Eliminate conditionals branching on "mode" | Shipping calculators, discount engines |

Java's `Comparator` is the most widely used Strategy in the standard library —
you pass a different one to `sort()` every time you need a different ordering.

---

## Commands

```bash
mvn compile exec:java   # run the demo
mvn test                # run the unit tests
```

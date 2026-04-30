# 03 — Abstract Factory: The Furniture Store

## The Story

You're furnishing an apartment and you've decided on a **Scandinavian** theme:
birch-wood chairs, pine-topped dining table, linen lampshade. Everything you
pick comes from the same factory, so every piece matches perfectly.

Your neighbor chose **Industrial**: steel-frame chairs, reclaimed-oak table with
cast-iron pipe legs, wire-cage lampshade. Their pieces also match — with each other.

What you cannot do is accidentally put a Scandinavian lamp next to an Industrial
table using the same factory — the factory ensures consistency. If you want to
redecorate, you swap to a different factory and every piece changes in concert.

This is the **Abstract Factory** pattern: an interface for creating **families** of
related objects, without specifying their concrete classes.

---

## The Problem It Solves

When you need to create sets of objects that belong together — and you need to
guarantee that mixing sets is impossible — you need a factory of factories.

- **Factory Method** creates one product. **Abstract Factory** creates a *family* of products.
- A single factory reference guarantees every object comes from the same family.
- Adding a new family (e.g., `ArtDecoFactory`) requires zero changes to client code.

---

## Structure

```
FurnitureFactory    ← Abstract Factory interface
  createChair()
  createTable()
  createLamp()
    ├── ScandinavianFactory ← Concrete Factory A
    └── IndustrialFactory   ← Concrete Factory B

Chair / Table / Lamp       ← Abstract Product interfaces
  ├── Scandinavian variants ← Concrete Products (family A)
  └── Industrial variants   ← Concrete Products (family B)

InteriorDesigner           ← Client (works only with abstractions)
```

---

## When to Use It

| Situation | Example |
|-----------|---------|
| System must be independent of how products are created | UI toolkit: cross-platform widgets |
| Products must be used together as a family | OS look-and-feel (buttons, scrollbars, menus all match) |
| You want to enforce product family consistency | Furniture styles, theme systems, cloud provider SDKs |

---

## Project Layout

```
src/
├── main/java/com/patterns/abstractfactory/
│   ├── FurnitureFactory.java    ← Abstract Factory
│   ├── Chair.java               ← Abstract Product
│   ├── Table.java               ← Abstract Product
│   ├── Lamp.java                ← Abstract Product
│   ├── ScandinavianFactory.java ← Concrete Factory (+ 3 inline products)
│   ├── IndustrialFactory.java   ← Concrete Factory (+ 3 inline products)
│   ├── InteriorDesigner.java    ← Client
│   └── Main.java                ← Demo
└── test/java/com/patterns/abstractfactory/
    └── FurnitureFactoryTest.java
```

---

## Commands

```bash
mvn compile exec:java   # run the demo
mvn test                # run the unit tests
```

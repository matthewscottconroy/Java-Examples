# 02 — Factory Method: The Pizza Franchise

## The Story

You walk into a pizza place and order a cheese pizza. At the New York location you
get a floppy, thin-crust slice you fold in half and eat walking down the street.
At the Chicago location, the same order produces a two-inch-deep butter-crust
casserole you eat with a knife and fork.

Same order. Completely different pizza.

The **PizzaStore** knows *how* to take orders — prepare, bake, cut, box — but it
defers the question of *which* pizza to create to the subclass (the franchise).
That deferral is the **Factory Method** pattern.

---

## The Problem It Solves

You need to create objects, but the exact type depends on a decision you can't
make in the base class — only a subclass (or plug-in, or configuration) can make
it. You want:

- The creation code in one place
- Subclasses to control *what* gets created
- Client code to remain ignorant of concrete types

Without Factory Method, `orderPizza()` would need to say `new NYCheesePizza()` or
`new ChicagoCheesePizza()` — tightly coupling the order workflow to specific classes.
Adding a third franchise would mean editing `orderPizza()`.

With Factory Method, adding a `CaliforniaStylePizzaStore` requires zero changes to
`PizzaStore` or any client code.

---

## Structure

```
PizzaStore              ← Abstract Creator
  └── orderPizza()      ← template: uses createPizza(), never names concrete type
  └── createPizza()     ← the factory method (abstract — subclasses override it)
      |
      ├── NYStylePizzaStore      ← Concrete Creator → produces NY-style pizzas
      └── ChicagoStylePizzaStore ← Concrete Creator → produces Chicago deep-dish

Pizza                   ← Abstract Product
  ├── NYCheesePizza     (inner)  ← Concrete Product
  ├── ChicagoVeggiePizza (inner) ← Concrete Product
  └── …
```

---

## When to Use It

| Situation | Example |
|-----------|---------|
| Subclass decides which object to create | Franchise decides its pizza style |
| Adding new variants without touching existing code | New franchise = new class only |
| Framework defines a process; plugins supply the objects | JUnit `@Extension`, Spring beans |

---

## Project Layout

```
src/
├── main/java/com/patterns/factory/
│   ├── Pizza.java                 ← Abstract Product
│   ├── PizzaStore.java            ← Abstract Creator (factory method: createPizza)
│   ├── NYStylePizzaStore.java     ← Concrete Creator + NY products
│   ├── ChicagoStylePizzaStore.java← Concrete Creator + Chicago products
│   └── Main.java                  ← Demo
└── test/java/com/patterns/factory/
    └── PizzaStoreTest.java        ← Tests
```

---

## Commands

```bash
mvn compile exec:java   # run the demo
mvn test                # run the unit tests
```

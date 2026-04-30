# 03 — Higher-Order Functions: The Payroll Processor

## The Story

HR needs a weekly report: which full-time engineers earn over $90k? Next week
it's: what's the total payroll after a 10% raise for everyone in Marketing?
The week after: list every employee's name alphabetically.

Each question involves the same operations — filter, transform, aggregate — but
with different criteria. A function that takes *another function as an argument*
lets you write `filter`, `map`, and `sum` once and pass the varying logic in.

---

## Higher-Order Functions

A **higher-order function** is a function that does at least one of:

- **Takes a function as an argument** — `filter(employees, predicate)`
- **Returns a function as its result** — `raiseBy(factor)` returns a transformer

This is the beating heart of functional programming. Instead of writing
`filterFullTimeEngineers()`, `filterPartTimeMarketing()`, etc., you write
`filter()` once and pass in the criterion.

Java's standard library uses this everywhere:
- `List.sort(Comparator)` — takes a function
- `Optional.map(Function)` — takes a function
- `Stream.filter(Predicate)` — takes a function

---

## The Three Workhorses

**`filter`** — keep only elements matching a predicate:
```java
Predicate<Employee> isEng = e -> e.department().equals("Engineering");
List<Employee> engineers = Payroll.filter(staff, isEng);
```

**`map`** — transform every element:
```java
List<String> names = Payroll.map(staff, Employee::name);
List<Employee> raised = Payroll.map(staff, Payroll.raiseBy(1.10));
```

**`reduce / sum`** — collapse a list to a single value:
```java
double total = Payroll.sum(staff, e -> e.salaryUsd());
```

These three, chained together, can express almost any data transformation.

---

## Returning Functions

`Payroll.raiseBy(factor)` doesn't apply a raise — it *builds* a raiser:

```java
Function<Employee, Employee> tenPercent = Payroll.raiseBy(1.10);
Function<Employee, Employee> twentyPercent = Payroll.raiseBy(1.20);
```

The factor is baked in by the closure. You can pass these around, store them,
compose them, or apply them later. This is the functional alternative to a
parameterised object.

---

## Commands

```bash
mvn compile exec:java
mvn test
```

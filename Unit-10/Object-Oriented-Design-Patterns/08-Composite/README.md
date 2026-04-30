# 08 — Composite: The Company Org Chart

## The Story

The CFO wants to know the total payroll. She calls her assistant:
"What does it cost to run Engineering?" The engineering VP asks each team lead.
Each team lead asks each engineer. Each engineer reports their own salary.
The answers bubble back up: team totals, division totals, company total.

The CFO doesn't distinguish between "asking a person" and "asking a team." The
request propagates down the tree and a number comes back up — the same operation
at every level.

This is the **Composite** pattern: treat individual objects and compositions
of objects **uniformly**, through a common interface.

---

## The Problem It Solves

Without Composite, client code must distinguish between leaves (individuals) and
composites (teams): `if (emp instanceof Team) { sum all members } else { return salary }`.

With Composite, the distinction disappears. `getTotalSalary()` on a `Staff`
returns one number. On a `Team` it recursively sums all members. The CFO calls
the same method, gets the right answer, and never touches a type-check.

---

## Structure

```
Employee (Component interface)
  ├── Staff  (Leaf)      — one salary
  └── Team   (Composite) — contains 0..* Employee nodes, delegates operations
```

---

## When to Use It

| Situation | Example |
|-----------|---------|
| Part-whole hierarchies | File system (files + folders), org charts, BOMs |
| Clients should treat individuals and groups identically | Render a shape vs. render a group of shapes |
| Operations must propagate through a tree | Tax, raise, count, search |

---

## Commands

```bash
mvn compile exec:java   # run the demo
mvn test                # run the unit tests
```

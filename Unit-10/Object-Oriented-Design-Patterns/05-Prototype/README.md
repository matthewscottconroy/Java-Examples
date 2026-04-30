# 05 — Prototype: The Legal Contract Template

## The Story

A law firm handles dozens of NDAs every month. Every NDA contains the same 15
boilerplate clauses, the same governing-law provision, the same remedies language.
The only things that change are the party names and the effective date.

The junior associate does not draft each NDA from scratch — they open the master
template, **make a copy**, fill in the names, and send it out. The master template
sits untouched in the shared drive, ready for the next engagement.

This is the **Prototype** pattern: instead of building an object from zero,
you **clone an existing one** and customise the copy. The original is preserved.

---

## The Problem It Solves

Construction can be expensive (loading defaults, setting up complex state).
If many instances need to start life nearly identical, it is wasteful to
re-build each one from scratch. The Prototype pattern:

- Copies an existing object in one `clone()` call
- Guarantees a **deep copy** — the clone's internal data is independent
- Hides the concrete class — callers work with the abstract type
- Works hand-in-hand with a **registry** (the `ContractLibrary`) that stores
  named templates and hands out clones on demand

---

## Structure

```
Contract            ← Prototype (implements Cloneable, provides clone())
ContractLibrary     ← Prototype Registry (stores and retrieves named clones)
```

---

## When to Use It

| Situation | Example |
|-----------|---------|
| Object creation is expensive | Database-backed entity loaded from a query |
| Many similar objects start from a common base | Contract templates, UI widget presets |
| The exact class of the object shouldn't matter to the client | Plugin system creating copies |
| System must be independent of how products are created | Document editors, game object spawning |

---

## Project Layout

```
src/
├── main/java/com/patterns/prototype/
│   ├── Contract.java         ← Prototype (Cloneable, clone(), customise())
│   ├── ContractLibrary.java  ← Prototype Registry
│   └── Main.java             ← Demo
└── test/java/com/patterns/prototype/
    └── ContractTest.java
```

---

## Commands

```bash
mvn compile exec:java   # run the demo
mvn test                # run the unit tests
```

# 01 — Singleton: The Stadium Scoreboard

## The Story

Every section of the arena looks up at **one** scoreboard. The TV broadcast booth,
the stats-tracking tablet on the bench, the PA announcer's laptop — they all read
from the same scoreboard. There is no second scoreboard. If the stats app records
a three-pointer, the PA announcer's display shows it immediately, because they hold
a reference to the **same object**.

This is the **Singleton** pattern: a class that guarantees only **one instance**
ever exists, and provides a single global access point to that instance.

---

## The Problem It Solves

Some resources are inherently singular: a configuration registry, a log file, a
print spooler, a hardware driver, a game scoreboard. Creating multiple instances
would cause conflicts, inconsistent state, or wasted resources. Singleton ensures
all code shares one instance, created exactly once on first use.

---

## Structure

```
Scoreboard
  ├── private Scoreboard()      ← blocks external instantiation
  ├── static getInstance()      ← the one global access point
  └── homeScore, awayScore …   ← state shared across all callers
```

### Implementation — initialization-on-demand holder

```java
private static final class Holder {
    private static final Scoreboard INSTANCE = new Scoreboard();
}

public static Scoreboard getInstance() {
    return Holder.INSTANCE;
}
```

`Holder` is not loaded until `getInstance()` is first called. The JVM's
class-loading guarantee creates exactly one instance — no `synchronized` keyword,
no double-checked locking, zero overhead on every subsequent call.

---

## When to Use It

| Situation | Notes |
|-----------|-------|
| Application-wide configuration | Single config object read by many modules |
| Logging / audit trail | All components write to the same log |
| Thread pool / connection pool | Expensive to create; must be shared |
| Hardware driver | One driver per physical device |

## When to Be Careful

- Singletons are **global mutable state** — they make tests harder (one test's
  mutations leak into the next). The test class uses `startGame()` in `@BeforeEach`
  to reset state between tests.
- In real applications, prefer **dependency injection** of a single, shared
  instance over hidden Singletons, to keep code testable.

---

## Project Layout

```
src/
├── main/java/com/patterns/singleton/
│   ├── Scoreboard.java   ← the Singleton (holder idiom, thread-safe)
│   └── Main.java         ← demo: broadcast booth, stats app, PA share one board
└── test/java/com/patterns/singleton/
    └── OracleTest.java   ← identity, shared state, score accumulation
```

---

## Commands

```bash
mvn compile exec:java   # run the demo
mvn test                # run the unit tests
```

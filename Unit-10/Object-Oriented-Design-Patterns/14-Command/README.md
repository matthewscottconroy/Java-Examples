# 14 — Command: The Restaurant Order System

## The Story

A guest at table 7 orders grilled salmon and Caesar salad. At table 8: ribeye
and mushroom risotto. The waiter **writes each order on a ticket** — a command
object — and holds them until the section is complete.

Before heading to the kitchen, the guest at table 8 changes their mind: "Cancel
the risotto." The waiter tears up that ticket. Nothing has been cooked yet.

At the pass, the waiter drops all remaining tickets through the window. The
kitchen reads each ticket and starts cooking. The waiter is long gone.

This is the **Command** pattern: encapsulate a request as an object, separating
the issuer (table) from the executor (kitchen). Commands can be queued, logged,
and cancelled before execution.

---

## The Problem It Solves

Without Command, the waiter would need direct access to every kitchen station
("add salmon to the fish station's queue, remove the risotto from the pasta
queue…"). The dining room and kitchen would be tightly coupled.

With Command:
- The waiter never knows what "cooking" means — they just hold tickets
- Cancellation is trivial: drop the ticket before handing it over
- The kitchen log is automatic: every ticket that was executed is recorded
- The system supports undo: re-fire a cancel command to restore a removed item

---

## Structure

```
Order (Command)
  └── DishOrder       ← Concrete Command (holds Kitchen receiver, dish, table)

Kitchen               ← Receiver (knows how to cook)
Waiter                ← Invoker (queues and submits Orders)
```

---

## When to Use It

| Situation | Example |
|-----------|---------|
| Queue or schedule requests | Job queue, print spooler |
| Support undo/redo | Text editor, drawing application |
| Parameterise UI actions | "Save", "Print" buttons hold Command objects |
| Transactional operations | Database transaction = sequence of commands |

---

## Commands

```bash
mvn compile exec:java   # run the demo
mvn test                # run the unit tests
```

# 16 — Mediator: The Air Traffic Control Tower

## The Story

Forty aircraft are circling an airport at the same time. Without a control tower,
each pilot would need to radio every other pilot to coordinate: "I'm landing on
runway 28L, are you clear?" — 40 × 39 / 2 = 780 simultaneous conversations.
The result would be chaos and collision.

The control **tower** is the mediator. No pilot talks to another pilot directly.
Every request — takeoff clearance, landing clearance, taxi instructions — goes
to the tower. The tower has the complete picture, enforces the rules, and issues
clearances in a safe order.

Remove the tower and you remove all coordination. The pilots haven't changed; the
protocol between them just collapsed.

---

## The Problem It Solves

When many objects need to coordinate, direct peer-to-peer references create an
O(n²) web of dependencies. Adding a new aircraft to a direct-communication model
requires every other aircraft to know about it. With a Mediator:

- Colleagues hold **one** reference (to the tower) instead of n−1
- All coordination logic lives in **one** place (easier to change rules)
- Adding a new colleague type requires zero changes to other colleagues

---

## Structure

```
AirTrafficControl  ← Mediator interface
ControlTower       ← Concrete Mediator (runway state, traffic log)
Aircraft           ← Colleague (holds a ref to the tower only)
```

---

## When to Use It

| Situation | Example |
|-----------|---------|
| Many-to-many coupling between objects | Chat room (messages go to server, not direct) |
| Coordination logic scattered across many classes | UI components updating each other |
| Adding new participant types without changing existing ones | New aircraft type, new chat client |

---

## Commands

```bash
mvn compile exec:java   # run the demo
mvn test                # run the unit tests
```

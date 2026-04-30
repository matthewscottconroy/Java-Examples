# 06 — Adapter: The Legacy Payment System

## The Story

Your new e-commerce platform expects every payment vendor to implement three
clean methods: `charge(customerId, cents)`, `refund(ref, cents)`, and
`getStatus(ref)`. Clear, modern, testable.

But your company has been using the same payment vendor for twenty years. Their
system has `debitAccount(accountNumber, dollarsFloat)`, `creditAccount(code, dollarsFloat)`,
and `queryTransaction(code)` — which returns an integer (1 = settled, 2 = pending,
3 = failed). The vendor is out of business. The code cannot change.

An **Adapter** wraps the old system and exposes the modern interface. Your
checkout service never knows the old system exists.

---

## The Problem It Solves

Two classes need to work together but their interfaces are incompatible. Rather
than changing either class (which may be impossible — the adaptee is third-party,
or locked), you write a thin wrapper that translates one interface into the other.

Common real-world uses: reading XML with a JSON-expecting parser, using an old
sorting library with a new comparison interface, wrapping a third-party SDK.

---

## Structure

```
PaymentProcessor        ← Target (what the client expects)
LegacyPaymentSystem     ← Adaptee (existing incompatible class)
LegacyPaymentAdapter    ← Adapter (wraps Adaptee, implements Target)
CheckoutService         ← Client (works only with Target)
```

---

## When to Use It

| Situation | Example |
|-----------|---------|
| Third-party library has a different interface | Wrapping an SDK |
| Legacy system cannot be modified | Payment vendor, OS API |
| You want a unified interface over several incompatible implementations | Multiple logger backends |

---

## Project Layout

```
src/
├── main/java/com/patterns/adapter/
│   ├── PaymentProcessor.java      ← Target interface
│   ├── LegacyPaymentSystem.java   ← Adaptee
│   ├── LegacyPaymentAdapter.java  ← Adapter
│   ├── CheckoutService.java       ← Client
│   └── Main.java                  ← Demo
└── test/java/com/patterns/adapter/
    └── AdapterTest.java
```

---

## Commands

```bash
mvn compile exec:java   # run the demo
mvn test                # run the unit tests
```

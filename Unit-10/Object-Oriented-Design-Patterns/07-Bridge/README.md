# 07 — Bridge: The Notification Service

## The Story

Your application sends two kinds of notifications: **Urgent Alerts** and **Newsletters**.
It can deliver them over two channels: **Email** and **SMS**.

Without Bridge you would create four concrete classes:
`UrgentAlertEmail`, `UrgentAlertSMS`, `NewsletterEmail`, `NewsletterSMS`.

Add a third notification type (Promotional) and you need three more classes.
Add Slack as a channel and you need two more. The class count grows at O(m×n).

With Bridge, you have **2 + 2 = 4 classes** instead of 2×2 = 4, and every new
notification type or new channel adds just **1** class, not m or n.

---

## The Problem It Solves

When a class can vary in two independent dimensions, inheritance alone causes
a combinatorial explosion. The Bridge separates the two dimensions into their
own hierarchies and connects them via composition.

> **Rule of thumb:** if you find yourself writing class names that are combinations
> of two concepts (e.g., `SomethingEmail`, `SomethingSlack`), you probably need a Bridge.

---

## Structure

```
Notification (Abstraction)           MessageChannel (Implementor)
  ├── UrgentAlert                       ├── EmailChannel
  └── Newsletter                        └── SmsChannel
       ↓ delegates to ↓
          channel.send(...)
```

The abstraction holds a reference to the implementor. New abstraction types
and new implementors can be added without affecting each other.

---

## When to Use It

| Situation | Example |
|-----------|---------|
| Two independent dimensions of variation | Notification type × delivery channel |
| Avoid m×n class explosion | Shapes × rendering APIs (OpenGL, DirectX) |
| Switch implementation at runtime | Swap Email for SMS without changing business logic |

---

## Commands

```bash
mvn compile exec:java   # run the demo
mvn test                # run the unit tests
```

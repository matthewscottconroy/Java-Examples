# 01 — Lambda Expressions: The Tax Rule Engine

## The Story

A tax engine has to apply a progressive rate schedule: 10% on the first $11,600,
12% on the next $35,550, 22% on the next $53,375, and so on. Each bracket is a
rule. Rules get added and removed when tax law changes. Sometimes a special
surtax needs to be bolted on for one year.

Before lambdas, each rule required an anonymous class:

```java
rules.add(new TaxRule() {
    @Override
    public double calculate(double income) {
        return income * 0.038;
    }
});
```

With a lambda, a rule is just a function — a value you can store, pass, and
compose:

```java
rules.add(income -> income > 200_000 ? (income - 200_000) * 0.038 : 0.0);
```

The lambda *is* the object. You never write the class.

---

## What a Lambda Is

A lambda expression is a concise way to create an instance of a **functional
interface** — an interface with exactly one abstract method. The compiler infers
which interface you mean from context.

```
(parameter list) -> expression
(parameter list) -> { statements; }
```

Java's `@FunctionalInterface` annotation enforces the one-method contract at
compile time, but it isn't required — any single-method interface works.

---

## Why This Matters

Before Java 8, behaviour was passed around by wrapping it in objects (Strategy,
Command, Comparator). That pattern still works, but lambdas eliminate the
ceremony. Functions become **first-class values**: stored in variables, collected
in lists, passed as arguments, returned from methods.

The tax engine stores rules in a `List<TaxRule>`. Adding a Medicare surtax,
a phase-out deduction, or a flat credit is one line. No subclass, no file, no
registration step.

---

## Key Concepts

| Concept | Shown here |
|---------|-----------|
| `@FunctionalInterface` | `TaxRule` — compiler-enforced single-method contract |
| Lambda as a value | Rules stored in `List<TaxRule>` |
| Lambda capturing context | `TaxBracket.toRule()` captures `floor`, `ceiling`, `rate` |
| Inline lambda | Medicare surtax added as `income -> ...` directly in `Main` |

---

## Commands

```bash
mvn compile exec:java
mvn test
```

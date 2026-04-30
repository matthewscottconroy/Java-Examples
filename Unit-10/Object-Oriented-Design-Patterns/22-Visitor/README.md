# 22 — Visitor: The Tax Return Processor

## The Story

At the end of the year you gather every source of income in one folder:
a W-2 from your employer, a 1099-DIV from your brokerage, a Schedule D for
shares you sold, and a Schedule E for the rental property you own. Each one is
taxed differently — salary at the ordinary income rate, qualified dividends at
15%, long-term capital gains at 15%, rental income on the net amount after
deducting maintenance costs.

Now imagine you need to run *two* operations on that same folder: first calculate
the federal tax, then print a plain-English summary for your accountant. The
income documents don't change between operations. Only what you *do* with them
changes.

This is the **Visitor** pattern: separate an algorithm from the object structure
it operates on, so you can add new operations without modifying the objects.

---

## The Problem It Solves

Without Visitor, you'd add `calculateFederalTax()` and `printSummary()` methods
directly to `SalaryIncome`, `DividendIncome`, etc. Every new operation
(state tax? AMT?) adds methods to every income class. The income classes, which
model *what money is*, get buried under methods that model *what to do with it*.

With Visitor, the income classes each have one stable `accept(TaxVisitor)` method.
New operations are new Visitor implementations — the income classes never change.

---

## Double Dispatch

Java's overloading is resolved at compile time, but we need the right `visit`
overload chosen at runtime based on the *actual* income type. The trick:
`income.accept(visitor)` calls `visitor.visit(this)` — the income object
passes *itself* as the concrete type, forcing the correct overload.

---

## Structure

```
TaxVisitor              ← Visitor interface (visit per income type)
  ├── FederalTaxVisitor
  └── TaxSummaryVisitor

IncomeSource            ← Element interface (accept)
  ├── SalaryIncome
  ├── DividendIncome
  ├── CapitalGainIncome
  └── RentalIncome
```

---

## When to Use It

| Situation | Example |
|-----------|---------|
| Many operations on a stable type hierarchy | AST transformations (compile, lint, format, emit) |
| Avoid polluting data classes with operations | Tax rules, rendering pipelines |
| Operations added frequently; types added rarely | Document export (PDF, HTML, Markdown) |

Compiler frameworks (ANTLR, JavaCC) generate Visitor skeletons for abstract
syntax trees precisely because parsing is stable but passes (type-check, optimise,
emit code) accumulate over time.

---

## Commands

```bash
mvn compile exec:java   # run the demo
mvn test                # run the unit tests
```

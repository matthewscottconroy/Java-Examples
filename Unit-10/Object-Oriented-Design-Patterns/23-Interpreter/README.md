# 23 — Interpreter: The Boolean Search Engine

## The Story

You type `java AND (patterns OR concurrency) AND NOT threads` into a technical
document search box. The engine needs to turn that text into a tree of operations,
evaluate each one against its index, and return the intersection of matching
documents.

Each node in the tree — `AND`, `OR`, `NOT`, or a plain keyword — is one grammar
rule. Each rule knows how to evaluate itself. A compound query is just a tree
of rule objects calling each other recursively.

This is the **Interpreter** pattern: represent a grammar as a class hierarchy and
interpret sentences by walking the object tree.

---

## The Problem It Solves

Without Interpreter, boolean search logic lives in one giant method with nested
conditionals. Composing `(A OR B) AND NOT C` requires careful parsing and
bookkeeping. Adding a new operator (phrase matching, proximity search) means
editing that method.

With Interpreter, each grammar rule is its own class:
`TermExpression`, `AndExpression`, `OrExpression`, `NotExpression`. Building
`(A OR B) AND NOT C` is just nesting constructors. Adding phrase matching is
adding a new class — everything else stays put.

---

## Structure

```
SearchExpression        ← Abstract expression interface (evaluate)
  ├── TermExpression    ← Terminal: single keyword lookup
  ├── AndExpression     ← Non-terminal: intersection of two sub-expressions
  ├── OrExpression      ← Non-terminal: union of two sub-expressions
  └── NotExpression     ← Non-terminal: complement of one sub-expression

InvertedIndex           ← Context: maps terms → document IDs
```

---

## When to Use It

| Situation | Example |
|-----------|---------|
| Recursive grammar that benefits from an OO tree | SQL AST, boolean queries, rule engines |
| Grammar is small and rarely changes | Configuration DSLs, math expression parsers |
| Composability matters more than raw speed | Cron expression parsing, filter pipelines |

The Interpreter pattern is the foundation of every expression-tree-based
evaluator: ANTLR-generated parse trees, JPA Criteria API, Spring's
`ExpressionParser`, and spreadsheet formula engines all use this approach.

---

## Commands

```bash
mvn compile exec:java   # run the demo
mvn test                # run the unit tests
```

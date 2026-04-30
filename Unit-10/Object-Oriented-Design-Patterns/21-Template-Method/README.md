# 21 — Template Method: The Report Generator

## The Story

Every quarter, the finance team needs the same payroll data in three formats:
a **CSV** to import into the accounting system, an **HTML** table for the
intranet portal, and a **Markdown** file that drops straight into the Confluence
wiki.

The overall process is identical every time:

1. Write a title / header
2. Write column names
3. Write each data row
4. Write a footer

But *how* you write a header in CSV (`# Q1 Payroll`), HTML (`<h1>Q1 Payroll</h1>`),
and Markdown (`# Q1 Payroll\n\n`) is completely different.

The Template Method locks the skeleton in an abstract base class and lets each
concrete subclass fill in the format-specific details.

---

## The Problem It Solves

Without Template Method you'd either duplicate the outer loop in every generator
or thread a massive `switch (format)` through the entire algorithm. Both options
scatter the shared logic across the codebase and make adding a new format (say,
PDF) require touching existing code.

With Template Method the outer loop lives in `ReportGenerator.generate()` and is
declared `final`. Subclasses provide only what varies. Adding PDF means adding
one new class; nothing else changes.

---

## Structure

```
ReportGenerator             ← Abstract class
  │  generate()             ← Template method (final)
  │  writeHeader()          ← Abstract hook
  │  writeColumnHeaders()   ← Abstract hook
  │  writeRow()             ← Abstract hook
  │  writeFooter()          ← Optional hook (default: no-op)
  │
  ├── CsvReportGenerator
  ├── HtmlReportGenerator
  └── MarkdownReportGenerator
```

---

## When to Use It

| Situation | Example |
|-----------|---------|
| Invariant algorithm with variant steps | Report generation, data parsing pipelines |
| Avoid duplicating the overall structure | Test fixtures, build scripts, migration scripts |
| Optional hook steps | `writeFooter()` — subclasses override only if needed |

Java's `AbstractList`, `HttpServlet.service()`, and JUnit's `@BeforeEach` /
`@AfterEach` lifecycle are all Template Method in practice.

---

## Commands

```bash
mvn compile exec:java   # run the demo
mvn test                # run the unit tests
```

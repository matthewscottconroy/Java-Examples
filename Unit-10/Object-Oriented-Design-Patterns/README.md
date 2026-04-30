# Object-Oriented Design Patterns

A self-contained collection of 26 Maven/Java 17 projects, each illustrating one
design pattern with a concrete, real-world example, full unit tests, and a
README explaining the pattern's purpose and when to reach for it.

---

## Why study design patterns?

Every programmer eventually hits the same wall. You've learned the syntax, you
can write working code, and then you're asked to build something that actually
has to *last* — code that another developer will modify six months from now,
that needs a new feature added without breaking existing ones, that has to be
tested without spinning up an entire production environment.

And you find yourself asking: is there a right way to structure this?

Design patterns are the collected answers to that question. Not theory invented
in a vacuum, but solutions distilled from decades of programmers independently
arriving at the same structures when solving the same kinds of problems. The Gang
of Four book (*Design Patterns*, Gamma, Helm, Johnson, Vlissides, 1994) named and
catalogued 23 of them. Practitioners have added more since.

Studying them gives you three things that experience alone takes much longer to
develop.

---

## What you actually gain

### 1. A vocabulary for design

The hardest part of software design isn't figuring out the solution — it's
communicating it. When you say "let's use an Observer here," every engineer in
the room instantly understands the structure: there's a subject, there are
subscribers, and the subject doesn't know who's listening. You don't have to draw
a diagram or write three paragraphs of explanation.

Patterns are a shared vocabulary. They let teams discuss architecture at the
right level of abstraction without talking past each other.

### 2. The ability to see the shape of a problem

Patterns teach you to recognise recurring *problem shapes*, not just recurring
*solutions*. Once you've seen Chain of Responsibility in an expense approval
workflow, you recognise the same shape in HTTP middleware, logging filters, and
compiler diagnostic pipelines — even though the surface code looks completely
different. Your eye gets trained to see structure.

This is what separates a programmer who can implement a feature from one who can
*design* a system.

### 3. A starting point, not a straitjacket

The GoF patterns were documented, not invented. You'll find yourself reinventing
them anyway, because they're the natural shapes that emerge when you're trying
to satisfy competing constraints — loose coupling, testability, extensibility,
clarity. Knowing them in advance means you arrive at the good structure sooner,
with less fumbling, and you know the trade-offs before you commit.

---

## The forces that patterns resolve

Every pattern in this collection is a response to one or more of the same
underlying tensions in software design:

**Coupling** — when one class knows too much about another, changes ripple
unpredictably. Adapter, Bridge, Facade, Mediator, and Observer all exist to
reduce or redirect coupling.

**Rigidity** — when adding a new feature requires editing many existing classes,
the design has become rigid. Factory Method, Strategy, Command, and Visitor let
you add new behaviour by adding new code rather than modifying old code (the
Open/Closed Principle).

**Duplication** — when the same algorithm skeleton appears in multiple places
with small variations, Template Method gives it one home and makes the variation
explicit.

**Brittleness** — when a null reference can crash the system at a call site far
from where the decision was made, Null Object eliminates the gap between "no
value" and "a value that does nothing."

**Untestability** — when a class creates its own dependencies, you can't test it
without a real database or network. Dependency Injection and Repository make the
seam explicit so you can substitute test doubles.

---

## How to use this collection

Each project is a runnable, testable Java program built around a single pattern.
The README in each directory explains:

- A concrete story that makes the pattern tangible
- The specific problem it solves
- The structure (which classes play which roles)
- When to reach for it in real code

Read the story first. If you can picture it, the code will make sense
immediately. If the code still feels abstract after you've read it, re-read the
story and ask yourself: which class is responsible for what, and why is that
boundary drawn where it is?

The patterns are grouped below by category, but within a category the order
matters less than you might expect. You don't have to read them in sequence.
Start with the one whose example sounds most familiar to you.

---

## Running any project

```bash
cd <pattern-directory>
mvn compile exec:java   # run the demo
mvn test                # run the unit tests
```

---

## Creational Patterns

These patterns deal with *how objects are created*. They give you control over
which class gets instantiated, how it's configured, and whether you get a fresh
object or a shared one — without coupling the code that needs the object to the
code that builds it.

| # | Pattern | Example | Core idea |
|---|---------|---------|-----------|
| 01 | [Singleton](01-Singleton/) | Stadium Scoreboard | One shared instance; all callers see the same state |
| 02 | [Factory Method](02-Factory-Method/) | Pizza Store | Subclasses decide which product to instantiate |
| 03 | [Abstract Factory](03-Abstract-Factory/) | Furniture Store | Create families of related objects without naming their concrete classes |
| 04 | [Builder](04-Builder/) | Custom PC Builder | Construct a complex object step by step with a fluent API |
| 05 | [Prototype](05-Prototype/) | Legal Contract Template | Clone an existing object rather than constructing from scratch |

---

## Structural Patterns

These patterns deal with *how classes and objects are composed* into larger
structures. They let you build flexible relationships between objects —
translating interfaces, wrapping behaviour, or simplifying complex hierarchies —
without baking those relationships into inheritance chains.

| # | Pattern | Example | Core idea |
|---|---------|---------|-----------|
| 06 | [Adapter](06-Adapter/) | Legacy Payment System | Translate one interface into another so incompatible types can collaborate |
| 07 | [Bridge](07-Bridge/) | Notification Service | Separate abstraction from implementation so both can vary independently |
| 08 | [Composite](08-Composite/) | Company Org Chart | Treat individual objects and groups through the same interface |
| 09 | [Decorator](09-Decorator/) | Coffee Shop | Add behaviour by wrapping, not subclassing |
| 10 | [Facade](10-Facade/) | Home Theater | Provide one simple interface to a complex subsystem |
| 11 | [Flyweight](11-Flyweight/) | Word Processor | Share fine-grained objects to avoid the cost of millions of similar instances |
| 12 | [Proxy](12-Proxy/) | Caching Database Proxy | Intercept access to an object for caching, logging, or access control |

---

## Behavioural Patterns

These patterns deal with *how objects communicate and distribute responsibility*.
They give you clean ways to express algorithms, define how information flows
through a system, and separate the code that does something from the code that
decides what gets done.

| # | Pattern | Example | Core idea |
|---|---------|---------|-----------|
| 13 | [Chain of Responsibility](13-Chain-of-Responsibility/) | Expense Approval | Pass a request along a chain of handlers until one accepts it |
| 14 | [Command](14-Command/) | Restaurant Order | Encapsulate a request as an object; enables undo, queuing, and logging |
| 15 | [Iterator](15-Iterator/) | Social Media Feed | Traverse a collection sequentially without exposing its internal structure |
| 16 | [Mediator](16-Mediator/) | Air Traffic Control | Centralise communication between objects so they don't reference each other directly |
| 17 | [Memento](17-Memento/) | Text Editor Undo | Snapshot an object's state and restore it later without violating encapsulation |
| 18 | [Observer](18-Observer/) | Weather Station | Automatically notify all interested parties when one object's state changes |
| 19 | [State](19-State/) | Vending Machine | Let an object's behaviour change completely when its internal state changes |
| 20 | [Strategy](20-Strategy/) | Route Planner | Define a family of algorithms and make them interchangeable at runtime |
| 21 | [Template Method](21-Template-Method/) | Report Generator | Define an algorithm's skeleton in a base class; subclasses fill in the steps |
| 22 | [Visitor](22-Visitor/) | Tax Return Processor | Add new operations to an object hierarchy without modifying its classes |
| 23 | [Interpreter](23-Interpreter/) | Boolean Search Engine | Represent a grammar as a class hierarchy and evaluate sentences by walking it |

---

## Modern Patterns

These three patterns aren't in the original GoF catalogue, but are now considered
essential to well-structured professional Java code. Each addresses a pain point
that became acutely visible as software systems grew larger and testing became a
first-class concern.

| # | Pattern | Example | Core idea |
|---|---------|---------|-----------|
| 24 | [Null Object](24-Null-Object/) | Shopping Cart Discount | Replace `null` with a do-nothing object and eliminate null-checks at every call site |
| 25 | [Dependency Injection](25-Dependency-Injection/) | User Registration Service | Receive dependencies from outside rather than creating them; enables testing and flexibility |
| 26 | [Repository](26-Repository/) | Book Inventory | Hide storage details behind a collection-like interface so domain logic never touches SQL |

---

## A note on when *not* to use a pattern

Knowing patterns is not a licence to use them everywhere. Every pattern adds
indirection. Indirection adds cognitive load. A simple problem solved with a
simple class is better than a simple problem buried inside an unnecessary
Abstract Factory.

The signal to reach for a pattern is a specific *pain*: you're editing a class
every time you add a new subscriber; you can't test a service without a live
database; two class hierarchies keep growing in lockstep. When you feel that
pain, you'll know exactly which pattern you need — and why it exists.

---

## Project conventions

- **Java 17** — records, switch expressions, and sealed types used where natural
- **JUnit Jupiter 5.10.2** — `@Test`, `@DisplayName`, `@BeforeEach`
- **Maven** — `mvn test` is the source of truth; IDE classpath warnings are noise
- **No magic** — every project is self-contained with its own `pom.xml`

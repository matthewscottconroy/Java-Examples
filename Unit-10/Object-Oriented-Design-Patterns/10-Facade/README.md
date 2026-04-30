# 10 — Facade: The Home Theater System

## The Story

You want to watch a movie. Your home theater has a Blu-ray player, an AV receiver,
a smart TV, and a smart lighting system — four separate remote controls.

The correct startup sequence: power on the receiver first, then set the volume,
enable surround sound, power on the TV, switch to HDMI 1, power on the Blu-ray,
load the disc, and dim the lights to 10%. Nine steps, four remotes.

The **Facade** collapses all nine steps into one:

```java
theater.watchMovie("2001: A Space Odyssey");
```

The facade knows the correct order. You don't have to.

---

## The Problem It Solves

Complex subsystems accumulate many classes and interactions. Client code that
needs to orchestrate them becomes brittle, hard to read, and tightly coupled to
implementation details.

The Facade provides a **simple interface** over a complex subsystem. It doesn't
add new functionality — it organises existing functionality for common use cases.

- Clients who need simple access use the facade.
- Clients who need fine-grained control can still access subsystem classes directly.

---

## Structure

```
HomeTheaterFacade      ← Facade
  watchMovie(title)    ← simple high-level operation
  endMovie()

Subsystem classes (clients can use these directly if needed):
  Television           SoundReceiver
  BluRayPlayer         SmartLights
```

---

## When to Use It

| Situation | Example |
|-----------|---------|
| Provide a simple interface to a complex library | `slf4j` over Log4j/Logback |
| Decouple client code from subsystem internals | Service layer over repositories and external APIs |
| Define entry points into each subsystem layer | Application layer in a layered architecture |

---

## Commands

```bash
mvn compile exec:java   # run the demo
mvn test                # run the unit tests
```

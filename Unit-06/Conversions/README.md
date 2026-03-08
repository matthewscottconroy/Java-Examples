# Conversions — A Unit Conversion Library

This project demonstrates package organization through a small, practical
unit conversion library. It covers weights, distances, and time.

---

## Package Overview

```
com.examples.math      — Unit types and quantity arithmetic
com.examples.format    — Formatted output for the terminal
com.examples.app       — Main program: everyday conversion examples
```

### `com.examples.math`

| Class | Purpose |
|---|---|
| `Unit` | Interface every unit type implements: symbol, full name, and conversion to/from a base unit. |
| `WeightUnit` | Enum of weight units: grams, kilograms, ounces, pounds, metric tons. |
| `DistanceUnit` | Enum of distance units: meters, kilometers, centimeters, feet, miles. |
| `TimeUnit` | Enum of time units: seconds, minutes, hours, days. |
| `Quantity<U>` | A value paired with its unit. Converts to any compatible unit via a common base. |

### `com.examples.format`

| Class | Purpose |
|---|---|
| `ConversionPrinter` | Renders a quantity and its conversions as a labeled terminal table. |

### `com.examples.app`

| Class | Purpose |
|---|---|
| `Main` | Three everyday examples: a marathon, a bag of flour, and a work day. |

---

## The Design

Every unit implements the `Unit` interface, which requires two methods:

```java
double toBase(double value);    // convert from this unit → base unit
double fromBase(double value);  // convert from base unit → this unit
```

Converting between any two units goes through the base:

```
A → base → B
```

This means adding a new unit only requires knowing its relationship to
the base — no conversion table between every pair needed.

`Quantity<U extends Unit>` is generic, so the compiler prevents nonsensical
conversions like turning kilograms into hours.

---

## Building and Running

```bash
# Compile and run
mvn compile exec:java -Dexec.mainClass="com.examples.app.Main"

# Run all tests
mvn test

# Package into a runnable jar
mvn package
java -jar target/conversions-1.0-SNAPSHOT.jar

# Generate Javadocs (output: target/site/apidocs/index.html)
mvn javadoc:javadoc
```

---

## Tests

Unit tests are in `src/test/java/com/examples/math/` and use **JUnit 5 (Jupiter)**.

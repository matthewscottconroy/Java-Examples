# Buffon's Needle — Monte Carlo Estimation of π

An interactive simulation of the classical Buffon's Needle experiment: needles are
dropped at random onto a ruled floor and the proportion that cross a line is used
to estimate π via a simple probability formula.

---

## Prerequisites

| Tool  | Minimum | Check |
|-------|---------|-------|
| JDK   | 17      | `java -version` |
| Maven | 3.6     | `mvn -version` |

---

## Quick Start

```bash
# Compile
mvn compile

# Launch the simulator
mvn exec:java

# Run all unit tests
mvn test

# Package a runnable jar
mvn package
java -jar target/buffon-1.0-SNAPSHOT.jar

# Generate JavaDoc
mvn javadoc:javadoc
# open target/site/apidocs/index.html
```

---

## Using the Simulator

The window shows a ruled floor (dark horizontal lines) onto which needles are
dropped one frame at a time.  Needles that cross a line are drawn in **red**;
needles that miss are drawn in **blue**.

| Control | Effect |
|---------|--------|
| Line spacing slider | Distance d between floor lines (pixels) |
| Needle length slider | Needle length L (pixels) |
| Needles / frame | Drop rate: 1 up to 1 000 needles per 16 ms frame |
| Pause / Resume | Freeze the simulation without losing statistics |
| Burst (100k) | Instantly drop 100 000 more needles |
| Reset | Clear everything and start over |

The **HUD** (upper-left) shows the current estimate π̂, its error relative to
the true value of π, the total drop count N, crossing count C, and the
observed and theoretical crossing probabilities.

The **convergence graph** (lower-right) plots the running π̂ estimate over
time.  The yellow dashed line marks the true value of π.  You can watch the
noisy early estimate gradually settle as N grows.

---

## Theory

### The Setup

Imagine a large floor ruled with parallel lines spaced **d** units apart.
Drop a needle of length **L ≤ d** at random: the centre lands at a uniformly
random position and the needle points in a uniformly random direction.

The needle **crosses** a line when its vertical extent (the projection of its
length onto the direction perpendicular to the lines) is large enough to bridge
a gap between two adjacent lines.

### The Probability

For a needle at angle θ from horizontal, the vertical half-span is
`(L/2)|sin θ|`.  The needle crosses a line if and only if this half-span
reaches a line, which happens when the distance from the centre to the nearest
line is less than the half-span.

Integrating over all uniformly random angles θ ∈ [0, π) and uniformly random
centre positions gives:

```
P(cross) = 2L / (π d)          (valid for L ≤ d)
```

This remarkable formula contains π.  Rearranging to solve for it:

```
π = 2L / (d · P(cross))
```

In practice we estimate P(cross) ≈ C/N (crossings over total drops), giving
the Monte Carlo estimator:

```
π̂ = 2LN / (dC)
```

### Why the Formula Involves π

The factor π enters through the integration of `|sin θ|` over [0, π):

```
(1/π) ∫₀^π |sin θ| dθ = 2/π
```

This is the expected value of |sin θ| for a uniform random angle.  The
crossing probability is therefore the product of this expected value, L/d, and
the appropriate normalisation — yielding 2L/(πd).

### The Long-Needle Case (L > d)

When L > d the needle can straddle multiple lines.  The simple formula
P = 2L/(πd) no longer holds; the correct expression is:

```
P = (2L/πd) − (2/πd)(√(L² − d²) − d·arccos(d/L))     (L > d)
```

This case is more complex and is flagged with a warning in the UI.  The
crossing *detection* in the code works correctly for any L, but the π
estimator should only be used when L ≤ d.

### Convergence Rate

The error in π̂ decreases as **1/√N**.  To gain one extra decimal digit of
accuracy, you need roughly **100× more trials**.  After one million drops you
can typically expect about two correct decimal places, depending on luck.
This is characteristic of all simple Monte Carlo estimators.

### Historical Note

Georges-Louis Leclerc, Comte de Buffon, posed the needle problem in 1733 and
solved it in 1777.  It is one of the oldest problems in geometric probability
and the first practical demonstration that a physical stochastic process could
produce an estimate of the mathematical constant π.

The Italian mathematician Mario Lazzarini famously claimed a result of
3.1415929 from 3408 throws in 1901 — implausibly accurate, and widely
suspected to be a case of stopping the experiment when the count happened to
be close to the expected value.

### Buffon's Noodle

A fascinating generalisation (Buffon's Noodle, due to Joseph Barbier, 1860):
if the straight needle is replaced by **any curve of length L**, the crossing
probability is *still* P = 2L/(πd), regardless of the shape.  The result
follows from the linearity of expectation: the expected number of crossings of
a curve is the sum of the expected crossings of each infinitesimal straight
segment, and each segment contributes 2 ds/(πd).  The shape cancels out
entirely.  A circle of circumference πd has P = 1 (it always crosses exactly
two lines, on average two per drop — try it!).

---

## Numerical Details

The crossing test uses exact integer floor arithmetic to avoid floating-point
boundary ambiguities:

```java
double halfSpan = 0.5 * needleLength * Math.abs(Math.sin(angle));
double yMin     = cy - halfSpan;
double yMax     = cy + halfSpan;
boolean crosses = (long) Math.floor(yMin / d) != (long) Math.floor(yMax / d);
```

Both endpoints are divided by the line spacing and their integer parts
compared.  If they differ, at least one grid line lies strictly between
them, confirming a crossing.  This is O(1) per needle and handles all
angles and lengths correctly without case analysis.

---

## Project Structure

```
buffon/
├── pom.xml
├── README.md
└── src/
    ├── main/java/com/buffon/
    │   ├── Main.java                      — Entry point
    │   ├── model/
    │   │   ├── Needle.java                — Immutable record: centre, angle, length, crosses flag
    │   │   └── BuffonExperiment.java      — Drop logic, crossing detection, π estimator, history
    │   └── ui/
    │       ├── MainFrame.java             — JFrame wrapper
    │       ├── SimulationPanel.java       — Animated canvas: floor, needles, HUD, convergence graph
    │       └── ControlPanel.java          — Sliders, speed selector, burst, reset
    └── test/java/com/buffon/
        └── model/
            ├── NeedleTest.java            — Endpoint geometry, length consistency
            └── BuffonExperimentTest.java  — Crossing detection, π convergence, reset, bounds
```

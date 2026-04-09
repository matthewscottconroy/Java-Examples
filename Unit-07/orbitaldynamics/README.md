# Orbital Dynamics — N-Body Simulator & Analytical Solver

Two companion applications for studying gravitational dynamics:

1. **N-Body Simulator** (`mvn exec:java`) — Drop masses onto a 2D canvas; Newtonian gravity drives realistic orbits computed by a 4th-order Runge-Kutta integrator.
2. **Two-Body Analytical Solver** (`mvn exec:java -Ptwobody`) — Set initial conditions; orbital elements are derived from the integrals of motion and positions are computed exactly from Kepler's equations at any point in time.

---

## Build, Run, Test & Docs

**Prerequisites**

| Tool | Minimum version | Check with |
|------|----------------|------------|
| JDK  | 17             | `java -version` |
| Maven | 3.6           | `mvn -version` |

```bash
# Compile
mvn compile

# Run N-Body Simulator
mvn exec:java

# Run Two-Body Analytical Solver
mvn exec:java@twobody

# Run the unit test suite
mvn test

# Package a self-contained JAR (N-Body Simulator as main class)
mvn package
java -jar target/orbitaldynamics-1.0-SNAPSHOT.jar

# Generate Javadoc API documentation
mvn javadoc:javadoc
# then open target/site/apidocs/index.html
```

---

## N-Body Simulator: How It Works

### Placing Bodies

Activate **Place Body** in the toolbar, then click and drag on the canvas. The body's radius and density are set by the toolbar spinners; mass is `ρ·π·r²` (area-based, treating each circle as a 2D disk of uniform density).

When you release the mouse, the velocity at the moment of release becomes the body's initial velocity. The angular velocity ω is derived from the rotational component of your drag motion relative to the body center: `ω = (offset × v) / (|offset|² / 2)`.

### Physics: RK4 Integration

The state of the system at any instant is a flat vector `[x₀, y₀, vx₀, vy₀, x₁, y₁, ...]`. Each frame, the engine advances this state by `dt` using the classic 4th-order Runge-Kutta method:

```
k₁ = f(s)
k₂ = f(s + dt/2 · k₁)
k₃ = f(s + dt/2 · k₂)
k₄ = f(s + dt   · k₃)
s' = s + dt/6 · (k₁ + 2k₂ + 2k₃ + k₄)
```

The derivative function `f` computes the gravitational acceleration each body experiences from every other:

```
aᵢ = G · Σ_{j≠i}  mⱼ · (rⱼ − rᵢ) / (|rⱼ − rᵢ|² + ε²)^(3/2)
```

The softening parameter ε² = 25 prevents numerical blow-up when two bodies pass very close together. Without softening, the force grows without bound as r → 0, which causes the integrator to take astronomically small steps or simply produce nonsense.

### Collisions

After each RK4 step, overlapping body pairs receive an elastic impulse: momentum and kinetic energy are both conserved. A positional correction separates the bodies to prevent sinking.

---

## Two-Body Analytical Solver: How It Works

### The Two-Body Reduction

When two bodies interact only with each other, the problem reduces exactly to a one-body problem: the relative position vector `r = r₂ − r₁` obeys

```
r̈ = −μ/|r|³ · r,    μ = G(m₁ + m₂)
```

This equation has two conserved quantities — specific angular momentum `h = r × ṙ` and specific orbital energy `E = v²/2 − μ/r` — that together determine the orbit's shape and size completely. Kepler proved in 1609 that the solution is always a conic section.

### Orbital Elements

From the initial conditions (position and velocity of each body), the solver computes:

| Symbol | Meaning |
|--------|---------|
| e      | Eccentricity — shape of the conic (0 = circle, <1 = ellipse, 1 = parabola, >1 = hyperbola) |
| a      | Semi-major axis — linear scale of the orbit |
| ω      | Argument of periapsis — orientation in the plane |
| T      | Period — time for one complete orbit (bound orbits only) |
| h      | Specific angular momentum — always conserved |
| E      | Specific orbital energy — always conserved |

### Kepler's Equation

To find the position at time `t`, the solver maps elapsed time to an "anomaly" angle through Kepler's equation. For an ellipse:

```
M = E − e·sin(E)          (Kepler's equation)
M = n·(t − t₀)           (mean anomaly, linear in time)
n = 2π/T                  (mean motion)
```

There is no closed-form inverse of Kepler's equation; the solver uses Newton–Raphson iteration, which converges in 3–5 iterations for typical eccentricities. Parabolas use Barker's equation (solved analytically via Cardano's formula). Hyperbolas use the hyperbolic analogue `M_h = e·sinh(F) − F`, also solved by Newton–Raphson.

---

## Simulation vs. Analytical: A Comparative View

The two apps present the same physical system through fundamentally different computational lenses. The contrast is instructive.

### What the N-Body Simulator does well

- Handles **any number of bodies**. Three or more bodies have no known closed-form solution (the three-body problem is generally chaotic).
- Models **collisions, perturbations, and emergent dynamics** that pure orbital mechanics cannot predict.
- Is straightforward to extend: drag a body mid-orbit, add a new one, change G — the simulation adapts immediately.
- **Intuitive and tactile**: you build physical intuition by experimenting directly.

### What the N-Body Simulator cannot guarantee

- **Energy conservation**. RK4 has O(dt⁴) local truncation error. Energy drifts — slowly for small dt, rapidly for large dt or close encounters. The softening parameter helps stability but introduces a fictional force at small distances that violates the true potential.
- **Long-term accuracy**. Symplectic integrators (Verlet, leapfrog) are specifically designed to conserve symplectic structure and provide much better long-term energy behavior; RK4 does not.
- **Exact repeatability**. Floating-point arithmetic is not associative; the same physical setup run at different speeds or step sizes may produce visibly different trajectories over time.

### What the Analytical Solver does well

- Produces **exact positions** (to floating-point precision) at any time without accumulating integration error.
- Makes conserved quantities — energy, angular momentum, eccentricity — explicit by construction. These never drift.
- Can jump to any time instantly: `positionAtTime(t)` doesn't need to integrate from `t=0`; it solves Kepler's equation directly.
- Makes the **geometry of orbits** transparent: you can read off the orbit type, period, periapsis, and apoapsis directly from the initial conditions.

### What the Analytical Solver cannot do

- Handle more than two bodies. The three-body problem has no general closed-form solution.
- Model **non-gravitational forces** (drag, radiation pressure, rocket thrust) without reformulating the equations entirely.
- Represent **collisions, mergers, or dynamical interactions** between multiple objects.
- The two-body reduction assumes an **isolated system** — no external perturbations.

---

## On Models, Reality, and the Nature of Physical Knowledge

The two applications are not just different algorithms. They represent different *epistemological stances* toward physical reality, and examining them together raises questions that go deeper than computational efficiency.

### What is a model?

A mathematical model is a formal system — a set of symbols, rules, and operations — that produces predictions about measurable outcomes. The gravitational model `F = Gm₁m₂/r²` is not gravity. It is a representation of a pattern we observe gravity to follow, expressed in a language (differential equations) that allows us to extrapolate that pattern forward in time.

This distinction matters. The map is not the territory. Newton's law of gravitation was accurate enough to predict planetary positions for centuries, discover Neptune by perturbation analysis, and guide spacecraft through the solar system. It was also *wrong* in a precise technical sense: general relativity predicts measurably different outcomes near massive objects and at high velocities, and those predictions match experiment better. Newton's model is not a failed approximation that was eventually corrected. It is an extraordinarily successful model that has a known, bounded domain of applicability.

### What makes a model good?

A good model is not simply one that matches data. A model with enough free parameters can fit any finite dataset — this is overfitting, and it generalizes poorly. The virtues of a model are:

**Accuracy**: does it agree with measurement within the precision we care about? For orbital mechanics of small bodies at low velocities, both Newtonian gravity and general relativity give the same answer to many significant figures. Newtonian gravity is not inaccurate for this domain; it is accurate enough that the difference is immeasurable.

**Economy**: does it use the simplest machinery necessary? Newton's three laws of motion and universal gravitation replace hundreds of Kepler's empirical rules with four axioms. General relativity explains gravity, inertia, and the geometry of spacetime in a single field equation. Economy is not mere aesthetics — simpler models have fewer hidden assumptions and are easier to falsify.

**Generativity**: does it produce predictions beyond the data used to build it? Newton didn't fit his laws to planetary orbits and then apply them to planetary orbits; he derived planetary orbits from a more fundamental principle. The model predicted phenomena (stellar aberration, tidal locking, the precession of equinoxes) that weren't part of its original domain.

**Computability**: can we actually derive predictions from it? The three-body problem is exactly solvable in principle — there is a unique trajectory determined by initial conditions — but the general solution cannot be written in closed form. The N-body simulator sacrifices exactness for computability. Kepler's equation is the boundary: it is not solvable in closed form (no finite formula exists for its inverse), but it is solvable by convergent iteration. This is a practical limitation of our mathematical language, not a flaw in the physics.

### Can a model be said to be real?

This is the deeper question. There are two coherent positions:

**Instrumentalism**: models are tools for making predictions. A model is useful or not useful, accurate or inaccurate — but it doesn't make sense to call it *real* or *true*. The electron is not a particle, not a wave; it is a calculational device that, when operated correctly, produces numbers that match experiment. The wavefunction is not a physical object; it is a probability amplitude. On this view, asking whether Newton's gravitational field or Einstein's curved spacetime is "really" what's happening is a category error. Both are models. Models are not real; phenomena are real.

**Scientific realism**: our best theories don't just predict observations; they describe structures that exist. When general relativity predicts that spacetime curvature causes clocks to tick differently in gravitational fields, and GPS satellites confirm this with millimeter precision, the agreement is too systematic and detailed to be coincidence. The model isn't merely correlating data — it is tracking something real. The discovery of gravitational waves, predicted by general relativity a century before they were detected, is the kind of evidence that is very hard to explain without supposing the model describes something that is actually there.

Neither position is easily dismissed. Instrumentalism is intellectually honest about the gap between symbols and phenomena. Realism is hard to maintain coherently — we don't know what it would mean for a differential equation to "really" describe spacetime — but it captures the intuition that science makes genuine discoveries, not merely useful fictions.

### The simulation as a lesson in model limits

The N-body simulator makes these issues concrete. Run the binary star preset and observe: the stars orbit stably for a while, then energy drifts, the orbit precesses, and eventually the dynamics diverge from what we expect. Nothing is wrong with the physics. The error is in the model's implementation — specifically, in the choice to use a non-symplectic integrator with a fixed time step that doesn't adapt to close encounters.

The analytical solver has no such drift. It is not "more accurate" in any physically meaningful sense — both apps model the same idealized point masses in flat Euclidean space with instantaneous action-at-a-distance. The analytical solver is more accurate in a computational sense: it exploits the exact integrals of motion to bypass the accumulation of numerical error.

But notice what the analytical solver cannot model: any physical effect that breaks the two-body integrability. A third body, a body with nonzero size, a perturbation from a distant mass, radiation pressure, the Yarkovsky effect (radiation recoil from an asymmetric body), tidal deformation — all of these shatter the closed-form solution. The "exact" model is only exact for an idealization that does not exist in nature.

This is not a failure. It is the normal condition of physical modeling. We choose the model that captures the phenomena we care about, with the precision we need, within the computational budget we have. The art of physics — and of simulation — is knowing which idealizations are safe to make, and being honest about the ones that are not.

### On the limits of all mathematical models of the physical world

Gödel showed that sufficiently expressive formal systems cannot prove all truths about themselves. This is usually held to be irrelevant to physics, since physics deals with finite measurement and finite precision. But there is a related, more practical limit: no finite mathematical model can fully specify a physical system.

A simulation of two gravitating bodies captures their center-of-mass motion. It does not model their internal structure, which affects their quadrupole gravitational field. It does not model tidal forces, which cause internal dissipation. It does not model the quantum mechanics of their constituent atoms, which (at negligible levels) affects their trajectories. In principle, to model any physical system to arbitrary precision, you would need to model the entire universe — because every mass in the universe exerts a gravitational influence on every other.

We do not do this. We cannot do this. Instead, we draw a boundary around the system we care about, declare everything outside that boundary to be either negligible or a fixed external parameter, and work within that boundary. This is not a deficiency in our models; it is what models *are*. A model that included everything would not be a model. It would just be the universe itself, which is unhelpfully large.

The lesson is not nihilism. The lesson is humility with confidence: humility about what our models capture, and confidence that within their domain, they work. Newton's law is valid. Kepler's equations are exact for two-body gravity. The RK4 simulator, run at small enough step sizes, will closely track the true trajectory for a long time. These are achievements. They are not perfect, and they are not final — but they are real knowledge.

---

## Structure

```
orbitaldynamics/
├── src/main/java/com/orbitaldynamics/
│   ├── math/
│   │   └── Vector2D.java              — Immutable 2D vector
│   ├── sim/
│   │   ├── Main.java                  — Entry point (N-body simulator)
│   │   ├── body/
│   │   │   ├── OrbitalBody.java       — Body state: position, velocity, spin, trail
│   │   │   └── BodyTexture.java       — Procedural surface texture
│   │   ├── physics/
│   │   │   └── PhysicsEngine.java     — RK4 integrator, collision resolution
│   │   └── ui/
│   │       ├── Camera.java            — Pan/zoom transform
│   │       ├── Sidebar.java           — Body info panel
│   │       ├── SimulationPanel.java   — Canvas, mouse interaction, game loop
│   │       └── SimulationFrame.java   — Main window and toolbar
│   └── twobody/
│       ├── TwoBodyMain.java           — Entry point (analytical solver)
│       ├── OrbitalElements.java       — Kepler orbital elements record
│       ├── TwoBodySolver.java         — Analytical position/velocity from Kepler's equations
│       └── ui/
│           ├── ParameterPanel.java    — Input spinners and elements display
│           ├── TwoBodyPanel.java      — Orbit canvas
│           └── TwoBodyFrame.java      — Main window and playback toolbar
└── pom.xml
```

# Pendulum Dynamics — Simple, Double & Newton's Cradle

Three interactive simulations that explore classical pendulum mechanics from
textbook SHM through chaotic dynamics and elastic collision propagation.

---

## Prerequisites

| Tool | Minimum version | Check |
|------|----------------|-------|
| JDK  | 17             | `java -version` |
| Maven | 3.6           | `mvn -version` |

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
java -jar target/pendulums-1.0-SNAPSHOT.jar

# Generate JavaDoc
mvn javadoc:javadoc
# open target/site/apidocs/index.html
```

---

## The Three Simulations

### Tab 1 — Simple Pendulum

A single point mass (bob) on a massless rigid rod pivoted at the top.

**Controls**

| Control | Effect |
|---------|--------|
| Click & drag | Set release angle |
| Length slider | Rod length in pixels |
| Gravity slider | Gravitational acceleration |
| Damping slider | Air-resistance coefficient |
| Angle slider | Precise initial angle |
| Euler / RK4 toggle | Switch integration method |
| Trail checkbox | Show / hide position history |
| Ghost checkbox | Overlay the small-angle SHM approximation |
| Reset | Re-release from current angle with zero velocity |

The **green dashed ghost** pendulum uses the small-angle approximation
(pure SHM with T = 2π√(L/g)).  At large amplitudes the true pendulum's
period is noticeably longer, and the two trajectories drift apart — this
difference is anharmonicity.

---

### Tab 2 — Double Pendulum

Two rods connected end-to-end, each carrying a bob.  The equations of
motion are derived from the Lagrangian and are deeply non-linear.

**Controls**

| Control | Effect |
|---------|--------|
| θ₁, θ₂ sliders | Release angles for each arm |
| L₁, L₂ sliders | Rod lengths |
| m₁, m₂ sliders | Bob masses (relative) |
| Gravity slider | Gravitational acceleration |
| Shadow checkbox | Overlay a second pendulum offset by 10⁻⁴ rad |
| Trail checkbox | Show the end-bob position history |
| Reset | Restart from current slider values |

Enable the **Shadow** pendulum to watch exponential divergence: two
trajectories indistinguishable at launch will occupy entirely different
regions of the phase space within seconds.  This is the hallmark of
deterministic chaos.

---

### Tab 3 — Newton's Cradle

A row of identical pendulums whose bobs hang touching at rest.

**Controls**

| Control | Effect |
|---------|--------|
| Balls spinner | Total number of balls (3 – 9) |
| Lift slider | How many balls to pull back |
| Angle slider | Pull-back angle |
| Restitution slider | Coefficient of restitution (0 = inelastic, 1 = elastic) |
| Gravity slider | Gravitational acceleration |
| Reset | Lift the balls and release |
| Stop All | Set all velocities to zero |

Drag any ball leftward and release it to set the initial condition
interactively.  Try pulling back 2 or 3 balls and observe that exactly 2
or 3 balls swing out the other side — a consequence of requiring both
momentum and energy to be simultaneously conserved.

---

## Theory

### Simple Pendulum

The exact equation of motion for a pendulum of length L in gravitational
field g is

```
d²θ/dt² = −(g/L) sin θ
```

For small angles, sin θ ≈ θ, giving simple harmonic motion with angular
frequency ω₀ = √(g/L) and period T₀ = 2π√(L/g).

The true period for large amplitude θ₀ is given by an elliptic integral:

```
T = 4√(L/g) · K(sin(θ₀/2))
```

where K is the complete elliptic integral of the first kind.  At θ₀ = 90°
the true period is about 18% longer than T₀; at 170° it is roughly 3×
longer.  The simulation integrates the exact (non-linear) ODE, so you can
observe this stretching of the period directly.

Adding a linear damping term −c · dθ/dt models light air resistance.
With damping, the pendulum spirals toward the stable equilibrium θ = 0.

### Double Pendulum and Chaos

Attaching a second pendulum to the first creates a system with two degrees
of freedom.  The Lagrangian L = T − V yields the coupled equations (with
δ = θ₁ − θ₂, M = m₁ + m₂):

```
α₁ = [−g(2m₁+m₂)sinθ₁ − m₂g sin(θ₁−2θ₂) − 2sinδ·m₂(ω₂²L₂ + ω₁²L₁cosδ)]
       / [L₁(2m₁+m₂ − m₂cos2δ)]

α₂ = [2sinδ·(ω₁²L₁(m₁+m₂) + g(m₁+m₂)cosθ₁ + m₂ω₂²L₂cosδ)]
       / [L₂(2m₁+m₂ − m₂cos2δ)]
```

For large initial angles the system is **chaotic**: nearby initial
conditions diverge exponentially with a positive Lyapunov exponent.  No
matter how precisely you specify the starting position, the trajectory
becomes unpredictable on a timescale of a few seconds.  Chaos is not
randomness — the system is completely deterministic — it is extreme
sensitivity to initial conditions.

The shadow pendulum in the double-pendulum tab provides a concrete
demonstration.  It starts with θ₁ offset by 10⁻⁴ rad (about 0.006°) and
its divergence from the primary pendulum is visible within tens of seconds.

### Newton's Cradle

Each ball is a simple pendulum.  Balls collide elastically (or nearly so).
For two equal-mass balls with coefficient of restitution e, the impulse
per unit mass along the contact normal is

```
J = (1 + e) · v_rel / 2
```

where v_rel is the relative approach speed along the contact normal.  At
e = 1 (perfectly elastic), momentum and kinetic energy are both conserved
and the balls exactly swap velocities.  When one moving ball hits a chain
of n stationary balls, the only solution satisfying both conservation laws
is: the first (n − m) balls in the chain stop and the last m balls carry
the full momentum.  This is why k balls in → k balls out.

At e < 1 energy is lost at each impact.  The cradle damps out much faster
and you can observe the progressive energy loss in the HUD display.

---

## Numerical Methods

The ODE systems above are integrated numerically.  The choice of method
affects both accuracy and energy behaviour.

### Euler Method (first-order)

```
y_{n+1} = y_n + dt · f(y_n)
```

Uses only the derivative at the start of each interval.  Local error is
O(dt²); global error over a fixed time span is O(dt).  For the pendulum,
Euler consistently *adds* a small amount of energy every step, so the
amplitude grows slowly without bound.  Useful as a comparison to show
why higher-order methods matter.

### Runge-Kutta 4 (fourth-order, RK4)

```
k₁ = f(yₙ)
k₂ = f(yₙ + dt·k₁/2)
k₃ = f(yₙ + dt·k₂/2)
k₄ = f(yₙ + dt·k₃)
y_{n+1} = yₙ + (dt/6)(k₁ + 2k₂ + 2k₃ + k₄)
```

Four derivative evaluations per step, combined with Simpson-rule weights.
Local error is O(dt⁵); global error is O(dt⁴).  For a typical pendulum
step size of dt = 0.002 s, RK4 conserves energy to within < 0.01% over
many periods.  The default method for all simulations here.

### Symplectic / Leapfrog (not implemented — worth knowing)

Leapfrog and the closely related Störmer-Verlet method update position and
velocity in a staggered ("leapfrog") fashion:

```
v_{n+½} = v_n + (dt/2) · a(x_n)
x_{n+1} = x_n + dt · v_{n+½}
v_{n+1} = v_{n+½} + (dt/2) · a(x_{n+1})
```

Symplectic integrators conserve a *modified* Hamiltonian (slightly
different from the true Hamiltonian) exactly.  They neither gain nor lose
energy over the long run — they just oscillate around the true value.
This makes them ideal for orbital mechanics or any simulation requiring
millions of steps without energy drift.  RK4 is more accurate per step
but can drift over very long runs; Leapfrog is lower order but stays
bounded forever.

### Choosing a Step Size

The physics substep in each panel is dt = 0.016 s / N_SUBSTEPS.  With
STEPS = 8 (simple pendulum, Newton's cradle) or STEPS = 12 (double
pendulum) that gives dt ≈ 0.001–0.002 s, well within the stability region
for both Euler and RK4 at these frequencies.

---

## Project Structure

```
pendulums/
├── pom.xml
├── README.md
└── src/
    ├── main/java/com/pendulums/
    │   ├── Main.java                         — Entry point
    │   ├── physics/
    │   │   └── Integrator.java               — Euler and RK4 integrators + Method enum
    │   ├── model/
    │   │   ├── SimplePendulum.java           — Single-bob pendulum: state, step, energy, trail
    │   │   ├── DoublePendulum.java           — Two-arm pendulum: Lagrangian ODE, chaos trail
    │   │   └── NewtonsCradle.java            — N-ball cradle: step, impulse collision resolution
    │   └── ui/
    │       ├── MainFrame.java                — JFrame with three-tab JTabbedPane
    │       ├── SimplePendulumPanel.java      — Canvas: rendering + mouse drag
    │       ├── SimplePendulumControls.java   — Control strip (sliders, toggles, reset)
    │       ├── DoublePendulumPanel.java      — Canvas: chaos trail + shadow pendulum
    │       ├── DoublePendulumControls.java   — Control strip for double pendulum
    │       ├── NewtonsCradlePanel.java       — Canvas: frame, strings, metallic balls
    │       └── NewtonsCradleControls.java    — Control strip (ball count, lift, restitution)
    └── test/java/com/pendulums/
        ├── physics/
        │   └── IntegratorTest.java           — Euler, RK4: accuracy, energy conservation, dispatch
        └── model/
            ├── SimplePendulumTest.java       — Construction, period, energy, dynamics, trail
            ├── DoublePendulumTest.java       — Construction, energy, chaos divergence, trail
            └── NewtonsCradleTest.java        — Ball count, lift, elastic/inelastic, geometry
```

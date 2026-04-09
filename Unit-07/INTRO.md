# Unit 7 — Simulation and Modeling

This unit uses a set of interactive programs to develop a single, transferable idea: **complex behavior can emerge from simple rules, and computation lets us study that emergence directly**.

Each program is a working simulation of a real physical, mathematical, or social system. You can run them, change their parameters, and observe what happens. The goal is not to produce correct answers to problem sets, but to build intuition about how systems behave — and to understand why the programs work the way they do.

---

## What Is a Simulation?

A simulation is a program that encodes a mathematical model of a system and steps that model forward in time. At each time step, the program applies a rule (or a set of equations) to the current state and produces the next state. Repeat that enough times and you get a trajectory — the system's history.

This is both powerful and limited:

**Powerful** because you can study systems that are analytically intractable. The three-body problem in orbital mechanics has no closed-form solution. Schelling's segregation model has no equation that tells you the final segregation level from the parameters. The Game of Life has no formula for whether a given initial configuration is stable. In all three cases, the only way to know the answer is to run the simulation.

**Limited** because a simulation is only as good as its model. Every model makes idealizations — it ignores things, approximates things, simplifies things. Understanding what a model captures and what it discards is as important as understanding how it works.

The programs in this unit span a range of physical domains, but they share a common structure: a state, a rule, and a loop. Read the code with that structure in mind.

---

## The Programs

### Classical Mechanics

These three programs model objects moving under forces: gravity, collisions, fluid resistance, and combustion pressure.

**`bouncingballs` — Glass Jar Simulator**
The simplest of the three. Balls in a jar collide with the walls and with each other using Euler integration and impulse-based collision resolution. The interesting addition is *window inertia*: when you drag the application window, the balls slosh as if the jar were physically accelerating. This teaches the principle of inertial reference frames — the balls experience the negative of the window's acceleration, exactly as if the jar were a real container being moved.

*Key concepts*: Euler integration, elastic and inelastic collisions, impulse-momentum theorem, inertial forces.

**`projectiles` — Destructible Terrain**
A slingshot game modeled on *Angry Birds*. The projectile follows a parabolic arc under gravity; collision detection uses a pixel-alpha map of the terrain so that destruction is geometrically exact. Debris particles use a fountain/radial spawn distribution. The primary lesson here is that realistic-looking physics does not require physically correct physics — the gravity constant is chosen to make the arc *look right* on screen, not to match Earth's 9.81 m/s².

*Key concepts*: Euler integration, parabolic projectile motion, pixel-perfect collision detection, procedural generation.

**`orbitaldynamics` — N-Body Simulator and Analytical Solver**
The most technically demanding of the classical mechanics programs. The N-body simulator uses a 4th-order Runge-Kutta integrator to advance the gravitational state of all bodies simultaneously; the analytical solver derives Kepler orbital elements from initial conditions and positions each body exactly, without integration error. Running both together on the same initial conditions and watching them diverge over time is a direct demonstration of numerical error accumulation. *See the project README for an extended discussion of what it means for a model to be correct.*

*Key concepts*: Newtonian gravitation, N-body problem, Runge-Kutta integration, Kepler's laws, orbital elements, conservation of energy and angular momentum.

---

### Thermodynamics and Fluid Physics

**`buoyancy/equation` — Archimedes' Principle Simulator**
Models cylinders floating in a fluid using Archimedes' principle directly: buoyant force equals the weight of fluid displaced. The simulation lets you change the cylinder density and dimensions in real time while observing forces and motion. The key insight is the equilibrium condition: an object floats when its average density equals the fluid density times the submerged fraction.

*Key concepts*: Archimedes' principle, buoyancy, density, Euler integration with viscous drag, equilibrium.

**`buoyancy/pressure` — Pressure Differential Simulator**
Models the same physical situation differently — not as a net Archimedes force, but as the sum of pressure forces on each face of the cylinder. This is the *mechanistic* explanation of why buoyancy exists: pressure increases with depth (Pascal's law), so the upward push on the bottom face always exceeds the downward push on the top face. The simulator shows both results side by side and confirms they agree numerically. Understanding that Archimedes' principle *follows from* Pascal's law — rather than being independent — deepens both.

*Key concepts*: Pascal's law, hydrostatic pressure, pressure-area force, face-by-face force decomposition, equivalence of two models.

**`combustionengine` — Otto-Cycle Simulator**
Models the four-stroke internal combustion engine using slider-crank kinematics for the mechanical geometry and the idealized Otto cycle for thermodynamics. Intake, compression, power, and exhaust strokes are all modeled; the crankshaft dynamics include load and friction torques so the engine reaches a natural RPM equilibrium. The P-V diagram on the lower panel traces the thermodynamic cycle in real time, making the abstract diagram from a thermodynamics course into a live, manipulable object.

*Key concepts*: Otto cycle, isentropic processes, slider-crank mechanism, torque generation, thermal efficiency, crankshaft dynamics.

---

### Emergent Systems

These programs are qualitatively different from the mechanics programs. The interesting behavior is not in any individual component but in the collective — what happens when many simple agents interact under a simple rule.

**`gameoflife` — Conway's Game of Life and Wolfram 1D CAs**
The canonical example of emergence: four rules applied to a two-dimensional grid of on/off cells produce gliders, guns, oscillators, and structures capable of universal computation. The Wolfram 1D explorer adds the complementary picture — 256 elementary rules, many of which produce complex or chaotic patterns from a single initial cell. Together they demonstrate that *complexity is not a property of rules; it is a property of the interaction between rules and initial conditions*.

The step-backward and rewind controls in the Game of Life are worth noting: they are implemented by storing a history of grid states, not by running the physics in reverse (which would be impossible — Life's rule is not injective; many configurations can lead to the same next state).

*Key concepts*: Cellular automata, Moore neighborhood, emergence, universality, computational irreducibility, reversibility.

**`neighborhoods` — Schelling Segregation Model**
Thomas Schelling's 1971 model showed that residential segregation can arise without any agents having strong segregationist preferences. If each agent merely wants *some* neighbors of their own type, the system reliably self-organizes into near-complete segregation. The simulation makes this viscerally clear: start from a well-mixed checkerboard, set the threshold to 0.3 (meaning an agent is satisfied as long as 30% of its neighbors match), and watch the city sort itself. The phase diagram reveals a sharp transition around threshold ≈ 0.5.

This is one of the clearest examples in all of social science of how individual preferences and collective outcomes can be radically misaligned. The emergent pattern (segregation) is not intended by any individual; it is a property of the system.

*Key concepts*: Agent-based modeling, emergence, phase transitions, segregation index, neighborhood types, initial conditions and convergence.

**`smallworld` — Watts-Strogatz Network Model**
Reproduces the landmark 1998 result: a tiny fraction of random long-range connections ("shortcuts") dramatically reduces the average shortest path through a network while leaving its local clustering nearly intact. This is the mathematical structure underlying the "six degrees of separation" phenomenon and explains why information, disease, and influence spread so efficiently through real social networks. The phase diagram sweep reproduces Figure 2 from the original paper.

*Key concepts*: Graph theory, clustering coefficient, average path length, random rewiring, small-world networks, phase transitions.

---

### Probabilistic Systems

**`markovmonopoly` — Markov Chains and Monopoly**
The only console-based program in the unit. A Markov chain is a system where the probability of the next state depends only on the current state — not on the history of how you got there. The program presents four classic examples (weather, gambler's ruin, PageRank, Ehrenfest urn) and then builds a Markov chain from a full Monopoly simulation. The result is that you can compute the long-run probability of landing on every square — and discover that Jail, Illinois Ave, and B&O Railroad are dramatically overrepresented, while the purple properties at the start of the board are undervisited. This is not a strategy program; it is a demonstration that probability theory gives exact answers to questions that look like they can only be answered by simulation.

*Key concepts*: Markov chains, transition matrices, stationary distributions, absorbing states, gambler's ruin, PageRank algorithm.

---

## Cross-Cutting Themes

### Numerical Integration

Five of the programs advance their state by integrating differential equations numerically. The simplest method is **Euler integration**:

```
x(t + dt) = x(t) + v(t) · dt
v(t + dt) = v(t) + a(t) · dt
```

Euler integration is easy to implement and works well for small `dt`, but it accumulates error over time. The orbital dynamics program uses **4th-order Runge-Kutta**, which evaluates the derivative four times per step and combines them to cancel lower-order error terms. The tradeoff is four times the computation per step, but much better accuracy for the same step size.

Neither method conserves energy exactly. For long simulations where energy conservation matters, **symplectic integrators** (like Verlet or leapfrog) are preferred — they don't conserve energy exactly either, but the error oscillates rather than accumulating monotonically.

### Emergence and Scale

Several programs demonstrate the same fundamental phenomenon at different scales and in different domains:
- The Game of Life: simple local rules → complex global structures (gliders, guns, computers)
- Schelling: mild individual preferences → dramatic collective outcome (segregation)
- Watts-Strogatz: a few random edges → globally transformed network topology
- Monopoly: symmetric dice rolls → asymmetric long-run distribution over squares

In each case, the macro-level behavior cannot be predicted by inspecting the micro-level rule. You have to run the system. This is the same reason the weather cannot be predicted from the equations of fluid dynamics alone — not because the equations are wrong, but because the system is computationally irreducible: the only way to know what happens next is to compute what happens next.

### The Role of Models

Every simulation is a model, and every model is wrong in some ways. The two buoyancy programs model the same physical situation differently. The orbital dynamics programs model the same gravitational system using two different computational approaches. In both cases, studying the differences between models is more instructive than studying either model alone.

A useful question to ask of every simulation: *what does this model assume, and when do those assumptions break down?* The combustion engine model assumes instantaneous heat release at TDC — real combustion takes tens of degrees of crank rotation. The Schelling model assumes agents move to a uniformly random empty cell — real people don't. The Game of Life rule is applied to an infinite grid — the simulator uses a finite grid with boundary conditions. None of these simplifications make the programs wrong; they make the programs models, which is what they are.

---

## Suggested Order

If you are approaching this unit for the first time, the following order builds from simple to complex:

1. **`bouncingballs`** — the game loop, Euler integration, basic collision physics
2. **`projectiles`** — collision detection, procedural generation, state machines
3. **`buoyancy/equation`** — forces, equilibrium, drag, real units
4. **`buoyancy/pressure`** — the same system, explained differently
5. **`gameoflife`** — emergence, cellular automata, the power of local rules
6. **`neighborhoods`** — agent-based modeling, social emergence, phase transitions
7. **`smallworld`** — graph theory, network structure, real-world applications
8. **`markovmonopoly`** — probability theory, Markov chains, analytical methods
9. **`orbitaldynamics`** — numerical methods, conservation laws, model limits
10. **`combustionengine`** — thermodynamic cycles, mechanical linkages, multi-physics

You do not have to follow this order. Each program is self-contained. But the progression roughly moves from physically intuitive (balls bouncing) toward mathematically abstract (stationary distributions, orbital elements), so starting from the beginning is lower-friction.

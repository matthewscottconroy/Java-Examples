# Evolutionary Algorithms

Eight self-contained Maven modules covering the core paradigms of evolutionary and swarm computation. Each module is a runnable demo with a real-world scenario, a full test suite, and a README explaining the key ideas.

---

## Why Evolutionary Algorithms?

Classical optimisation (gradient descent, linear programming) requires you to know the mathematical structure of your problem. Evolutionary algorithms make almost no assumptions — they need only a way to *evaluate* candidate solutions and a way to *generate* nearby ones. That generality makes them applicable to a striking range of problems: scheduling, routing, circuit design, symbolic regression, robot control.

The unifying idea across all eight modules is **guided search with feedback**: a population of solutions is evaluated, better solutions are preferentially retained or imitated, and the process repeats until a satisfactory answer emerges.

---

## Module Index

| # | Module | Algorithm | Scenario | Key Concept |
|---|--------|-----------|----------|-------------|
| 01 | [GA Foundations](01-GA-Foundations/) | Binary GA | Password cracker | Tournament selection, single-point crossover, bit-flip mutation |
| 02 | [Traveling Salesman](02-Traveling-Salesman/) | Permutation GA | City tour optimiser | Ordered Crossover (OX), swap mutation |
| 03 | [Continuous Optimization](03-Continuous-Optimization/) | Real-valued GA | Hyperparameter tuner | Arithmetic crossover, Gaussian mutation, Rastrigin benchmark |
| 04 | [Simulated Annealing](04-Simulated-Annealing/) | SA | Job scheduler | Metropolis acceptance, geometric cooling schedule |
| 05 | [Particle Swarm](05-Particle-Swarm/) | PSO | WiFi access point placer | Velocity update, inertia, cognitive and social components |
| 06 | [Genetic Programming](06-Genetic-Programming/) | GP | Formula discoverer | Expression trees, subtree crossover, bloat control |
| 07 | [Multi-Objective](07-Multi-Objective/) | NSGA-II | Cloud deployment planner | Non-dominated sorting, crowding distance, Pareto front |
| 08 | [Ant Colony](08-Ant-Colony/) | ACO | Network router | Pheromone matrix, probabilistic path selection, evaporation |

---

## Taxonomy

```
Evolutionary Algorithms
│
├── Population-based (maintain a set of candidate solutions)
│   ├── Genetic Algorithm (GA) — selection, crossover, mutation
│   │   ├── Binary chromosomes       → 01-GA-Foundations
│   │   ├── Permutation chromosomes  → 02-Traveling-Salesman
│   │   └── Real-valued chromosomes  → 03-Continuous-Optimization
│   │
│   ├── Genetic Programming (GP) — evolves programs, not parameters
│   │   └── Expression trees         → 06-Genetic-Programming
│   │
│   ├── Multi-objective GA — Pareto optimality instead of single fitness
│   │   └── NSGA-II                  → 07-Multi-Objective
│   │
│   └── Swarm Intelligence — emergent behaviour from simple local rules
│       ├── Particle Swarm (PSO)     → 05-Particle-Swarm
│       └── Ant Colony (ACO)         → 08-Ant-Colony
│
└── Single-solution (trajectory-based)
    └── Simulated Annealing (SA)     → 04-Simulated-Annealing
```

---

## Key Concepts Across All Modules

### The Exploration / Exploitation Trade-off

Every algorithm here must balance:
- **Exploration** — searching new, unknown regions of the solution space
- **Exploitation** — refining promising solutions already found

| Algorithm | Exploration mechanism | Exploitation mechanism |
|-----------|----------------------|----------------------|
| GA | Mutation, crossover diversity | Tournament / fitness-proportional selection |
| SA | High-temperature random acceptance | Low-temperature hill climbing |
| PSO | Random r₁, r₂; inertia ω | Pull toward personal and global best |
| GP | Random subtree generation | Subtree crossover from fit parents |
| NSGA-II | Crowding distance diversity | Non-dominated rank selection |
| ACO | Probabilistic path choice | Pheromone amplification of short tours |

### Representation Determines Operators

The chromosome format constrains which operators are valid:

| Representation | Valid crossover | Valid mutation |
|----------------|----------------|----------------|
| Binary string | Single-point, uniform | Bit flip |
| Permutation | OX, PMX, cycle | Swap, insert, invert |
| Real-valued vector | Arithmetic, BLX-α | Gaussian noise + clamp |
| Expression tree | Subtree swap | Node replacement |
| Pheromone matrix | — (no crossover) | Deposit + evaporation |

---

## Running Any Module

```bash
cd <module-name>
mvn compile exec:java   # run the demo
mvn test                # run the test suite
```

# 05 — Particle Swarm Optimisation: WiFi Planner

## The Story

A building needs three wireless access points placed to maximise coverage. The optimal positions aren't obvious — the APs should spread out, avoid walls, and not cluster. PSO models this as 30 candidate placements (particles), each moving through the 6D search space (x₁,y₁, x₂,y₂, x₃,y₃) guided by their own best memory and the swarm's collective best-so-far.

---

## The Swarm Metaphor

Imagine a flock of birds searching for food. Each bird remembers the best location it has personally visited, and the flock shares the location of the best food found by any bird. Each bird steers toward a blend of its personal memory and the swarm's collective knowledge — with some inertia keeping it on its current heading.

---

## The Velocity Update

Each particle has a **position** (current solution) and a **velocity** (current direction and speed):

```
velocity[d] = ω  ×  velocity[d]           ← inertia: carry momentum
            + c₁ × r₁ × (pBest[d] - pos[d])  ← cognitive: pull toward personal best
            + c₂ × r₂ × (gBest[d] - pos[d])  ← social: pull toward global best
```

| Parameter | Typical value | Effect |
|-----------|--------------|--------|
| ω (inertia) | 0.72 | Large → exploration; small → exploitation |
| c₁ (cognitive) | 1.49 | How much to trust personal memory |
| c₂ (social) | 1.49 | How much to follow the swarm |
| r₁, r₂ | random [0,1] | Stochastic variation — different each step |

---

## PSO vs GA

| Property | PSO | GA |
|----------|----|-----|
| Representation | Real-valued | Binary, permutation, tree, real |
| Operators | Velocity + position update | Selection, crossover, mutation |
| Memory | Personal best per particle | Population (implicit) |
| Convergence | Fast, can premature-converge | Slower, more diverse |
| Parameters | ω, c₁, c₂ | mutation rate, crossover rate, pop size |

PSO is often faster than GA for continuous numerical problems; GA is more flexible for discrete and combinatorial ones.

---

## Commands

```bash
mvn compile exec:java
mvn test
```

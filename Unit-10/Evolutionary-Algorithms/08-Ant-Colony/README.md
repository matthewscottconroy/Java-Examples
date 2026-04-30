# 08 — Ant Colony Optimisation: Network Router

## The Story

Eight data centres are connected by links with measured latencies. An operations team needs to find the shortest round-trip path that visits every data centre exactly once — a Travelling Salesman Problem instance. ACO models this as a colony of 20 virtual ants, each building a complete tour by following pheromone trails deposited by successful predecessors.

---

## The Biological Metaphor

Real ants find short paths between nest and food by depositing pheromone. Shorter paths are traversed more often per unit time, so they accumulate more pheromone — a positive feedback loop that amplifies good solutions while evaporation prevents stagnation.

```
Iteration 1:   Ants explore randomly, deposit pheromone proportional to 1/tour_length
Iteration 2:   Slightly stronger pheromone on shorter edges — ants prefer them a little
Iteration N:   Pheromone concentrates on near-optimal edges — most ants follow them
```

---

## The Probabilistic Choice Rule

At each city, an ant chooses its next destination stochastically:

```
P(i → j) ∝  τ(i,j)^α  ×  η(i,j)^β

where:
  τ(i,j) = pheromone strength on edge (i,j)
  η(i,j) = 1 / distance(i,j)  — the heuristic desirability
  α      = controls trust in pheromone history
  β      = controls trust in local distance information
```

| α | β | Behaviour |
|---|---|-----------|
| High | Low | Ants follow history; fast convergence, risk of premature fixation |
| Low | High | Ants follow nearest-neighbour greedily; diverse but no learning |
| 1.0 | 2.0 | Typical balance — pheromone guides, distance informs |

---

## Pheromone Update

After all ants complete their tours:

```
τ(i,j) ← (1−ρ) × τ(i,j) + Σₖ Δτₖ(i,j)

where:
  ρ       = evaporation rate (e.g. 0.1)
  Δτₖ     = Q / tour_length_k  for each ant k that used edge (i,j)
  Q       = deposit constant (scales absolute pheromone level)
```

Evaporation ensures old, suboptimal trails fade away — preventing the colony from locking onto an early poor solution forever.

---

## ACO vs GA for TSP

| Property | ACO | GA (permutation) |
|----------|-----|-----------------|
| Representation | Pheromone matrix | Permutation chromosome |
| Memory | Shared pheromone across iterations | Population (implicit) |
| Diversity mechanism | Evaporation | Crossover + mutation |
| Strength | Builds solutions incrementally using local structure | Recombines entire tours |
| Weakness | Can converge prematurely without tuning | Crossover can disrupt good sub-tours |

---

## Commands

```bash
mvn compile exec:java
mvn test
```

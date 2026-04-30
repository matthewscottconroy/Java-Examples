# 04 — Simulated Annealing: Job Scheduler

## The Story

A factory has 10 jobs to run overnight. Each has a processing time and a customer priority (weight). The goal is to sequence them so that high-priority customers' jobs finish as early as possible — minimising total weighted completion time. Greedy ordering (SPT/W) works well but gets stuck at local optima when job interactions create better orderings. Simulated annealing explores beyond local optima by accepting worse moves — especially early on.

---

## The Annealing Metaphor

In metallurgy, metal is heated and cooled slowly. At high temperature, atoms move freely and can rearrange into low-energy states. Rapid cooling traps atoms in local energy minima; slow cooling (annealing) allows the crystal to find a globally low-energy configuration.

SA applies this to optimisation:
- **Temperature T** starts high (random walk), then decreases (hill climbing)
- A worse neighbour is accepted with probability **exp(-ΔE / T)**

When T is large, exp(-ΔE/T) ≈ 1 (accept almost anything).  
When T is small, exp(-ΔE/T) ≈ 0 (only accept improvements).

---

## The Algorithm

```
current = initialSolution
best = current

while T > T_final:
    for i in 1..iterations_per_temp:
        candidate = neighbour(current)
        ΔE = cost(candidate) - cost(current)

        if ΔE < 0 or rand() < exp(-ΔE / T):
            current = candidate
            if cost(current) < cost(best): best = current

    T *= coolingRate   (geometric cooling: T ← α·T, α ≈ 0.99)

return best
```

---

## Key Design Decisions

**Cooling schedule** — how fast temperature drops.  
Geometric cooling (T ← α·T) is simple and common. Linear and logarithmic schedules exist.

**Neighbour function** — how to generate a nearby solution.  
For permutation problems: swap two elements. For continuous: add Gaussian noise.  
A bad neighbour function (jumps are too large or too small) kills performance.

**Initial temperature** — should be high enough that ~80% of worse moves are accepted initially. A calibration run (estimate average ΔE, set T = -ΔE / ln(0.8)) is common.

---

## SA vs GA

| Property | SA | GA |
|----------|----|----|
| Population | Single solution | Many solutions |
| Parallelism | None | Implicit (population) |
| Memory | O(1) | O(N × chromosome) |
| Tuning | 3 parameters | 4+ parameters |
| Good for | Continuous spaces, large neighbourhood | Combinatorial, discrete |

---

## Commands

```bash
mvn compile exec:java
mvn test
```

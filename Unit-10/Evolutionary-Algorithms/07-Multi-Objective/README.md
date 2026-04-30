# 07 — Multi-Objective Optimisation: Cloud Deployment Planner

## The Story

A platform team deploys six microservices. Every service can be configured anywhere from cheap-but-flaky to expensive-but-reliable. There is no single "best" configuration — a tight budget demands one trade-off, a five-nines SLA demands another. NSGA-II produces the **Pareto front**: the complete set of configurations where no other option is cheaper *and* more reliable simultaneously.

---

## Why Single-Objective Isn't Enough

A single fitness value forces you to pick a weight between cost and reliability *before* running the algorithm. Change your mind and you re-run everything. Multi-objective optimisation surfaces all trade-offs in one run — decision makers choose their operating point after seeing the full picture.

---

## Non-Dominated Sorting (the "front" concept)

```
Objective 2 (unreliability)
  ↑
5 │  ×         ← dominated (worse than ★ on both)
4 │      ★
3 │          ★
2 │              ★
1 │                  ★
  └──────────────────────→ Objective 1 (cost)
       1   2   3   4   5

★ = Pareto-optimal (rank 0 front)
× = Dominated (rank 1 or higher)
```

A solution A **dominates** B if A is no worse on every objective and strictly better on at least one. The rank-0 front contains all solutions that no other solution dominates.

---

## NSGA-II's Two Selection Pressures

| Pressure | Mechanism | Effect |
|----------|-----------|--------|
| **Quality** | Prefer lower rank (front number) | Converge toward the Pareto front |
| **Diversity** | Prefer higher crowding distance | Spread solutions along the front |

**Crowding distance** measures how isolated a solution is within its front. Boundary solutions get ∞; interior solutions get the normalised perimeter of the bounding box formed by their two neighbours.

---

## Crowded Comparison Operator

When selecting parents for the next generation, NSGA-II breaks ties with the crowded-comparison operator:

```
prefer A over B if:
    rank(A) < rank(B)              // quality first
    OR (rank(A) == rank(B) AND crowding(A) > crowding(B))  // diversity second
```

This drives convergence *and* diversity from a single comparison — no weight vector needed.

---

## Commands

```bash
mvn compile exec:java
mvn test
```

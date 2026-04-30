# 03 — Continuous Optimization: Hyperparameter Tuner

## The Story

Training a neural network requires choosing hyperparameters — learning rate, dropout rate, regularisation weight — before training begins. Grid search over a 3D space with 10 values per axis means 1000 full training runs. A real-valued GA explores the same space in tens of evaluations by treating each hyperparameter as a gene and evolving combinations toward higher validation accuracy.

---

## Real-Valued Chromosomes

Instead of bits, each gene is a floating-point number within a bounded range:

```
Individual: [learning_rate=0.0032, dropout=0.18, l2=0.00012]
              ────────────────────────────────────────────────
                     one real number per parameter
```

The search space is continuous — infinitely many possible values — so operators must be redesigned.

---

## Arithmetic Crossover

Mix parents' values with a random blending weight α ∈ [0,1]:

```
Parent 1:  [0.001,  0.30,  0.0005]
Parent 2:  [0.050,  0.10,  0.0002]
α = 0.7

Child: [0.7×0.001 + 0.3×0.050,  0.7×0.30 + 0.3×0.10,  ...]
     = [0.0157,  0.24,  0.00041]
```

The child is always within the convex hull of its parents — valid by construction if parents were valid.

---

## Gaussian Mutation

Add noise sampled from a normal distribution N(0, σ), then clamp to bounds:

```java
gene[d] += rng.nextGaussian() * sigma;
gene[d] = clamp(gene[d], lowerBound[d], upperBound[d]);
```

**σ (mutationSigma)** controls step size:
- Large σ → big jumps → good for early exploration
- Small σ → fine tuning → good for late convergence

Adaptive σ schedules (e.g., 1/5-rule, CMA-ES) adjust σ automatically.

---

## The Rastrigin Function (Classic Benchmark)

Rastrigin is a standard test for continuous EAs. It has a global minimum of 0 at the origin, surrounded by a landscape of regularly spaced local minima that trap hill-climbing algorithms.

```
f(x) = 10n + Σ [xᵢ² - 10·cos(2πxᵢ)]
```

A GA can find the global minimum by maintaining population diversity that prevents premature convergence into a local basin.

---

## Commands

```bash
mvn compile exec:java
mvn test
```

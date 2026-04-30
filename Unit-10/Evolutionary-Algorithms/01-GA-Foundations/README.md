# 01 — Genetic Algorithm Foundations: Feature Selection

## The Story

A machine learning team has 20 candidate features for a classification model. Some features genuinely help; others add noise and hurt accuracy. Testing every subset would take 2²⁰ = 1,048,576 evaluations. A genetic algorithm searches this space in hundreds of evaluations by evolving a population of candidate subsets, breeding the better ones, and discarding the worse.

---

## The Biological Analogy

Genetic algorithms borrow metaphors from natural selection:

| Biology | GA |
|---------|-----|
| Individual organism | Candidate solution |
| Chromosome | Encoded solution (e.g., bit string) |
| Gene | One decision variable (one bit) |
| Fitness | Objective function score |
| Selection | Prefer fitter individuals as parents |
| Crossover | Combine two parents to produce offspring |
| Mutation | Random perturbation of a gene |
| Generation | One round of selection + reproduction |

---

## The Standard Loop

```
1. Initialise: create a random population of N individuals
2. Evaluate:   compute fitness for every individual
3. Select:     choose parents (fitter = more likely to be chosen)
4. Crossover:  combine two parents at a random cut point
5. Mutate:     flip each gene with small probability p_m
6. Elitism:    copy the best individual unchanged into the next generation
7. Go to 2 until termination condition
```

---

## Key Operators

### Tournament Selection
Pick k individuals at random; the one with the highest fitness becomes a parent. Larger k → more selection pressure (population converges faster but may lose diversity).

### Single-Point Crossover
```
Parent 1:  1 1 0 | 1 0 1 0
Parent 2:  0 0 1 | 0 1 0 1
                  ↑ cut point

Child 1:   1 1 0   0 1 0 1   ← first part from parent 1
Child 2:   0 0 1   1 0 1 0   ← first part from parent 2
```

### Bit-Flip Mutation
Each gene flips independently with probability p_m (typically 1/L where L = chromosome length). Mutation prevents the population from getting stuck in local optima.

### Elitism
The best individual is always copied into the next generation unchanged. This guarantees fitness never decreases between generations.

---

## Exploration vs Exploitation

The central tension in every evolutionary algorithm:
- **Exploration** — searching new areas of the solution space (high mutation, large population, low selection pressure)
- **Exploitation** — refining known good solutions (low mutation, high selection pressure)

Too much exploitation → premature convergence (gets stuck in a local optimum).  
Too much exploration → random walk (never converges).

---

## Commands

```bash
mvn compile exec:java
mvn test
```

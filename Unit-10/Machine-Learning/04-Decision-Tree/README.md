# 04 — Decision Tree: Loan Approver

## The Story

A bank's loan officer follows a mental flowchart: "If credit score > 700 AND debt ratio < 40%, approve. Otherwise…" A decision tree formalises this process by learning the thresholds and feature order directly from historical loan decisions. Unlike logistic regression, it can capture non-linear thresholds and feature interactions without any manual feature engineering.

---

## Tree Structure

```
Is credit_score > 0.65?
  ├─ YES → Is debt_ratio < 0.35?
  │           ├─ YES → APPROVED
  │           └─ NO  → Is income > 0.55?
  │                       ├─ YES → APPROVED
  │                       └─ NO  → DENIED
  └─ NO  → DENIED
```

Each **internal node** tests a single feature against a threshold. Each **leaf node** predicts a class. Prediction is a single path from root to leaf — O(depth).

---

## Gini Impurity

At each node the algorithm tries every (feature, threshold) pair and picks the one that minimises the **weighted Gini impurity** of the resulting children:

```
Gini(S) = 1 - Σₖ pₖ²

Gain = Gini(parent) - [|Sₗ|/|S| × Gini(Sₗ) + |Sᵣ|/|S| × Gini(Sᵣ)]
```

A pure node (all one class) has Gini = 0. A perfectly mixed two-class node has Gini = 0.5.

---

## Overfitting and Regularisation

An unconstrained tree will memorise the training data (every leaf is a single sample). Two controls prevent this:

| Parameter | Effect |
|-----------|--------|
| `maxDepth` | Hard ceiling on tree depth |
| `minSamplesSplit` | Refuse to split a node with too few samples |

A shallow tree (depth 1 = a "stump") has high bias but low variance. A deep tree has low bias but high variance. The optimal depth is tuned on a validation set.

---

## Decision Tree vs KNN

| Property | Decision Tree | KNN |
|----------|--------------|-----|
| Boundary shape | Axis-aligned rectangles | Voronoi regions |
| Prediction cost | O(depth) | O(m) |
| Interpretability | Rule extraction | "Nearest cases" |
| Missing features | Handled by surrogate splits | Requires imputation |

---

## Commands

```bash
mvn compile exec:java
mvn test
```

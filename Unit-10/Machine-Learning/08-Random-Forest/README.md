# 08 — Random Forest: Credit Risk Classifier

## The Story

A lending platform must assess credit risk across thousands of applications per day. A single decision tree is interpretable but fragile — small changes in training data produce very different trees. A Random Forest averages 20 diverse trees, making predictions robust to noise and individual outliers. It also produces feature importances that explain *which signals matter most* for the credit decision.

---

## Ensemble Learning: Wisdom of the Crowd

A single expert can be wrong. A crowd of independent experts, each with their own perspective, is collectively much more reliable. Random Forest achieves this with:

1. **Bootstrap aggregation (bagging)** — each tree trains on a random sample drawn *with replacement*. About 63% of training points appear at least once; the rest form an implicit validation set ("out-of-bag" samples).

2. **Random feature subsampling** — at each split, only √n features are eligible as candidates. This *de-correlates* the trees so their errors are independent.

---

## Why De-correlation Matters

If all trees trained on identical data and considered the same features, they would all make the same split decisions and be perfectly correlated. Averaging correlated predictors doesn't reduce variance:

```
Var(average of n identical models) = Var(one model)   ← no benefit

Var(average of n independent models) = Var(one model) / n   ← ideal
```

Bagging + feature subsampling moves from the first scenario toward the second.

---

## Feature Importance

Each split in each tree has a **Gini gain** — how much it reduced impurity. Summing this gain over all uses of a feature across all trees, then normalising, gives a feature importance score:

```
importance[f] = Σ_trees Σ_splits_on_f  gain(split) / total_gain
```

The credit risk model should show that *credit score* and *missed payments* dominate — consistent with real-world lending intuition.

---

## Random Forest vs Decision Tree

| Property | Decision Tree | Random Forest |
|----------|--------------|---------------|
| Variance | High (overfits easily) | Low (ensemble averaging) |
| Interpretability | Full tree printout | Feature importances only |
| Training time | O(m·n·log m) | O(T·m·√n·log m) |
| Accuracy | Lower | Higher |
| Hyperparameters | depth, minSamples | + numTrees, maxFeatures |

---

## Commands

```bash
mvn compile exec:java
mvn test
```

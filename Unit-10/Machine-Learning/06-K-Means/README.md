# 06 — K-Means: Customer Segmentation

## The Story

A retail analytics team wants to group customers by spending behaviour without any predefined categories. K-Means discovers natural clusters in the data — high-value loyal shoppers, occasional mid-spenders, and infrequent bargain hunters — enabling targeted marketing campaigns for each segment.

---

## Lloyd's Algorithm

```
Initialise k centroids (K-Means++ placement)
Repeat until convergence:
  1. Assign each point to its nearest centroid
  2. Recompute each centroid as the mean of its assigned points
```

Each iteration is guaranteed not to increase the within-cluster sum of squares (WCSS). Convergence is assured but may reach a local minimum.

---

## K-Means++ Initialisation

Random centroid placement risks putting multiple centroids in the same dense region. K-Means++ spreads them out:

```
1. Pick first centroid uniformly at random
2. For each subsequent centroid:
   - Compute distance² from each point to its nearest existing centroid
   - Sample next centroid with probability ∝ distance²
```

Far-away points are preferentially chosen as seeds, dramatically reducing the chance of a poor local solution.

---

## Choosing k: The Elbow Method

Plot WCSS against k. The optimal k is the "elbow" — the point where adding another cluster produces diminishing improvement:

```
WCSS
  │
  ●  k=1 (all in one cluster)
  │╲
  │ ●  k=2
  │  ╲
  │   ●  k=3  ← elbow
  │    ╲___________●  k=4  (little gain)
  └─────────────────────── k
```

---

## K-Means vs K-Nearest Neighbours

Despite the similar name, these are completely different algorithms:

| | K-Means | KNN |
|--|---------|-----|
| Learning type | Unsupervised (no labels) | Supervised (uses labels) |
| k meaning | Number of clusters to find | Number of neighbours to vote |
| Output | Cluster assignments | Class prediction |
| Training | Required (iterative) | None (lazy) |

---

## Commands

```bash
mvn compile exec:java
mvn test
```

# 14 — 0/1 Knapsack: Cloud Service Deployment

## The Story

A deployment system has 10 GB of RAM. Nine services are waiting to be deployed, each consuming some RAM and delivering some business value. Which subset of services maximises total value without exceeding the memory budget? You can't split a service — it's either deployed or not (the 0/1 constraint). Greedy by value-density fails here; you need DP.

---

## Why Greedy Fails

Greedy picks the highest value-per-unit-weight item first. But that can leave awkward gaps that smaller items can't fill, whereas a slightly worse item might enable a better combination overall.

```
Items: A(weight=2, value=8, density=4.0)
       B(weight=4, value=14, density=3.5)
       C(weight=3, value=11, density=3.67)
Capacity: 7

Greedy: picks A(density 4.0), then C(density 3.67) → total value 8+11=19, weight 2+3=5 ✓
        then B doesn't fit (2+3+4=9>7).

DP optimal: picks B+C → value 14+11=25, weight 4+3=7 ✓
```

The optimal answer is 25, not 19. DP explores all combinations implicitly.

---

## The Recurrence

```
dp[i][w] = max value using the first i items with knapsack capacity w

dp[0][w] = 0   (no items)
dp[i][0] = 0   (no capacity)

dp[i][w] = dp[i-1][w]                              if item i is too heavy
         = max(dp[i-1][w],
               dp[i-1][w - weight[i]] + value[i])  otherwise
```

Read as: "either skip item i, or include it (if it fits)."

---

## Space Optimization

The full O(n·W) table is needed to reconstruct which items were chosen. If you only need the **maximum value**, process weights in reverse order so a single 1D array of size W+1 suffices.

```java
for (Item item : items)
    for (int w = capacity; w >= item.weight(); w--)
        dp[w] = Math.max(dp[w], dp[w - item.weight()] + item.value());
```

Descending order prevents an item from being counted twice within the same pass.

---

## Variants

| Variant | Constraint | Algorithm |
|---------|-----------|-----------|
| 0/1 knapsack | Each item once | DP O(n·W) |
| Unbounded knapsack | Unlimited copies | DP ascending order |
| Fractional knapsack | Items splittable | Greedy by density O(n log n) |
| Multiple knapsacks | k bins, n items | NP-hard in general |

---

## Commands

```bash
mvn compile exec:java
mvn test
```

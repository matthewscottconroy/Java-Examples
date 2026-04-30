# 12 — Dynamic Programming Foundations

## The Story

A vending machine needs to give change for any amount using the fewest possible coins from a fixed set of denominations. Trying every combination of coins is exponential. DP solves it in O(amount × coins) by building up from simpler sub-problems: if you know the fewest coins for every amount up to 10, you can compute the answer for 11 in one step.

---

## The Two Conditions for DP

**Optimal substructure** — the optimal solution to a problem is built from optimal solutions to sub-problems. For coin change: the optimal way to make 11¢ uses the optimal way to make 11 − coin¢.

**Overlapping sub-problems** — the same sub-problem recurs many times in a naive recursive solution. Fibonacci(5) requires Fibonacci(3) twice; without caching, that subtree is recomputed exponentially many times.

When both conditions hold, DP trades time for space by storing sub-problem results.

---

## Two Styles

### Top-Down (Memoization)
Write the natural recursion, add a cache. The first call computes and stores; subsequent calls return immediately.

```java
long fib(int n, long[] memo) {
    if (n <= 1) return n;
    if (memo[n] != 0) return memo[n];
    return memo[n] = fib(n-1, memo) + fib(n-2, memo);
}
```

**Pro:** only computes sub-problems that are actually needed.  
**Con:** recursion overhead, potential stack overflow on deep problems.

### Bottom-Up (Tabulation)
Fill a table from the smallest sub-problem up to the full problem. No recursion.

```java
long fib(int n) {
    long prev2 = 0, prev1 = 1;
    for (int i = 2; i <= n; i++) {
        long curr = prev1 + prev2;
        prev2 = prev1; prev1 = curr;
    }
    return prev1;
}
```

**Pro:** no recursion, often cache-friendly, easy to reduce space.  
**Con:** must compute all sub-problems (even unused ones).

---

## Problems in This Module

| Problem | DP State | Recurrence |
|---------|----------|-----------|
| Fibonacci | `dp[i]` = ith number | `dp[i] = dp[i-1] + dp[i-2]` |
| Coin Change | `dp[a]` = min coins for amount a | `dp[a] = min(dp[a-coin] + 1)` |
| LIS | `dp[i]` = LIS ending at index i | `dp[i] = max(dp[j]+1) for j<i, arr[j]<arr[i]` |
| Unique Paths | `dp[col]` = paths to current cell | `dp[col] += dp[col-1]` |

---

## Commands

```bash
mvn compile exec:java
mvn test
```

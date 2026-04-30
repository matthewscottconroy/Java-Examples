# 06 — Two-Pointer and Sliding Window

## The Story

A network operations center monitors real-time bandwidth consumption. They need to find the longest uninterrupted window of time where total data transfer stays within the allocated cap — catching sustained bursts without penalizing momentary spikes. Scanning every possible window is O(n²). A sliding window shrinks and grows in one pass: O(n).

---

## Two-Pointer

Two-pointer maintains **left and right indices** that move through a sorted array in concert. Because the array is sorted, moving left forward increases the sum, moving right backward decreases it — so you can home in on a target without checking every pair.

```
arr = [1, 3, 5, 7, 9], target = 12
lo=0(1), hi=4(9): sum=10 < 12 → move lo right
lo=1(3), hi=4(9): sum=12 == 12 ✓ → found [1,4]
```

Naïve O(n²) checks all pairs. Two-pointer does it in O(n).

**Classic uses:**
- Two-sum on sorted array
- Three-sum (fix one element, two-pointer the rest)
- Removing duplicates in-place
- Container with most water

---

## Sliding Window

Sliding window maintains a **contiguous subarray [lo, hi)** where a property holds (sum ≤ cap, all distinct, etc.). The right pointer expands the window; the left pointer shrinks it when the property is violated.

```
arr = [3, 1, 4, 7, 2], cap = 10

hi=0: window=[3], sum=3
hi=1: window=[3,1], sum=4
hi=2: window=[3,1,4], sum=8
hi=3: window=[3,1,4,7], sum=15 > 10 → shrink left
      window=[1,4,7], sum=12 > 10 → shrink left
      window=[4,7], sum=11 > 10 → shrink left
      window=[7], sum=7
hi=4: window=[7,2], sum=9
```

Each element enters and exits the window at most once → O(n) total.

**Classic uses:**
- Longest subarray with bounded sum
- Minimum subarray with sum ≥ target
- Longest substring without repeating characters
- Maximum average subarray of fixed length

---

## Why O(n) Not O(n²)?

The key insight in both patterns: **monotonicity**. Moving the right pointer never makes the sum smaller; moving the left pointer never makes it larger. So you never need to reconsider past positions — the search space collapses from 2D (all pairs) to 1D (one sweep).

---

## Commands

```bash
mvn compile exec:java
mvn test
```

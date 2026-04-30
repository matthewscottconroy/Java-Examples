# 15 — Activity Scheduling: Conference Room Booking

## The Story

Ten meetings are requested for a single conference room. Each meeting has a start and end time. Some overlap — they can't all be booked. Which subset allows the maximum number of meetings? The greedy approach works here: always pick the meeting that ends earliest. It never "wastes" time, so it leaves the most room for future meetings.

---

## Why Greedy Works Here

**Exchange argument**: suppose an optimal solution doesn't include the earliest-finishing activity A. Swap the earliest activity in the optimal solution with A. Because A finishes no later, this replacement cannot cause more conflicts — so the resulting solution is still optimal. By induction, always choosing earliest-finish is safe.

This is a rare case where greedy is provably optimal. Most optimisation problems require DP.

---

## The Algorithm

```
Sort activities by end time.

lastEnd = -∞
for each activity a (sorted by end):
    if a.start >= lastEnd:
        select a
        lastEnd = a.end
```

O(n log n) for the sort, O(n) for the selection pass.

---

## Greedy vs DP

| Criterion | Greedy | DP |
|-----------|--------|----|
| Works when | Greedy choice is globally safe | Optimal substructure + overlapping sub-problems |
| Activity scheduling | ✓ provably optimal | Overkill |
| 0/1 Knapsack | ✗ greedy by density fails | ✓ required |
| Fractional Knapsack | ✓ greedy by density is optimal | Overkill |

The difference: in fractional knapsack you can take partial items, so greedy density-ordering is safe. In 0/1 knapsack you can't, so picking the densest item might block a better combination.

---

## Multi-Track Extension

With k rooms, assign each incoming meeting (sorted by start time) to any room whose previous meeting has already ended, preferring the room that ended most recently (to keep earlier-ending rooms free for longer future meetings). This is the interval graph colouring problem; the minimum number of rooms needed equals the maximum number of simultaneously overlapping meetings.

---

## Commands

```bash
mvn compile exec:java
mvn test
```

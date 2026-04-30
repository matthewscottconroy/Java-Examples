# 03 — Quick Sort: The Inventory Sorter

## The Story

A warehouse management system needs to re-sort 100,000 product records several
times a day — by SKU for stock-takes, by price for reports, by stock level for
reorder alerts. The sort must be fast in practice, not just in theory. Merge sort
guarantees O(n log n) but needs O(n) extra memory. Quick sort typically uses
O(log n) stack space and has a smaller constant factor — making it faster on
modern hardware for in-memory data.

---

## The Algorithm

```
quickSort(arr, lo, hi):
  if lo >= hi: return
  pivot = arr[randomIndex(lo..hi)]   // random pivot avoids sorted-input worst case
  (lt, gt) = partition(arr, lo, hi, pivot)
    // arr[lo..lt-1] < pivot
    // arr[lt..gt]  == pivot
    // arr[gt+1..hi] > pivot
  quickSort(arr, lo, lt - 1)
  quickSort(arr, gt + 1, hi)
```

**Three-way partitioning** (Dutch National Flag) is used here. Standard two-way
partitioning degrades to O(n²) when many duplicates are present because all equal
elements end up in the same partition. Three-way partitioning places all equal
elements in the middle partition and skips them in recursive calls — O(n) on an
array of identical values.

---

## Properties

| Property | Value |
|----------|-------|
| Time complexity | O(n log n) average, O(n²) worst (avoided by random pivot) |
| Space complexity | O(log n) stack space |
| Stable | No — not a stable sort |
| Best for | In-memory sorting; cache-efficient; typically fastest in practice |

Quick sort is unstable: equal elements may change relative order. Where stability
matters, use merge sort. Java's `Arrays.sort` for primitives uses a variant of
dual-pivot quick sort; `Arrays.sort` for objects uses Timsort (stable).

---

## Commands

```bash
mvn compile exec:java
mvn test
```

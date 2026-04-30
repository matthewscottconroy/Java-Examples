# 02 — Merge Sort: The Hospital Patient Record Merger

## The Story

Two hospitals merge their IT systems. Each maintains a patient list sorted by
surname. The combined system needs one unified sorted list. Re-sorting from
scratch would be wasteful — both lists are already sorted. The merge step of
merge sort does exactly this: combine two sorted lists into one in O(n) time by
advancing two pointers and always picking the smaller front element.

This is also why merge sort is the algorithm behind `Collections.sort` in Java
(via Timsort): it can exploit partially-sorted runs in real data.

---

## The Algorithm

```
mergeSort(list):
  if len(list) <= 1: return list          // base case
  mid = len(list) / 2
  left  = mergeSort(list[0..mid])         // recurse
  right = mergeSort(list[mid..end])       // recurse
  return merge(left, right)               // combine

merge(left, right):
  result = []
  while both lists non-empty:
    pick the smaller front element        // O(1) per element
  append any remaining elements
  return result                           // O(n) total
```

The recursion has depth O(log n). Each level does O(n) work. Total: **O(n log n)**.

---

## Properties

| Property | Value |
|----------|-------|
| Time complexity | O(n log n) — all cases |
| Space complexity | O(n) — needs auxiliary storage for the merge step |
| Stable | Yes — equal elements keep their original order |
| Best for | Linked lists, external sorting, merging pre-sorted data |

The O(n) auxiliary space is merge sort's main trade-off against quick sort. For
external sorting (data larger than RAM) it is the *only* practical choice.

---

## Commands

```bash
mvn compile exec:java
mvn test
```

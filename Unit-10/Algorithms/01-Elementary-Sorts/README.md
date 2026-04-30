# 01 — Elementary Sorts: The Library Book Sorter

## The Story

A library volunteer has a cart of returned books to re-shelve. They're jumbled
out of order by author surname. The volunteer sorts them, but the *strategy*
they use — and how many times they pick up or compare books — varies dramatically.

Three strategies, three algorithms. All produce the same sorted result. All run
in O(n²) time in the worst case. But the *constant factors* and *best-case
behaviour* differ in ways that matter in practice.

---

## The Three Algorithms

### Bubble Sort
Walk the list repeatedly. Each pass, compare neighbours and swap if out of order.
The largest unsorted element "bubbles" to its correct position each pass.

- **Best case:** O(n) — if sorted, one pass confirms it (early-exit optimisation)
- **Worst case:** O(n²) comparisons, O(n²) swaps
- **Key insight:** many swaps; good for detecting already-sorted input

### Selection Sort
Find the minimum of the unsorted portion, place it at the front. Repeat.

- **Best/Worst case:** always O(n²) comparisons — never benefits from sorted input
- **O(n) swaps** — each element is placed exactly once; useful when writes are expensive
- **Key insight:** fewest writes of any elementary sort

### Insertion Sort
Maintain a sorted prefix. Take each new element and shift it left into place.

- **Best case:** O(n) — nearly-sorted input requires almost no shifts
- **Worst case:** O(n²) — reverse-sorted requires maximum shifts
- **Key insight:** fastest in practice for small n; used as the base case in Timsort and Introsort

---

## Complexity Summary

| Algorithm | Best | Average | Worst | Swaps | Stable |
|-----------|------|---------|-------|-------|--------|
| Bubble | O(n) | O(n²) | O(n²) | O(n²) | Yes |
| Selection | O(n²) | O(n²) | O(n²) | O(n) | No |
| Insertion | O(n) | O(n²) | O(n²) | O(n²) | Yes |

---

## Commands

```bash
mvn compile exec:java
mvn test
```

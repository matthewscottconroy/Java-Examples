# 05 — Binary Search: The Version Finder

## The Story

A CI/CD pipeline has been running releases nightly for months. Somewhere around release 1.0.9 a regression crept in, and every version from that point onward is broken. The QA team knows releases before the break are clean and releases after it are broken — the list is sorted. They need to find the exact first bad version without checking every one. Binary search cuts the search space in half with each check: 1,000 releases requires at most 10 checks.

---

## The Core Idea

Binary search works on **sorted, random-access** collections. At each step:

1. Look at the middle element.
2. If it equals the target — done.
3. If it's too small — the answer is in the right half.
4. If it's too large — the answer is in the left half.

```
Sorted array:  [1, 3, 5, 7, 9, 11, 13]
Search for 7:

lo=0, hi=6, mid=3 → arr[3]=7 ✓  Found at index 3.

Search for 6:
lo=0, hi=6, mid=3 → arr[3]=7 > 6 → hi=2
lo=0, hi=2, mid=1 → arr[1]=3 < 6 → lo=2
lo=2, hi=2, mid=2 → arr[2]=5 < 6 → lo=3
lo=3 > hi=2 → not found.
```

**Time: O(log n)** — each iteration halves the remaining range.  
**Space: O(1)** — no extra memory.

---

## Variants

### Basic Search
Returns any matching index. Useful when you just need to know if something exists.

### First / Last Occurrence
When duplicates exist, basic search returns an arbitrary match. `searchFirst` and `searchLast` pin the result to the boundaries by continuing to search after finding a match.

```java
List<Integer> list = List.of(1, 2, 2, 2, 3);
BinarySearch.searchFirst(list, 2, cmp)  // → 1
BinarySearch.searchLast(list,  2, cmp)  // → 3
```

### Lower Bound / Upper Bound
These answer insertion-point questions:
- `lowerBound(target)` — first index where `element >= target`
- `upperBound(target)` — first index where `element > target`

```java
upperBound(list, 2, cmp) - lowerBound(list, 2, cmp)  // count of 2s → 3
```

### First Bad Version (VersionFinder)
A predicate-based search: all releases before some boundary are "good", all after are "bad". Binary search finds the exact boundary in O(log n) checks.

---

## The Mid-Point Overflow Trap

A classic bug:
```java
int mid = (lo + hi) / 2;  // wrong: lo+hi can overflow int
int mid = lo + (hi - lo) / 2;  // correct
```

This matters when searching arrays with more than ~2 billion elements — rare in Java, common in embedded and competitive programming.

---

## Commands

```bash
mvn compile exec:java
mvn test
```

# 04 — Heap Sort: The Task Scheduler

## The Story

An operations team receives incidents, bug reports, and routine tasks throughout the day. They need to work through them in strict priority order — a production outage always goes before documentation. Heap sort solves this directly: insert everything into a min-heap, then extract one item at a time. The heap guarantees you always get the highest-priority item next.

---

## The Heap Data Structure

A **binary heap** is a complete binary tree stored as an array. For a min-heap, every parent is smaller than or equal to both its children, so the minimum element is always at index 0.

```
Array index:   0   1   2   3   4   5   6
Value:        [1] [3] [5] [7] [4] [8] [6]

Tree view:
          1           ← always the minimum
        /   \
       3     5
      / \   / \
     7   4 8   6
```

**Index arithmetic (0-based):**
- Parent of `i`: `(i - 1) / 2`
- Left child of `i`: `2*i + 1`
- Right child of `i`: `2*i + 2`

No pointers. No node objects. The entire tree lives in a contiguous array.

---

## The Two Core Operations

**Sift Up** (used after insert):
Add the new element at the end, then repeatedly swap it with its parent while it's smaller than its parent. The heap property is restored in O(log n) steps.

**Sift Down** (used after extractMin):
Replace the root with the last element, remove the last, then repeatedly swap the root with its smallest child while it's larger than that child. Also O(log n).

---

## Heap Sort

```
for each element:    insert into heap   → O(n log n)
for each element:    extractMin         → O(n log n)
total:                                  → O(n log n)
```

The result comes out in sorted order because each `extractMin` gives the next-smallest element.

---

## Comparison with Other O(n log n) Sorts

| Property        | Heap Sort  | Merge Sort | Quick Sort  |
|-----------------|-----------|------------|-------------|
| Time (avg)      | O(n log n) | O(n log n) | O(n log n)  |
| Time (worst)    | O(n log n) | O(n log n) | O(n²)       |
| Space           | O(n)*      | O(n)       | O(log n)    |
| Stable?         | No         | Yes        | No          |
| Real-world use  | Priority queues | External sort | General sort |

\* This implementation uses a separate heap (O(n) extra space). The in-place variant achieves O(1) extra space by building the heap inside the input array itself.

Heap sort's killer feature is not the sort itself — it's the `MinHeap` as a **priority queue**, which supports O(log n) insert and O(log n) extractMin independently. Java's `PriorityQueue` is exactly this structure.

---

## The `MinHeap` API

```java
MinHeap<Task> scheduler = new MinHeap<>(Comparator.naturalOrder());
scheduler.insert(new Task(3, "Send report", "..."));
scheduler.insert(new Task(1, "Fix prod bug", "..."));

Task next = scheduler.extractMin();  // Task(1, "Fix prod bug", ...)
Task top  = scheduler.peek();        // look without removing
int  n    = scheduler.size();
```

---

## Commands

```bash
mvn compile exec:java
mvn test
```

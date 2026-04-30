package com.algorithms.sorting.heapsort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A generic min-heap backed by an array list.
 *
 * <p>The heap property: every parent is ≤ both its children (by the given
 * comparator). The minimum element is always at index 0.
 *
 * <p>Operations:
 * <ul>
 *   <li>{@link #insert} — O(log n): add at the end, sift up.</li>
 *   <li>{@link #extractMin} — O(log n): remove root, replace with last, sift down.</li>
 *   <li>{@link #peek} — O(1): read root without removing.</li>
 * </ul>
 *
 * @param <T> element type
 */
public class MinHeap<T> {

    private final List<T>        data;
    private final Comparator<T>  cmp;

    public MinHeap(Comparator<T> cmp) {
        this.data = new ArrayList<>();
        this.cmp  = cmp;
    }

    public void insert(T value) {
        data.add(value);
        siftUp(data.size() - 1);
    }

    public T extractMin() {
        if (data.isEmpty()) throw new NoSuchElementException("Heap is empty");
        T min = data.get(0);
        int last = data.size() - 1;
        data.set(0, data.get(last));
        data.remove(last);
        if (!data.isEmpty()) siftDown(0);
        return min;
    }

    public T peek() {
        if (data.isEmpty()) throw new NoSuchElementException("Heap is empty");
        return data.get(0);
    }

    public int size()    { return data.size(); }
    public boolean isEmpty() { return data.isEmpty(); }

    private void siftUp(int i) {
        while (i > 0) {
            int parent = (i - 1) / 2;
            if (cmp.compare(data.get(i), data.get(parent)) < 0) {
                swap(i, parent);
                i = parent;
            } else break;
        }
    }

    private void siftDown(int i) {
        int n = data.size();
        while (true) {
            int smallest = i, left = 2*i+1, right = 2*i+2;
            if (left  < n && cmp.compare(data.get(left),  data.get(smallest)) < 0) smallest = left;
            if (right < n && cmp.compare(data.get(right), data.get(smallest)) < 0) smallest = right;
            if (smallest == i) break;
            swap(i, smallest);
            i = smallest;
        }
    }

    private void swap(int i, int j) {
        T t = data.get(i); data.set(i, data.get(j)); data.set(j, t);
    }
}

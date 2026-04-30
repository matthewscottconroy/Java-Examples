package com.algorithms.sorting.heapsort;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class HeapSortTest {

    @Test
    @DisplayName("sorts integers in ascending order")
    void sortsIntegers() {
        List<Integer> input  = List.of(5, 3, 8, 1, 9, 2, 7, 4, 6);
        List<Integer> result = HeapSort.sort(input);
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9), result);
    }

    @Test
    @DisplayName("empty list returns empty list")
    void emptyInput() {
        assertTrue(HeapSort.sort(List.<Integer>of()).isEmpty());
    }

    @Test
    @DisplayName("single element returns single element")
    void singleElement() {
        assertEquals(List.of(42), HeapSort.sort(List.of(42)));
    }

    @Test
    @DisplayName("already sorted input stays sorted")
    void alreadySorted() {
        List<Integer> sorted = List.of(1, 2, 3, 4, 5);
        assertEquals(sorted, HeapSort.sort(sorted));
    }

    @Test
    @DisplayName("reverse-sorted input is correctly sorted")
    void reverseSorted() {
        List<Integer> input  = List.of(5, 4, 3, 2, 1);
        List<Integer> result = HeapSort.sort(input);
        assertEquals(List.of(1, 2, 3, 4, 5), result);
    }

    @Test
    @DisplayName("all duplicate elements remain correct")
    void allDuplicates() {
        List<Integer> input  = List.of(7, 7, 7, 7, 7);
        List<Integer> result = HeapSort.sort(input);
        assertEquals(5, result.size());
        result.forEach(v -> assertEquals(7, v));
    }

    @Test
    @DisplayName("sorts Tasks by priority, ties preserved by insertion order")
    void sortsTasks() {
        List<Task> tasks = List.of(
            new Task(3, "C", ""),
            new Task(1, "A", ""),
            new Task(2, "B", "")
        );
        List<Task> result = HeapSort.sort(tasks);
        assertEquals(1, result.get(0).priority());
        assertEquals(2, result.get(1).priority());
        assertEquals(3, result.get(2).priority());
    }

    @Test
    @DisplayName("custom comparator sorts strings by length")
    void customComparator() {
        List<String> input  = List.of("banana", "kiwi", "fig", "mango", "apple");
        List<String> result = HeapSort.sort(input, (a, b) -> Integer.compare(a.length(), b.length()));
        // lengths: 6, 4, 3, 5, 5 → sorted: fig(3), kiwi(4), mango(5), apple(5), banana(6)
        assertEquals(3, result.get(0).length());  // fig
        assertEquals(4, result.get(1).length());  // kiwi
        // index 2 and 3 are length 5 — order unspecified (not stable)
        assertEquals(5, result.get(2).length());
        assertEquals(5, result.get(3).length());
        assertEquals(6, result.get(4).length());  // banana
    }

    @Test
    @DisplayName("agrees with Collections.sort on 5000 random integers")
    void agreesWithCollectionsSort() {
        Random rng = new Random(99);
        List<Integer> data = new ArrayList<>();
        for (int i = 0; i < 5000; i++) data.add(rng.nextInt(10_000));

        List<Integer> expected = new ArrayList<>(data);
        Collections.sort(expected);

        assertEquals(expected, HeapSort.sort(data));
    }

    @Test
    @DisplayName("MinHeap extractMin always returns the smallest element")
    void minHeapExtractOrder() {
        MinHeap<Integer> heap = new MinHeap<>(Integer::compare);
        List.of(10, 3, 7, 1, 5).forEach(heap::insert);

        assertEquals(1,  heap.extractMin());
        assertEquals(3,  heap.extractMin());
        assertEquals(5,  heap.extractMin());
        assertEquals(7,  heap.extractMin());
        assertEquals(10, heap.extractMin());
        assertTrue(heap.isEmpty());
    }

    @Test
    @DisplayName("MinHeap peek does not remove the element")
    void minHeapPeek() {
        MinHeap<Integer> heap = new MinHeap<>(Integer::compare);
        heap.insert(4);
        heap.insert(2);
        heap.insert(6);

        assertEquals(2, heap.peek());
        assertEquals(3, heap.size());
        assertEquals(2, heap.extractMin());
        assertEquals(2, heap.size());
    }
}

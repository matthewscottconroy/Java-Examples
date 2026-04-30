package com.algorithms.search.binary;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BinarySearchTest {

    private static final Comparator<Integer> ICMP = Integer::compare;

    @Test
    @DisplayName("search finds element in the middle")
    void searchMiddle() {
        List<Integer> list = List.of(1, 3, 5, 7, 9);
        assertEquals(2, BinarySearch.search(list, 5, ICMP));
    }

    @Test
    @DisplayName("search finds element at the start")
    void searchStart() {
        List<Integer> list = List.of(1, 3, 5, 7, 9);
        assertEquals(0, BinarySearch.search(list, 1, ICMP));
    }

    @Test
    @DisplayName("search finds element at the end")
    void searchEnd() {
        List<Integer> list = List.of(1, 3, 5, 7, 9);
        assertEquals(4, BinarySearch.search(list, 9, ICMP));
    }

    @Test
    @DisplayName("search returns -1 for absent element")
    void searchMissing() {
        List<Integer> list = List.of(1, 3, 5, 7, 9);
        assertEquals(-1, BinarySearch.search(list, 4, ICMP));
    }

    @Test
    @DisplayName("search on empty list returns -1")
    void searchEmpty() {
        assertEquals(-1, BinarySearch.search(List.of(), 1, ICMP));
    }

    @Test
    @DisplayName("searchFirst returns first occurrence among duplicates")
    void searchFirstDuplicates() {
        List<Integer> list = List.of(1, 2, 2, 2, 3);
        assertEquals(1, BinarySearch.searchFirst(list, 2, ICMP));
    }

    @Test
    @DisplayName("searchLast returns last occurrence among duplicates")
    void searchLastDuplicates() {
        List<Integer> list = List.of(1, 2, 2, 2, 3);
        assertEquals(3, BinarySearch.searchLast(list, 2, ICMP));
    }

    @Test
    @DisplayName("searchFirst returns -1 when element absent")
    void searchFirstMissing() {
        assertEquals(-1, BinarySearch.searchFirst(List.of(1, 3, 5), 2, ICMP));
    }

    @Test
    @DisplayName("lowerBound returns insertion point for missing element")
    void lowerBoundMissing() {
        List<Integer> list = List.of(1, 3, 5, 7, 9);
        assertEquals(2, BinarySearch.lowerBound(list, 4, ICMP));
    }

    @Test
    @DisplayName("upperBound points past all equal elements")
    void upperBoundDuplicates() {
        List<Integer> list = List.of(1, 2, 2, 2, 3);
        assertEquals(4, BinarySearch.upperBound(list, 2, ICMP));
    }

    @Test
    @DisplayName("count of duplicates via upperBound - lowerBound")
    void countDuplicates() {
        List<Integer> list = List.of(1, 2, 2, 2, 3);
        int count = BinarySearch.upperBound(list, 2, ICMP)
                  - BinarySearch.lowerBound(list, 2, ICMP);
        assertEquals(3, count);
    }

    @Test
    @DisplayName("VersionFinder finds first broken release")
    void versionFinderBasic() {
        List<Release> releases = new ArrayList<>();
        for (int patch = 0; patch <= 10; patch++) {
            releases.add(new Release(1, 0, patch, patch >= 6));
        }
        Release first = VersionFinder.firstBroken(releases);
        assertNotNull(first);
        assertEquals(6, first.patch());
    }

    @Test
    @DisplayName("VersionFinder returns null when no release is broken")
    void versionFinderNoneBroken() {
        List<Release> releases = List.of(
            new Release(1, 0, 0, false),
            new Release(1, 0, 1, false),
            new Release(1, 0, 2, false)
        );
        assertNull(VersionFinder.firstBroken(releases));
    }

    @Test
    @DisplayName("VersionFinder handles first version being broken")
    void versionFinderFirstBroken() {
        List<Release> releases = List.of(
            new Release(1, 0, 0, true),
            new Release(1, 0, 1, true)
        );
        assertEquals(0, VersionFinder.firstBroken(releases).patch());
    }

    @Test
    @DisplayName("binary search on large sorted list is correct")
    void largeSortedList() {
        int n = 1_000_000;
        List<Integer> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) list.add(i * 2);
        assertEquals(499_999, BinarySearch.search(list, 999_998, ICMP));
        assertEquals(-1,      BinarySearch.search(list, 999_999, ICMP));
    }
}

package com.functional.recursion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class RecursionTest {

    private static FileNode sampleTree() {
        return FileNode.dir("root",
                FileNode.dir("src",
                        FileNode.file("Main.java",  1_000),
                        FileNode.file("Util.java",  2_000)),
                FileNode.dir("res",
                        FileNode.file("icon.png",  50_000)),
                FileNode.file("README.md",            500));
    }

    @Test
    @DisplayName("totalSize of a file equals its sizeBytes")
    void fileSizeIsItself() {
        FileNode file = FileNode.file("a.txt", 1_234);
        assertEquals(1_234, FileSystem.totalSize(file));
    }

    @Test
    @DisplayName("totalSize of a directory sums all descendants")
    void dirSizeIsSumOfDescendants() {
        // 1000 + 2000 + 50000 + 500 = 53500
        assertEquals(53_500, FileSystem.totalSize(sampleTree()));
    }

    @Test
    @DisplayName("allFilePaths returns one path per leaf file")
    void allFilePathsCountsLeaves() {
        List<String> paths = FileSystem.allFilePaths(sampleTree(), "");
        assertEquals(4, paths.size());
    }

    @Test
    @DisplayName("largerThan filters files above the threshold")
    void largerThanFilters() {
        List<FileNode> large = FileSystem.largerThan(sampleTree(), 1_000);
        // Only Util.java (2000) and icon.png (50000) exceed 1000
        assertEquals(2, large.size());
    }

    @Test
    @DisplayName("Memoized Fibonacci gives correct values")
    void memoFibCorrect() {
        Function<Integer, Long> fib = Memo.memoize(self -> n ->
                n <= 1 ? (long) n : self.apply(n - 1) + self.apply(n - 2));
        assertEquals(0L,   fib.apply(0));
        assertEquals(1L,   fib.apply(1));
        assertEquals(55L,  fib.apply(10));
        assertEquals(6765L, fib.apply(20));
    }

    @Test
    @DisplayName("Memoized function calls the underlying supplier only once per key")
    void memoCallsOnce() {
        AtomicInteger callCount = new AtomicInteger(0);
        Function<String, Integer> fn = Memo.of(s -> {
            callCount.incrementAndGet();
            return s.length();
        });
        fn.apply("hello");
        fn.apply("hello");
        fn.apply("hello");
        assertEquals(1, callCount.get(), "Supplier should be called only once per key");
    }

    @Test
    @DisplayName("Memoization uses cached value on repeated calls")
    void memoCachesResult() {
        Function<Integer, Long> fib = Memo.memoize(self -> n ->
                n <= 1 ? (long) n : self.apply(n - 1) + self.apply(n - 2));
        long first  = fib.apply(30);
        long second = fib.apply(30);
        assertEquals(first, second);
    }
}

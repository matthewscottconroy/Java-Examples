package com.functional.recursion;

import java.util.List;
import java.util.function.Function;

/**
 * Demonstrates recursion and memoization with a file system and Fibonacci.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Recursion & Memoization ===\n");

        // ── File system recursion ────────────────────────────────────────
        FileNode root = FileNode.dir("home",
            FileNode.dir("projects",
                FileNode.dir("webapp",
                    FileNode.file("App.java",        24_576),
                    FileNode.file("Config.java",      8_192),
                    FileNode.dir("resources",
                        FileNode.file("logo.png",   512_000),
                        FileNode.file("style.css",    4_096))),
                FileNode.dir("scripts",
                    FileNode.file("deploy.sh",        2_048),
                    FileNode.file("backup.sh",        1_024))),
            FileNode.dir("documents",
                FileNode.file("report.pdf",       2_097_152),
                FileNode.file("notes.txt",            3_072)));

        System.out.println("File system tree:");
        FileSystem.print(root, 0);

        System.out.printf("%nTotal size: %,d bytes%n", FileSystem.totalSize(root));

        List<FileNode> large = FileSystem.largerThan(root, 100_000);
        System.out.println("\nFiles > 100 KB:");
        large.forEach(f -> System.out.printf("  %-20s  %,d bytes%n", f.name(), f.sizeBytes()));

        // ── Naive Fibonacci (exponential without memoization) ────────────
        System.out.println("\n--- Fibonacci: naive vs. memoized ---");

        Function<Integer, Long> naiveFib = null;
        // Naive recursive — stack-based, no cache
        // Defined as a regular method for clarity (can't self-reference in lambda easily)

        // Memoized Fibonacci via Memo.memoize
        Function<Integer, Long> memoFib = Memo.memoize(self -> n ->
                n <= 1 ? (long) n : self.apply(n - 1) + self.apply(n - 2));

        System.out.println("Memoized Fibonacci:");
        for (int n : new int[]{0, 1, 5, 10, 20, 40, 50}) {
            long t0  = System.nanoTime();
            long fib = memoFib.apply(n);
            long us  = (System.nanoTime() - t0) / 1_000;
            System.out.printf("  fib(%2d) = %-15d  (%d µs)%n", n, fib, us);
        }

        // ── Simple memoization of a slow function ────────────────────────
        System.out.println("\n--- Memoization of an expensive lookup ---");
        Function<String, Integer> expensiveLookup = Memo.of(key -> {
            System.out.printf("  [Computing for '%s'…]%n", key);
            try { Thread.sleep(100); } catch (InterruptedException e) {}
            return key.length() * 17;
        });

        System.out.println("First call (computed):");
        System.out.println("  Result: " + expensiveLookup.apply("hello"));
        System.out.println("Second call (cached):");
        System.out.println("  Result: " + expensiveLookup.apply("hello"));
    }
}

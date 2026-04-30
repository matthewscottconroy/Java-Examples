package com.meta.mutual;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.out.println("=== Mutual Recursion ===\n");

        // 1. Even / Odd
        System.out.println("--- Even / Odd ---");
        for (int n : new int[]{ 0, 1, 2, 3, 10, 11 }) {
            System.out.printf("  isEven(%2d)=%b  isOdd(%2d)=%b%n",
                n, MutualRecursion.isEven(n), n, MutualRecursion.isOdd(n));
        }

        // 2. Tokeniser
        System.out.println("\n--- Tokeniser ---");
        String[] inputs = { "3.14 + foo * 2", "sin(x) - 42.0", "a1 + b2 * 3" };
        for (String input : inputs) {
            List<MutualRecursion.Token> tokens = MutualRecursion.tokenise(input);
            System.out.printf("  \"%s\"%n  → %s%n", input, tokens);
        }

        // 3. Hofstadter M / F sequences
        System.out.println("\n--- Hofstadter M and F sequences ---");
        System.out.print("  M: ");
        for (int n = 0; n <= 15; n++) System.out.printf("%2d ", MutualRecursion.hofstadterM(n));
        System.out.println();
        System.out.print("  F: ");
        for (int n = 0; n <= 15; n++) System.out.printf("%2d ", MutualRecursion.hofstadterF(n));
        System.out.println();
        System.out.println("  (M(n) + F(n) == n+1 for all n≥1)");
        for (int n = 1; n <= 10; n++) {
            int sum = MutualRecursion.hofstadterM(n) + MutualRecursion.hofstadterF(n);
            System.out.printf("  M(%2d) + F(%2d) = %d + %d = %d  (== %d+1: %b)%n",
                n, n,
                MutualRecursion.hofstadterM(n), MutualRecursion.hofstadterF(n),
                sum, n, sum == n + 1);
        }

        // 4. Balanced parentheses
        System.out.println("\n--- Balanced parentheses ---");
        String[] tests = { "", "()", "(())", "((()))", "()()", "(()(()))", "(", ")(", "(()" };
        for (String t : tests) {
            System.out.printf("  %-12s → %s%n",
                "\"" + t + "\"", MutualRecursion.isBalanced(t) ? "balanced" : "UNBALANCED");
        }
    }
}

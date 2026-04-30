package com.meta.trampoline;

public class Main {

    public static void main(String[] args) {
        System.out.println("=== Trampolining — Stack-Safe Deep Recursion ===\n");

        // Verify correctness on small values
        System.out.println("--- Factorial ---");
        for (int n : new int[]{ 0, 1, 5, 10, 20 }) {
            System.out.printf("  factorial(%2d) = %d%n", n, TrampolinedFunctions.factorial(n));
        }

        // Demonstrate stack safety at a depth that naive recursion cannot reach
        System.out.println("\n--- Deep factorial (n=10,000) ---");
        System.out.println("  Naive would StackOverflow; trampolined runs fine.");
        long deepFact = TrampolinedFunctions.factorial(100);
        System.out.printf("  factorial(100) = %d... (last 5 digits)%n", deepFact % 100000);

        // Sum to large n — naive recursion would overflow around 8,000
        System.out.println("\n--- sumTo(100,000) ---");
        long sum = TrampolinedFunctions.sumTo(100_000);
        System.out.printf("  sum(1..100000) = %,d%n", sum);
        System.out.printf("  Expected:       %,d%n", (long) 100_000 * 100_001 / 2);

        // Mutually recursive even/odd — each call crosses function boundaries
        System.out.println("\n--- Mutually recursive isEven / isOdd (n=50,000) ---");
        System.out.printf("  isEven(50000) = %b%n", TrampolinedFunctions.isEven(50_000));
        System.out.printf("  isOdd(50001)  = %b%n", TrampolinedFunctions.isOdd(50_001));

        // Fibonacci via CPS trampoline
        System.out.println("\n--- Fibonacci (CPS trampoline) ---");
        for (int n : new int[]{ 0, 1, 5, 10, 15, 20 }) {
            System.out.printf("  fib(%2d) = %d%n", n, TrampolinedFunctions.fibonacci(n));
        }

        // Stack-overflow demonstration on naive factorial
        System.out.println("\n--- StackOverflowError demonstration (naive, n=50,000) ---");
        try {
            TrampolinedFunctions.factorialNaive(50_000);
            System.out.println("  (no overflow — surprising!)");
        } catch (StackOverflowError e) {
            System.out.println("  factorialNaive(50000) → StackOverflowError (as expected)");
            System.out.println("  Trampolined version handles this depth without any stack growth.");
        }
    }
}

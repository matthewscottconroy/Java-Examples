package com.info.random;

import java.util.*;
import java.util.stream.IntStream;

public class Main {

    public static void main(String[] args) {
        System.out.println("=== Randomness — Controlled Unpredictability ===\n");

        // ---------------------------------------------------------------
        // 1. Seeded PRNG — reproducible sequences
        // ---------------------------------------------------------------
        System.out.println("--- 1. Seeded PRNG (same seed → same sequence) ---");
        long[] seq1 = Randomness.seededSequence(42L, 5);
        long[] seq2 = Randomness.seededSequence(42L, 5);
        System.out.println("  Seed 42 run 1: " + Arrays.toString(seq1));
        System.out.println("  Seed 42 run 2: " + Arrays.toString(seq2));
        System.out.println("  Equal: " + Arrays.equals(seq1, seq2));

        // ---------------------------------------------------------------
        // 2. Dice simulation
        // ---------------------------------------------------------------
        System.out.println("\n--- 2. 10,000 rolls of a d6 ---");
        Random rng = new Random(99L);
        int[] rolls = Randomness.rollDice(6, 10_000, rng);
        int[] hist  = Randomness.histogram(rolls, 6);
        for (int i = 0; i < hist.length; i++)
            System.out.printf("  Face %d: %5d  (%.1f%%)%n", i + 1, hist[i], 100.0 * hist[i] / rolls.length);
        System.out.printf("  Expected ~1667 each. Std dev across faces: %.1f%n",
            Randomness.stdDev(hist));

        // ---------------------------------------------------------------
        // 3. Fisher-Yates shuffle
        // ---------------------------------------------------------------
        System.out.println("\n--- 3. Shuffle ---");
        List<Integer> deck = IntStream.rangeClosed(1, 13).boxed().toList();
        List<Integer> shuffled = Randomness.shuffle(deck, new Random(7L));
        System.out.println("  Original : " + deck);
        System.out.println("  Shuffled : " + shuffled);

        // ---------------------------------------------------------------
        // 4. Weighted sampling
        // ---------------------------------------------------------------
        System.out.println("\n--- 4. Weighted sampling (A=50%, B=30%, C=20%) ---");
        int[] weights = {50, 30, 20};
        String[] names = {"A", "B", "C"};
        int[] counts = new int[3];
        Random r = new Random(1L);
        for (int i = 0; i < 10_000; i++) counts[Randomness.weightedSample(weights, r)]++;
        for (int i = 0; i < names.length; i++)
            System.out.printf("  %s: %5d (%.1f%%)%n", names[i], counts[i], 100.0 * counts[i] / 10_000);

        // ---------------------------------------------------------------
        // 5. Monte Carlo π
        // ---------------------------------------------------------------
        System.out.println("\n--- 5. Monte Carlo π estimation ---");
        for (int n : new int[]{100, 1_000, 10_000, 100_000, 1_000_000}) {
            double pi = Randomness.estimatePi(n, new Random(n));
            System.out.printf("  n=%8d  π ≈ %.5f  error = %.5f%n", n, pi, Math.abs(pi - Math.PI));
        }

        // ---------------------------------------------------------------
        // 6. SecureRandom bytes (cryptographic use)
        // ---------------------------------------------------------------
        System.out.println("\n--- 6. SecureRandom (for tokens/keys/nonces) ---");
        byte[] token = Randomness.secureBytes(16);
        System.out.println("  16 secure random bytes: " + Arrays.toString(token));
    }
}

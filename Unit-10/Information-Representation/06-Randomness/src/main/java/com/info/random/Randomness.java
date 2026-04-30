package com.info.random;

import java.security.SecureRandom;
import java.util.*;

/**
 * Pseudorandom and cryptographically secure random number generation.
 *
 * <p><b>PRNG vs CSPRNG</b>:
 * <ul>
 *   <li>{@link Random} is a linear congruential generator — fast, deterministic
 *       given the seed, suitable for simulations and games. An attacker who
 *       observes enough output can predict future values.
 *   <li>{@link SecureRandom} uses OS entropy sources (hardware events, /dev/urandom).
 *       Its output is computationally indistinguishable from true randomness.
 *       Use it for keys, tokens, passwords, nonces.
 * </ul>
 *
 * <p><b>Seeding</b>: two {@code Random} instances with the same seed produce
 * the same sequence. This is useful for reproducible simulations and tests.
 */
public final class Randomness {

    private Randomness() {}

    // ---------------------------------------------------------------
    // Dice and sampling
    // ---------------------------------------------------------------

    /** Returns {@code count} rolls of a {@code faces}-sided die (1-based). */
    public static int[] rollDice(int faces, int count, Random rng) {
        int[] rolls = new int[count];
        for (int i = 0; i < count; i++) rolls[i] = rng.nextInt(faces) + 1;
        return rolls;
    }

    /** Weighted random selection: returns an index proportional to its weight. */
    public static int weightedSample(int[] weights, Random rng) {
        int total = Arrays.stream(weights).sum();
        int r = rng.nextInt(total);
        int cumulative = 0;
        for (int i = 0; i < weights.length; i++) {
            cumulative += weights[i];
            if (r < cumulative) return i;
        }
        return weights.length - 1;
    }

    /** Fisher-Yates shuffle — returns a new shuffled list, leaving the original unchanged. */
    public static <T> List<T> shuffle(List<T> list, Random rng) {
        List<T> copy = new ArrayList<>(list);
        for (int i = copy.size() - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            T tmp = copy.get(i); copy.set(i, copy.get(j)); copy.set(j, tmp);
        }
        return copy;
    }

    // ---------------------------------------------------------------
    // Monte Carlo estimation
    // ---------------------------------------------------------------

    /**
     * Estimates π by throwing random darts at a unit square and counting
     * how many land inside the inscribed quarter circle.
     *
     * <p>P(inside) = π/4, so π ≈ 4 × hits / samples.
     * Accuracy improves as O(1/√n) — 10,000× more samples → 100× more accuracy.
     */
    public static double estimatePi(int samples, Random rng) {
        long inside = 0;
        for (int i = 0; i < samples; i++) {
            double x = rng.nextDouble();
            double y = rng.nextDouble();
            if (x * x + y * y <= 1.0) inside++;
        }
        return 4.0 * inside / samples;
    }

    // ---------------------------------------------------------------
    // Seeded reproducibility
    // ---------------------------------------------------------------

    /** Two generators seeded identically produce the same sequence. */
    public static long[] seededSequence(long seed, int length) {
        Random rng = new Random(seed);
        long[] seq = new long[length];
        for (int i = 0; i < length; i++) seq[i] = rng.nextLong();
        return seq;
    }

    // ---------------------------------------------------------------
    // Cryptographically secure bytes
    // ---------------------------------------------------------------

    public static byte[] secureBytes(int n) {
        byte[] bytes = new byte[n];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }

    // ---------------------------------------------------------------
    // Analysis helpers
    // ---------------------------------------------------------------

    /** Builds a frequency histogram over die faces [1..faces]. */
    public static int[] histogram(int[] rolls, int faces) {
        int[] hist = new int[faces];
        for (int r : rolls) hist[r - 1]++;
        return hist;
    }

    /** Sample mean. */
    public static double mean(int[] values) {
        return Arrays.stream(values).average().orElse(0);
    }

    /** Sample standard deviation. */
    public static double stdDev(int[] values) {
        double m = mean(values);
        double variance = Arrays.stream(values)
            .mapToDouble(v -> (v - m) * (v - m))
            .average().orElse(0);
        return Math.sqrt(variance);
    }
}

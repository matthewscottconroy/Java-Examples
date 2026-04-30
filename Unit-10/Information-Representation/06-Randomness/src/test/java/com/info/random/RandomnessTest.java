package com.info.random;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class RandomnessTest {

    // -- rollDice --

    @Test @DisplayName("rollDice values are in [1, faces]")
    void dice_inBounds() {
        int[] rolls = Randomness.rollDice(6, 1000, new Random(0));
        for (int r : rolls) assertTrue(r >= 1 && r <= 6, "Out of range: " + r);
    }

    @Test @DisplayName("rollDice returns the requested count")
    void dice_count() {
        assertEquals(50, Randomness.rollDice(6, 50, new Random(0)).length);
    }

    @Test @DisplayName("rollDice with seeded RNG is reproducible")
    void dice_seeded() {
        int[] a = Randomness.rollDice(6, 10, new Random(42));
        int[] b = Randomness.rollDice(6, 10, new Random(42));
        assertArrayEquals(a, b);
    }

    // -- shuffle --

    @Test @DisplayName("shuffle contains same elements in different order (with high probability)")
    void shuffle_sameElements() {
        List<Integer> original = IntStream.rangeClosed(1, 52).boxed().toList();
        List<Integer> shuffled = Randomness.shuffle(original, new Random(7));
        assertEquals(new HashSet<>(original), new HashSet<>(shuffled));
    }

    @Test @DisplayName("shuffle does not modify original list")
    void shuffle_doesNotMutate() {
        List<Integer> original = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        List<Integer> copy = new ArrayList<>(original);
        Randomness.shuffle(original, new Random(0));
        assertEquals(copy, original);
    }

    @Test @DisplayName("shuffle with same seed is reproducible")
    void shuffle_seeded() {
        List<Integer> list = IntStream.rangeClosed(1, 10).boxed().toList();
        assertEquals(
            Randomness.shuffle(list, new Random(99)),
            Randomness.shuffle(list, new Random(99)));
    }

    // -- estimatePi --

    @Test @DisplayName("estimatePi with 100,000 samples is within 0.05 of π")
    void pi_accuracy() {
        double est = Randomness.estimatePi(100_000, new Random(12345));
        assertEquals(Math.PI, est, 0.05,
            "Expected π ≈ 3.14159, got " + est);
    }

    @Test @DisplayName("estimatePi returns value between 2 and 4")
    void pi_roughBounds() {
        double est = Randomness.estimatePi(1000, new Random(0));
        assertTrue(est > 2.0 && est < 4.0);
    }

    // -- seededSequence --

    @Test @DisplayName("seededSequence with same seed produces identical sequence")
    void seeded_reproducible() {
        assertArrayEquals(
            Randomness.seededSequence(77L, 20),
            Randomness.seededSequence(77L, 20));
    }

    @Test @DisplayName("seededSequence with different seeds produces different sequences")
    void seeded_different() {
        assertFalse(Arrays.equals(
            Randomness.seededSequence(1L, 10),
            Randomness.seededSequence(2L, 10)));
    }

    // -- weightedSample --

    @Test @DisplayName("weightedSample returns index in [0, weights.length)")
    void weighted_inBounds() {
        int[] weights = {50, 30, 20};
        Random rng = new Random(0);
        for (int i = 0; i < 1000; i++) {
            int idx = Randomness.weightedSample(weights, rng);
            assertTrue(idx >= 0 && idx < weights.length);
        }
    }

    @Test @DisplayName("weightedSample with single non-zero weight always returns 0")
    void weighted_singleNonZero() {
        int[] weights = {100, 0, 0};
        Random rng = new Random(0);
        for (int i = 0; i < 100; i++)
            assertEquals(0, Randomness.weightedSample(weights, rng));
    }

    // -- secureBytes --

    @Test @DisplayName("secureBytes returns array of requested length")
    void secure_length() {
        assertEquals(32, Randomness.secureBytes(32).length);
    }

    @Test @DisplayName("secureBytes two calls return different values (with overwhelming probability)")
    void secure_different() {
        assertFalse(Arrays.equals(Randomness.secureBytes(16), Randomness.secureBytes(16)));
    }

    // -- histogram --

    @Test @DisplayName("histogram sums to number of rolls")
    void hist_sum() {
        int[] rolls = Randomness.rollDice(6, 600, new Random(0));
        int[] hist  = Randomness.histogram(rolls, 6);
        int sum = Arrays.stream(hist).sum();
        assertEquals(600, sum);
    }
}

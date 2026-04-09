package com.markovmonopoly.monopoly.dice;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A pair of standard six-sided dice.
 *
 * <p>Rolling doubles (both dice show the same value) has special significance in
 * Monopoly: the player rolls again, and three consecutive doubles send the player to Jail.
 */
public final class Dice {

    private final Random rng;

    public Dice(Random rng) {
        this.rng = rng;
    }

    /** Creates dice using a thread-local random number generator. */
    public Dice() {
        this(ThreadLocalRandom.current());
    }

    /**
     * Rolls both dice and returns the result.
     */
    public DiceRoll roll() {
        int d1 = rng.nextInt(6) + 1;
        int d2 = rng.nextInt(6) + 1;
        return new DiceRoll(d1, d2);
    }

    /**
     * Returns the probability distribution over dice totals (2–12).
     * Index 0 = total of 2, index 10 = total of 12.
     *
     * <p>This is the theoretical distribution from two fair dice — useful for
     * constructing the theoretical Markov chain transition matrix.
     */
    public static double[] totalProbabilities() {
        double[] probs = new double[13];  // index = total (2..12)
        for (int d1 = 1; d1 <= 6; d1++) {
            for (int d2 = 1; d2 <= 6; d2++) {
                probs[d1 + d2] += 1.0 / 36.0;
            }
        }
        return probs;
    }

    /**
     * Probability of rolling doubles on two fair dice = 6/36 = 1/6.
     */
    public static double doublesProbability() {
        return 1.0 / 6.0;
    }
}

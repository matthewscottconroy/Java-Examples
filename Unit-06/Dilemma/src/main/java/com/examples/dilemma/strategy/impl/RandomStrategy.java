package com.examples.dilemma.strategy.impl;

import com.examples.dilemma.strategy.GameHistory;
import com.examples.dilemma.strategy.Move;
import com.examples.dilemma.strategy.Strategy;

import java.util.Random;

/**
 * Random — a 50/50 coin flip every round.
 *
 * <p>Ignores all history. Provides a useful baseline: any strategy that
 * cannot outperform random noise is not doing meaningful work.
 */
public final class RandomStrategy implements Strategy {

    private final Random random;

    /** Creates a Random strategy with a fixed seed for reproducibility. */
    public RandomStrategy() {
        this.random = new Random(42);
    }

    @Override public String getName() { return "Random"; }

    @Override public String getDescription() {
        return "Cooperate or defect with equal probability each round.";
    }

    @Override
    public Move choose(GameHistory history) {
        return random.nextBoolean() ? Move.COOPERATE : Move.DEFECT;
    }

    @Override
    public void reset() {
        random.setSeed(42);
    }
}

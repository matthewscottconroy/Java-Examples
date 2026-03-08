package com.examples.dilemma.strategy.impl;

import com.examples.dilemma.strategy.GameHistory;
import com.examples.dilemma.strategy.Move;
import com.examples.dilemma.strategy.Strategy;

import java.util.Random;

/**
 * Joss — Tit for Tat with occasional sneaky defections.
 *
 * <p>Behaves exactly like Tit for Tat except that when it would cooperate
 * it defects with 10% probability, hoping to steal a Temptation point.
 * In practice this small betrayal rate tends to erode mutual cooperation
 * and drag scores below a pure Tit for Tat.
 */
public final class Joss implements Strategy {

    private static final double DEFECT_PROBABILITY = 0.10;
    private final Random random;

    /** Creates a Joss strategy with a fixed seed for reproducibility. */
    public Joss() {
        this.random = new Random(7);
    }

    @Override public String getName() { return "Joss"; }

    @Override public String getDescription() {
        return "Tit for Tat but defects with 10% chance when it would cooperate.";
    }

    @Override
    public Move choose(GameHistory history) {
        Move tftChoice = history.getOpponentLastMove().orElse(Move.COOPERATE);
        if (tftChoice == Move.DEFECT) return Move.DEFECT;
        return random.nextDouble() < DEFECT_PROBABILITY ? Move.DEFECT : Move.COOPERATE;
    }

    @Override
    public void reset() {
        random.setSeed(7);
    }
}

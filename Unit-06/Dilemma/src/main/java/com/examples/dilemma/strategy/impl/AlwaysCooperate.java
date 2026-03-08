package com.examples.dilemma.strategy.impl;

import com.examples.dilemma.strategy.GameHistory;
import com.examples.dilemma.strategy.Move;
import com.examples.dilemma.strategy.Strategy;

/**
 * Always Cooperate — unconditional altruism.
 *
 * <p>Cooperates every single round regardless of what the opponent does.
 * Scores well against itself and Tit for Tat, but is easily exploited
 * by Always Defect.
 */
public final class AlwaysCooperate implements Strategy {

    @Override public String getName() { return "Always Cooperate"; }

    @Override public String getDescription() {
        return "Cooperate every round, no matter what.";
    }

    @Override
    public Move choose(GameHistory history) {
        return Move.COOPERATE;
    }

    @Override public void reset() { /* stateless */ }
}

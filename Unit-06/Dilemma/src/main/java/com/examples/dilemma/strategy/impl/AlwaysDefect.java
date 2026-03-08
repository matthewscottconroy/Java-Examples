package com.examples.dilemma.strategy.impl;

import com.examples.dilemma.strategy.GameHistory;
import com.examples.dilemma.strategy.Move;
import com.examples.dilemma.strategy.Strategy;

/**
 * Always Defect — pure selfishness.
 *
 * <p>Defects every round. Dominates any cooperating opponent but scores
 * only Punishment points in mutual defection. Cannot build the cooperative
 * surplus that makes Tit for Tat successful.
 */
public final class AlwaysDefect implements Strategy {

    @Override public String getName() { return "Always Defect"; }

    @Override public String getDescription() {
        return "Defect every round, no matter what.";
    }

    @Override
    public Move choose(GameHistory history) {
        return Move.DEFECT;
    }

    @Override public void reset() { /* stateless */ }
}

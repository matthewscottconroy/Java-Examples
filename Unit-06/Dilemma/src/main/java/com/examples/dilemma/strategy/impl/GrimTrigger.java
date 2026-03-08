package com.examples.dilemma.strategy.impl;

import com.examples.dilemma.strategy.GameHistory;
import com.examples.dilemma.strategy.Move;
import com.examples.dilemma.strategy.Strategy;

/**
 * Grim Trigger (Friedman strategy) — eternal retaliation.
 *
 * <p>Cooperates until the opponent defects even once, then defects for
 * every remaining round. The harshest possible punishment: a single
 * betrayal is never forgiven.
 */
public final class GrimTrigger implements Strategy {

    private boolean triggered = false;

    @Override public String getName() { return "Grim Trigger"; }

    @Override public String getDescription() {
        return "Cooperate until the opponent defects once; then defect forever.";
    }

    @Override
    public Move choose(GameHistory history) {
        if (!triggered && history.getOpponentMoves().contains(Move.DEFECT)) {
            triggered = true;
        }
        return triggered ? Move.DEFECT : Move.COOPERATE;
    }

    @Override
    public void reset() {
        triggered = false;
    }
}

package com.examples.dilemma.strategy.impl;

import com.examples.dilemma.strategy.GameHistory;
import com.examples.dilemma.strategy.Move;
import com.examples.dilemma.strategy.Strategy;

/**
 * Suspicious Tit for Tat — starts hostile, then mirrors.
 *
 * <p>Opens with a defection (testing the opponent), then copies whatever
 * the opponent did last round. The opening defection often seeds a cycle
 * of mutual retaliation against other mirroring strategies.
 */
public final class SuspiciousTitForTat implements Strategy {

    @Override public String getName() { return "Suspicious Tit for Tat"; }

    @Override public String getDescription() {
        return "Defect on round 1; then mirror the opponent's last move.";
    }

    @Override
    public Move choose(GameHistory history) {
        if (history.getRound() == 0) return Move.DEFECT;
        return history.getOpponentLastMove().orElse(Move.DEFECT);
    }

    @Override public void reset() { /* stateless */ }
}

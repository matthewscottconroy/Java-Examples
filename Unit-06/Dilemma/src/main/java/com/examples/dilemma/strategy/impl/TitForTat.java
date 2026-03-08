package com.examples.dilemma.strategy.impl;

import com.examples.dilemma.strategy.GameHistory;
import com.examples.dilemma.strategy.Move;
import com.examples.dilemma.strategy.Strategy;

/**
 * Tit for Tat — the winner of Axelrod's first tournament (1980).
 *
 * <p>Cooperate on the first move, then copy whatever the opponent did
 * last round. Simple, forgiving, and impossible to exploit for long.
 */
public final class TitForTat implements Strategy {

    @Override public String getName() { return "Tit for Tat"; }

    @Override public String getDescription() {
        return "Cooperate first; then mirror the opponent's last move.";
    }

    @Override
    public Move choose(GameHistory history) {
        return history.getOpponentLastMove().orElse(Move.COOPERATE);
    }

    @Override public void reset() { /* stateless */ }
}

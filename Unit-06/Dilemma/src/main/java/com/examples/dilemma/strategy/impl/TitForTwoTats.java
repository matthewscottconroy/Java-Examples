package com.examples.dilemma.strategy.impl;

import com.examples.dilemma.strategy.GameHistory;
import com.examples.dilemma.strategy.Move;
import com.examples.dilemma.strategy.Strategy;

import java.util.List;

/**
 * Tit for Two Tats — a more forgiving Tit for Tat.
 *
 * <p>Cooperates unless the opponent defected on <em>both</em> of the last two
 * rounds. A single defection is forgiven, which makes it more robust against
 * noise or accidental betrayals.
 */
public final class TitForTwoTats implements Strategy {

    @Override public String getName() { return "Tit for Two Tats"; }

    @Override public String getDescription() {
        return "Only defect after the opponent defects twice in a row.";
    }

    @Override
    public Move choose(GameHistory history) {
        List<Move> opp = history.getOpponentMoves();
        int n = opp.size();
        if (n < 2) return Move.COOPERATE;
        if (opp.get(n - 1) == Move.DEFECT && opp.get(n - 2) == Move.DEFECT) {
            return Move.DEFECT;
        }
        return Move.COOPERATE;
    }

    @Override public void reset() { /* stateless */ }
}

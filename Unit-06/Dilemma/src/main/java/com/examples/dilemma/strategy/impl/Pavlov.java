package com.examples.dilemma.strategy.impl;

import com.examples.dilemma.strategy.GameHistory;
import com.examples.dilemma.strategy.Move;
import com.examples.dilemma.strategy.Payoff;
import com.examples.dilemma.strategy.Strategy;

/**
 * Pavlov (Win-Stay, Lose-Shift) — learn from outcomes.
 *
 * <p>Cooperates on the first move. Thereafter: if last round's payoff was
 * a "win" (Reward or Temptation), repeat the same move; if it was a "loss"
 * (Punishment or Sucker), switch to the opposite move.
 *
 * <p>Pavlov can correct accidental defections and exploit persistent cooperators,
 * but is vulnerable to Always Defect.
 */
public final class Pavlov implements Strategy {

    @Override public String getName() { return "Pavlov"; }

    @Override public String getDescription() {
        return "Win-Stay, Lose-Shift: repeat if rewarded, switch if punished.";
    }

    @Override
    public Move choose(GameHistory history) {
        if (history.getRound() == 0) return Move.COOPERATE;

        Move myLast   = history.getMyLastMove().orElseThrow();
        Move theirLast = history.getOpponentLastMove().orElseThrow();
        int lastScore  = Payoff.score(myLast, theirLast);
        boolean won    = (lastScore == Payoff.REWARD || lastScore == Payoff.TEMPTATION);
        return won ? myLast : myLast.opposite();
    }

    @Override public void reset() { /* stateless */ }
}

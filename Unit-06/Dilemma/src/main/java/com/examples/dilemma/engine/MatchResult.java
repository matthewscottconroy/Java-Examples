package com.examples.dilemma.engine;

import com.examples.dilemma.strategy.Move;

import java.util.List;

/**
 * The outcome of a single game between two strategies.
 *
 * <p>A {@code MatchResult} records each player's total score and the full
 * sequence of moves played, enabling both aggregate standings and
 * round-by-round analysis.
 *
 * @param nameA   the name of strategy A
 * @param nameB   the name of strategy B
 * @param scoreA  strategy A's total score
 * @param scoreB  strategy B's total score
 * @param movesA  strategy A's move sequence (one entry per round)
 * @param movesB  strategy B's move sequence (one entry per round)
 */
public record MatchResult(
        String nameA,
        String nameB,
        int scoreA,
        int scoreB,
        List<Move> movesA,
        List<Move> movesB) {

    /**
     * Returns the name of the winner, or {@code "Draw"} if scores are equal.
     *
     * @return winner name or "Draw"
     */
    public String winner() {
        if (scoreA > scoreB) return nameA;
        if (scoreB > scoreA) return nameB;
        return "Draw";
    }

    /** Returns {@code true} if the two scores are equal. */
    public boolean isDraw() {
        return scoreA == scoreB;
    }

    /** Returns the number of rounds played in this match. */
    public int rounds() {
        return movesA.size();
    }
}

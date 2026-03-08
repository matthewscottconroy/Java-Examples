package com.examples.dilemma.engine;

import com.examples.dilemma.strategy.GameHistory;
import com.examples.dilemma.strategy.Move;
import com.examples.dilemma.strategy.Payoff;
import com.examples.dilemma.strategy.Strategy;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs a single iterated Prisoner's Dilemma match between two strategies.
 *
 * <p>Each strategy is given an immutable {@link GameHistory} snapshot at the
 * start of each round. The snapshot reflects all moves made so far from that
 * player's own perspective (its own moves + the opponent's moves). Strategies
 * cannot see each other's internal state — only what has been played.
 *
 * <p>Scores accumulate according to {@link Payoff}. Neither strategy is told
 * the other's identity.
 */
public final class Game {

    private final int rounds;

    /**
     * Creates a game that runs for the given number of rounds.
     *
     * @param rounds the number of rounds per match (must be positive)
     */
    public Game(int rounds) {
        if (rounds <= 0) throw new IllegalArgumentException("Rounds must be positive.");
        this.rounds = rounds;
    }

    /**
     * Plays a full match between strategy {@code a} and strategy {@code b}.
     *
     * <p>Both strategies are reset at the start. Each round, both choose
     * simultaneously based on the history of previous rounds. Scores are
     * tallied and the full move lists recorded.
     *
     * @param a the first strategy
     * @param b the second strategy
     * @return the result of the match
     */
    public MatchResult play(Strategy a, Strategy b) {
        a.reset();
        b.reset();

        List<Move> movesA = new ArrayList<>(rounds);
        List<Move> movesB = new ArrayList<>(rounds);
        int scoreA = 0;
        int scoreB = 0;

        for (int r = 0; r < rounds; r++) {
            // Each strategy sees the game from its own perspective
            GameHistory histA = new GameHistory(movesA, movesB);
            GameHistory histB = new GameHistory(movesB, movesA);

            Move ma = a.choose(histA);
            Move mb = b.choose(histB);

            scoreA += Payoff.score(ma, mb);
            scoreB += Payoff.score(mb, ma);

            movesA.add(ma);
            movesB.add(mb);
        }

        return new MatchResult(
                a.getName(), b.getName(),
                scoreA, scoreB,
                List.copyOf(movesA), List.copyOf(movesB));
    }
}

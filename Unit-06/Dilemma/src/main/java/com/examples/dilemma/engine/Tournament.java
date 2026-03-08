package com.examples.dilemma.engine;

import com.examples.dilemma.strategy.Strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A round-robin Prisoner's Dilemma tournament.
 *
 * <p>Every strategy plays against every other strategy exactly once, for
 * {@code roundsPerMatch} rounds each. Scores accumulate across all matches.
 *
 * <p>A progress callback can be supplied to {@link #run(Consumer)} so that
 * a GUI or logger can display real-time status updates without blocking.
 */
public final class Tournament {

    private final List<Strategy> strategies;
    private final int roundsPerMatch;

    /**
     * Creates a tournament with the given strategies and rounds per match.
     *
     * @param strategies    the strategies to enter; must contain at least 2
     * @param roundsPerMatch number of rounds each pair plays
     */
    public Tournament(List<Strategy> strategies, int roundsPerMatch) {
        if (strategies.size() < 2) {
            throw new IllegalArgumentException("A tournament needs at least 2 strategies.");
        }
        this.strategies    = List.copyOf(strategies);
        this.roundsPerMatch = roundsPerMatch;
    }

    /**
     * Runs the tournament and returns the complete results.
     *
     * @return the tournament result with ranked standings
     */
    public TournamentResult run() {
        return run(msg -> {});
    }

    /**
     * Runs the tournament, calling {@code progress} after each match with a
     * status message. Safe to use from a background thread.
     *
     * @param progress a callback invoked after each match (e.g., to update a status bar)
     * @return the tournament result with ranked standings
     */
    public TournamentResult run(Consumer<String> progress) {
        Game game = new Game(roundsPerMatch);
        List<MatchResult> matches = new ArrayList<>();

        for (int i = 0; i < strategies.size(); i++) {
            for (int j = i + 1; j < strategies.size(); j++) {
                Strategy a = strategies.get(i);
                Strategy b = strategies.get(j);
                MatchResult result = game.play(a, b);
                matches.add(result);
                progress.accept(a.getName() + " vs " + b.getName()
                        + " → " + result.scoreA() + " : " + result.scoreB());
            }
        }

        return TournamentResult.from(matches, roundsPerMatch);
    }

    /** Returns the number of matches that will be played in this tournament. */
    public int totalMatches() {
        int n = strategies.size();
        return n * (n - 1) / 2;
    }
}

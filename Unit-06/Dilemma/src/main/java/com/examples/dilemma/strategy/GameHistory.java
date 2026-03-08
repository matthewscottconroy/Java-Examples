package com.examples.dilemma.strategy;

import java.util.List;
import java.util.Optional;

/**
 * An immutable snapshot of the moves played so far in a single game,
 * from the perspective of one player.
 *
 * <p>A fresh {@code GameHistory} (round 0) has no moves recorded. After
 * each round the game engine creates a new snapshot with the latest moves
 * appended and passes it to the strategy before the next choice.
 *
 * <p>The history is always told from <em>this player's</em> point of view:
 * {@link #getMyMoves()} returns the player's own past moves, and
 * {@link #getOpponentMoves()} returns what the opponent did.
 */
public final class GameHistory {

    private final List<Move> myMoves;
    private final List<Move> opponentMoves;

    /**
     * Creates a history with the given move lists.
     *
     * @param myMoves       this player's moves, earliest first
     * @param opponentMoves the opponent's moves, earliest first
     */
    public GameHistory(List<Move> myMoves, List<Move> opponentMoves) {
        this.myMoves       = List.copyOf(myMoves);
        this.opponentMoves = List.copyOf(opponentMoves);
    }

    /** Returns an empty history representing the start of a game. */
    public static GameHistory empty() {
        return new GameHistory(List.of(), List.of());
    }

    /**
     * Returns the current round number (= number of rounds already played).
     * This is 0 before the first move is made.
     *
     * @return the number of completed rounds
     */
    public int getRound() {
        return myMoves.size();
    }

    /**
     * Returns this player's move history, earliest first.
     *
     * @return unmodifiable list of this player's past moves
     */
    public List<Move> getMyMoves() {
        return myMoves;
    }

    /**
     * Returns the opponent's move history, earliest first.
     *
     * @return unmodifiable list of the opponent's past moves
     */
    public List<Move> getOpponentMoves() {
        return opponentMoves;
    }

    /**
     * Returns this player's most recent move, or empty if no rounds have been played.
     *
     * @return the last move made by this player
     */
    public Optional<Move> getMyLastMove() {
        return myMoves.isEmpty() ? Optional.empty()
                : Optional.of(myMoves.get(myMoves.size() - 1));
    }

    /**
     * Returns the opponent's most recent move, or empty if no rounds have been played.
     *
     * @return the last move made by the opponent
     */
    public Optional<Move> getOpponentLastMove() {
        return opponentMoves.isEmpty() ? Optional.empty()
                : Optional.of(opponentMoves.get(opponentMoves.size() - 1));
    }
}

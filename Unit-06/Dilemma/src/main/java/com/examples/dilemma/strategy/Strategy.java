package com.examples.dilemma.strategy;

/**
 * The contract every tournament competitor must fulfil.
 *
 * <h2>Writing your own strategy</h2>
 *
 * <ol>
 *   <li>Create a public class with a public no-arg constructor that implements
 *       this interface.</li>
 *   <li>Compile it against {@code dilemma-1.0-SNAPSHOT.jar}:
 *       <pre>javac -cp target/dilemma-1.0-SNAPSHOT.jar MyStrategy.java</pre></li>
 *   <li>Drop the resulting {@code MyStrategy.class} into the {@code strategies/}
 *       directory at the project root.</li>
 *   <li>Run the tournament — your strategy is discovered and entered automatically.</li>
 * </ol>
 *
 * <h2>The rules</h2>
 * <ul>
 *   <li>Each round you choose {@link Move#COOPERATE} or {@link Move#DEFECT}.</li>
 *   <li>Your score for the round is determined by {@link Payoff}.</li>
 *   <li>You see the full history of past moves (yours and your opponent's)
 *       before making each choice.</li>
 *   <li>You do <em>not</em> know which strategy you are facing.</li>
 * </ul>
 */
public interface Strategy {

    /**
     * Returns the display name of this strategy, e.g., {@code "Tit for Tat"}.
     *
     * @return a short, human-readable name
     */
    String getName();

    /**
     * Returns a one-sentence description of the strategy's logic.
     *
     * @return a brief description
     */
    String getDescription();

    /**
     * Chooses a move for the current round.
     *
     * <p>This method is called once per round with an immutable snapshot of
     * all moves played so far. On the first round, {@code history.getRound()}
     * equals 0 and no previous moves exist.
     *
     * @param history the move history up to but not including this round
     * @return the move to play this round
     */
    Move choose(GameHistory history);

    /**
     * Resets any internal state before a new game begins.
     *
     * <p>Stateless strategies (those that derive all decisions purely from the
     * {@link GameHistory}) may leave this method empty. Stateful strategies
     * such as {@code GrimTrigger} must clear their state here so that a new
     * game starts cleanly.
     */
    void reset();
}

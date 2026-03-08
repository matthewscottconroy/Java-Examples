package com.examples.dilemma.strategy;

/**
 * The two choices available to a player in each round of the Prisoner's Dilemma.
 *
 * <p>A player either <em>cooperates</em> (trusts the other player) or
 * <em>defects</em> (betrays them). The combination of both players' choices
 * determines the payoff via {@link Payoff}.
 */
public enum Move {

    /** Trust the other player this round. */
    COOPERATE,

    /** Betray the other player this round. */
    DEFECT;

    /**
     * Returns the opposite move: COOPERATE becomes DEFECT and vice versa.
     *
     * @return the opposite move
     */
    public Move opposite() {
        return this == COOPERATE ? DEFECT : COOPERATE;
    }

    @Override
    public String toString() {
        return this == COOPERATE ? "C" : "D";
    }
}

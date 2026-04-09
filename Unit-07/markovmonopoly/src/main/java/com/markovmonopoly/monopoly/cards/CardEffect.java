package com.markovmonopoly.monopoly.cards;

/**
 * The type of effect a Chance or Community Chest card has on the player.
 *
 * <p>For Markov chain modeling, only movement effects matter — they determine
 * transitions between board positions. Money effects are tracked for game
 * accuracy but do not change the transition matrix.
 */
public enum CardEffect {
    /** Move to a specific board position (parameter = target index). */
    MOVE_TO,
    /** Move forward by N spaces (parameter = number of spaces). */
    MOVE_FORWARD,
    /** Move back by N spaces (parameter = number of spaces). */
    MOVE_BACK,
    /** Go directly to Jail (state 40); do not pass Go. */
    GO_TO_JAIL,
    /** Advance to the nearest railroad clockwise. */
    NEAREST_RAILROAD,
    /** Advance to the nearest utility clockwise. */
    NEAREST_UTILITY,
    /** Collect money from the bank (parameter = amount); no movement. */
    COLLECT,
    /** Pay money to the bank (parameter = amount); no movement. */
    PAY,
    /** Get Out of Jail Free — saved for later use; no movement. */
    GET_OUT_OF_JAIL_FREE
}

package com.markovmonopoly.monopoly.board;

/**
 * The category of a Monopoly board space.
 *
 * <p>This determines what happens when a player lands on the space:
 * properties can be bought and developed, railroads and utilities are
 * a special type of property, tax spaces deduct money, and the special
 * spaces (Chance, Community Chest, Go To Jail) trigger additional game logic.
 */
public enum SpaceType {
    /** Collect $200 when you pass. */
    GO,
    /** Can be purchased, developed with houses/hotels; landing triggers rent. */
    PROPERTY,
    /** One of the four railroads; rent scales with number owned. */
    RAILROAD,
    /** Electric Company or Water Works; rent is a multiple of the dice roll. */
    UTILITY,
    /** Income Tax ($200) or Luxury Tax ($100). */
    TAX,
    /** Draw a Chance card — may move the player. */
    CHANCE,
    /** Draw a Community Chest card — may move the player. */
    COMMUNITY_CHEST,
    /** No effect; players in jail pass through or wait here (Just Visiting). */
    JUST_VISITING,
    /** Landing here immediately sends the player to Jail (state 40). */
    GO_TO_JAIL,
    /** No effect; a rest square. */
    FREE_PARKING,
    /**
     * Virtual state: the player is currently in jail (not just visiting).
     * This is state index 40 in the 41-state Markov model — it does not
     * correspond to a physical board square.
     */
    IN_JAIL
}

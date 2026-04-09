package com.markovmonopoly.monopoly.board;

/**
 * An immutable representation of a single space on the Monopoly board.
 *
 * @param index     position on the board (0–39), or 40 for the virtual IN_JAIL state
 * @param name      display name (e.g., "Illinois Ave", "Go To Jail")
 * @param type      category of the space
 * @param colorGroup property color group (e.g., "Red"), or empty string for non-properties
 * @param price     purchase price (0 for non-purchasable spaces)
 */
public record BoardSpace(int index, String name, SpaceType type,
                         String colorGroup, int price) {

    /** Convenience constructor for non-property spaces (no color group, no price). */
    public BoardSpace(int index, String name, SpaceType type) {
        this(index, name, type, "", 0);
    }

    /** Returns true if this space can be purchased (property, railroad, or utility). */
    public boolean isPurchasable() {
        return type == SpaceType.PROPERTY
            || type == SpaceType.RAILROAD
            || type == SpaceType.UTILITY;
    }
}

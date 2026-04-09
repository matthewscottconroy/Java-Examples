package com.markovmonopoly.monopoly.cards;

/**
 * A single Chance or Community Chest card.
 *
 * @param description human-readable card text
 * @param effect      what the card does
 * @param parameter   interpretation depends on effect:
 *                    MOVE_TO → target board index;
 *                    MOVE_FORWARD / MOVE_BACK → number of spaces;
 *                    COLLECT / PAY → dollar amount;
 *                    others → unused (0)
 */
public record Card(String description, CardEffect effect, int parameter) {

    /** Returns true if this card causes the player to change position. */
    public boolean isMovement() {
        return switch (effect) {
            case MOVE_TO, MOVE_FORWARD, MOVE_BACK,
                 GO_TO_JAIL, NEAREST_RAILROAD, NEAREST_UTILITY -> true;
            default -> false;
        };
    }
}

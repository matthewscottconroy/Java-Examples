package com.markovmonopoly.monopoly.dice;

/**
 * The result of rolling two six-sided dice.
 *
 * @param die1 value of the first die (1–6)
 * @param die2 value of the second die (1–6)
 */
public record DiceRoll(int die1, int die2) {

    /** The sum of both dice (2–12). */
    public int total() { return die1 + die2; }

    /** True if both dice show the same value. */
    public boolean isDoubles() { return die1 == die2; }

    @Override
    public String toString() {
        return String.format("[%d + %d = %d%s]", die1, die2, total(),
            isDoubles() ? " DOUBLES" : "");
    }
}

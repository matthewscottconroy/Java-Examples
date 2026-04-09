package com.markovmonopoly.monopoly.game;

/**
 * Represents a single player's state in the Monopoly simulation.
 *
 * <p>For Markov chain purposes, the critical state is the player's position on
 * the board (0–39) and whether they are in jail. The "effective state" for the
 * Markov model is returned by {@link #getEffectiveState()}: positions 0–39 for
 * all normal board spaces, and 40 for "In Jail".
 *
 * <p>The money and property fields exist to drive game mechanics (paying to leave
 * jail, for example) but are deliberately simplified — there is no bankruptcy,
 * no auction, and no enforced rent payment — since these do not affect the
 * position dynamics that the Markov chain models.
 */
public final class Player {

    private final String name;
    private int position;       // physical board index, always 0–39
    private boolean inJail;
    private int jailTurns;      // turns spent in jail so far (max 3)
    private int goojfCards;     // Get Out of Jail Free cards held
    private int money;
    private int doublesStreak;  // consecutive doubles rolled this turn sequence

    public Player(String name) {
        this.name   = name;
        this.money  = 1500;
        this.position = 0;
    }

    // -------------------------------------------------------------------------
    // Identity
    // -------------------------------------------------------------------------

    public String getName() { return name; }

    // -------------------------------------------------------------------------
    // Position
    // -------------------------------------------------------------------------

    /** Physical board position (0–39). */
    public int getPosition() { return position; }

    /**
     * Markov chain state index: returns 40 if the player is in jail, otherwise
     * returns the physical position.
     */
    public int getEffectiveState() { return inJail ? 40 : position; }

    /** Moves the player to the given board position (0–39). */
    public void moveTo(int boardIndex) {
        if (boardIndex < 0 || boardIndex >= 40) {
            throw new IllegalArgumentException("Invalid board index: " + boardIndex);
        }
        this.position = boardIndex;
    }

    // -------------------------------------------------------------------------
    // Jail
    // -------------------------------------------------------------------------

    public boolean isInJail()  { return inJail; }
    public int     getJailTurns() { return jailTurns; }

    /** Sends the player to jail (state 40, physical position 10). */
    public void sendToJail() {
        this.position  = 10;   // physical board: Jail square
        this.inJail    = true;
        this.jailTurns = 0;
        this.doublesStreak = 0;
    }

    /** Releases the player from jail (they stay at position 10, just visiting). */
    public void leaveJail() {
        this.inJail    = false;
        this.jailTurns = 0;
    }

    /** Increments the jail turn counter. */
    public void incrementJailTurns() { jailTurns++; }

    // -------------------------------------------------------------------------
    // Get Out of Jail Free
    // -------------------------------------------------------------------------

    public boolean hasGoojfCard()   { return goojfCards > 0; }
    public void    receiveGoojf()   { goojfCards++; }
    public void    useGoojf()       { if (goojfCards > 0) goojfCards--; }
    public int     getGoojfCards()  { return goojfCards; }

    // -------------------------------------------------------------------------
    // Doubles tracking
    // -------------------------------------------------------------------------

    public int  getDoublesStreak()         { return doublesStreak; }
    public void incrementDoublesStreak()   { doublesStreak++; }
    public void resetDoublesStreak()       { doublesStreak = 0; }

    // -------------------------------------------------------------------------
    // Money
    // -------------------------------------------------------------------------

    public int  getMoney()              { return money; }
    public void addMoney(int amount)    { money += amount; }
    public void subtractMoney(int amt)  { money = Math.max(0, money - amt); }
    public boolean canAfford(int cost)  { return money >= cost; }

    @Override
    public String toString() {
        return String.format("Player[%s pos=%d %s money=$%d]",
            name, position, inJail ? "(IN JAIL turn " + jailTurns + ")" : "", money);
    }
}

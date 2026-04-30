package com.patterns.singleton;

/**
 * The stadium scoreboard — the one and only source of truth for a game.
 *
 * <p>Every section of the arena, every broadcast camera, every PA announcement,
 * and every stats-tracking tablet reads from the <em>same</em> scoreboard.
 * There is no second scoreboard. If the home team scores, every observer sees
 * the update instantly, because they all hold a reference to this one object.
 *
 * <p>Implemented with the <em>initialization-on-demand holder</em> idiom:
 * thread-safe, lazily created, zero synchronization overhead after the first call.
 *
 * <p><b>Pattern roles:</b>
 * <pre>
 *   Scoreboard           — the Singleton class
 *   private Scoreboard() — prevents external construction
 *   getInstance()        — the global access point
 *   homeScore, awayScore — shared mutable state
 * </pre>
 */
public final class Scoreboard {

    private String homeTeam  = "Home";
    private String awayTeam  = "Away";
    private int    homeScore = 0;
    private int    awayScore = 0;
    private int    period    = 1;

    private Scoreboard() {}

    // Initialization-on-demand holder — thread-safe without synchronized
    private static final class Holder {
        private static final Scoreboard INSTANCE = new Scoreboard();
    }

    /**
     * Returns the one scoreboard for this game.
     * Every caller — from the press box to the luxury suites — gets the same object.
     *
     * @return the singleton Scoreboard
     */
    public static Scoreboard getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Configures the team names at the start of the game.
     *
     * @param home name of the home team
     * @param away name of the away team
     */
    public void startGame(String home, String away) {
        this.homeTeam  = home;
        this.awayTeam  = away;
        this.homeScore = 0;
        this.awayScore = 0;
        this.period    = 1;
    }

    /**
     * Records points scored by the home team.
     *
     * @param points number of points to add
     */
    public void homeScores(int points) { homeScore += points; }

    /**
     * Records points scored by the away team.
     *
     * @param points number of points to add
     */
    public void awayScores(int points) { awayScore += points; }

    /** Advances the game to the next period. */
    public void nextPeriod() { period++; }

    /** @return the home team's current score */
    public int getHomeScore() { return homeScore; }

    /** @return the away team's current score */
    public int getAwayScore() { return awayScore; }

    /** @return the current period number */
    public int getPeriod() { return period; }

    /** @return the home team's name */
    public String getHomeTeam() { return homeTeam; }

    /** @return the away team's name */
    public String getAwayTeam() { return awayTeam; }

    @Override
    public String toString() {
        return String.format("Period %d | %s %d — %d %s",
                period, homeTeam, homeScore, awayScore, awayTeam);
    }
}

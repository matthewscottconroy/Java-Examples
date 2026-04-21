package com.bugblaster.core;

/**
 * Mutable value object holding all scoring and progression state for one game.
 *
 * <p>A combo is maintained when the player kills bugs within
 * {@value #COMBO_WINDOW_MS} milliseconds of the previous kill. Each kill within
 * a combo awards {@code basePoints × comboMultiplier}. The multiplier caps at
 * {@value #MAX_COMBO}.
 */
public final class GameState {

    /** Starting number of lives. */
    public static final int STARTING_LIVES = 3;

    /** Maximum allowed sticky traps on the board at once. */
    public static final int MAX_TRAPS = 5;

    private static final long COMBO_WINDOW_MS = 1_500;
    private static final int  MAX_COMBO       = 8;

    private int  score;
    private int  lives;
    private int  wave;
    private int  combo;
    private long lastKillTime;
    private boolean gameOver;
    private boolean paused;

    public GameState() {
        reset();
    }

    /** Resets all state to the beginning of a new game. */
    public void reset() {
        score       = 0;
        lives       = STARTING_LIVES;
        wave        = 1;
        combo       = 0;
        lastKillTime = 0;
        gameOver    = false;
        paused      = false;
    }

    /**
     * Records a kill worth {@code basePoints} and returns the points actually
     * awarded (base × current combo multiplier).
     */
    public int recordKill(int basePoints) {
        long now = System.currentTimeMillis();
        if (now - lastKillTime <= COMBO_WINDOW_MS) {
            combo = Math.min(combo + 1, MAX_COMBO);
        } else {
            combo = 1;
        }
        lastKillTime = now;
        int awarded = basePoints * combo;
        score += awarded;
        return awarded;
    }

    /** Called when a bug reaches the Snack Bowl. Triggers game over when lives hit zero. */
    public void loseLife() {
        if (lives > 0) lives--;
        if (lives == 0) gameOver = true;
        combo = 0;
    }

    /** Advances to the next wave. */
    public void advanceWave() { wave++; }

    // ------------------------------------------------------------------ accessors

    public int     getScore()       { return score; }
    public int     getLives()       { return lives; }
    public int     getWave()        { return wave; }
    public int     getCombo()       { return combo; }
    public boolean isGameOver()     { return gameOver; }
    public boolean isPaused()       { return paused; }
    public void    togglePause()    { paused = !paused; }
}

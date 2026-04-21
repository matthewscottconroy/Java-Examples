package com.bugblaster;

import com.bugblaster.core.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link GameState} — scoring, combo system, life management,
 * and game-over transitions.
 */
class GameStateTest {

    private GameState state;

    @BeforeEach
    void setUp() {
        state = new GameState();
    }

    @Test
    void initialStateIsCorrect() {
        assertEquals(0, state.getScore());
        assertEquals(GameState.STARTING_LIVES, state.getLives());
        assertEquals(1, state.getWave());
        assertEquals(0, state.getCombo());
        assertFalse(state.isGameOver());
        assertFalse(state.isPaused());
    }

    @Test
    void firstKillAwardsBasePoints() {
        int awarded = state.recordKill(10);
        assertEquals(10, awarded);
        assertEquals(10, state.getScore());
        assertEquals(1, state.getCombo());
    }

    @Test
    void rapidKillsIncrementCombo() {
        // All kills within the combo window
        state.recordKill(10);   // combo = 1
        state.recordKill(10);   // combo = 2
        int awarded = state.recordKill(10);   // combo = 3 → 10×3 = 30
        assertEquals(3,  state.getCombo());
        assertEquals(30, awarded);
        assertEquals(10 + 20 + 30, state.getScore());
    }

    @Test
    void comboIsCapedAtMaximum() {
        for (int i = 0; i < 20; i++) state.recordKill(1);
        assertTrue(state.getCombo() <= 8, "Combo should not exceed 8");
    }

    @Test
    void loseLifeDecrementsLives() {
        state.loseLife();
        assertEquals(GameState.STARTING_LIVES - 1, state.getLives());
        assertFalse(state.isGameOver());
    }

    @Test
    void losingAllLivesTriggersGameOver() {
        for (int i = 0; i < GameState.STARTING_LIVES; i++) state.loseLife();
        assertEquals(0, state.getLives());
        assertTrue(state.isGameOver());
    }

    @Test
    void livesDoNotGoBelowZero() {
        for (int i = 0; i < GameState.STARTING_LIVES + 5; i++) state.loseLife();
        assertEquals(0, state.getLives());
    }

    @Test
    void loseLifeResetsCombo() {
        state.recordKill(10);
        state.recordKill(10);
        assertEquals(2, state.getCombo());
        state.loseLife();
        assertEquals(0, state.getCombo());
    }

    @Test
    void togglePauseFlipsFlag() {
        assertFalse(state.isPaused());
        state.togglePause();
        assertTrue(state.isPaused());
        state.togglePause();
        assertFalse(state.isPaused());
    }

    @Test
    void advanceWaveIncrementsWave() {
        state.advanceWave();
        assertEquals(2, state.getWave());
        state.advanceWave();
        assertEquals(3, state.getWave());
    }

    @Test
    void resetRestoresInitialState() {
        state.recordKill(100);
        state.loseLife();
        state.advanceWave();
        state.togglePause();

        state.reset();

        assertEquals(0, state.getScore());
        assertEquals(GameState.STARTING_LIVES, state.getLives());
        assertEquals(1, state.getWave());
        assertEquals(0, state.getCombo());
        assertFalse(state.isGameOver());
        assertFalse(state.isPaused());
    }
}

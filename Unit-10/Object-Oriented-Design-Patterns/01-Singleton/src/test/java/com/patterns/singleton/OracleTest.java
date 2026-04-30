package com.patterns.singleton;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Scoreboard Singleton.
 */
class OracleTest {

    @BeforeEach
    void resetGame() {
        Scoreboard.getInstance().startGame("Home", "Away");
    }

    @Test
    @DisplayName("getInstance always returns the identical object")
    void singletonIdentity() {
        Scoreboard a = Scoreboard.getInstance();
        Scoreboard b = Scoreboard.getInstance();
        assertSame(a, b);
    }

    @Test
    @DisplayName("Score updates are visible through any reference")
    void sharedState() {
        Scoreboard ref1 = Scoreboard.getInstance();
        Scoreboard ref2 = Scoreboard.getInstance();

        ref1.homeScores(3);
        assertEquals(3, ref2.getHomeScore(),
                "Score added via ref1 must be visible through ref2");
    }

    @Test
    @DisplayName("homeScores and awayScores accumulate correctly")
    void scoringAccumulates() {
        Scoreboard board = Scoreboard.getInstance();
        board.homeScores(2);
        board.homeScores(3);
        board.awayScores(2);

        assertEquals(5, board.getHomeScore());
        assertEquals(2, board.getAwayScore());
    }

    @Test
    @DisplayName("nextPeriod increments period number")
    void nextPeriod() {
        Scoreboard board = Scoreboard.getInstance();
        assertEquals(1, board.getPeriod());
        board.nextPeriod();
        assertEquals(2, board.getPeriod());
    }

    @Test
    @DisplayName("toString contains both team names and scores")
    void toStringFormat() {
        Scoreboard board = Scoreboard.getInstance();
        board.startGame("Lakers", "Celtics");
        board.homeScores(5);
        String s = board.toString();
        assertTrue(s.contains("Lakers"));
        assertTrue(s.contains("Celtics"));
        assertTrue(s.contains("5"));
    }
}

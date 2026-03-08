package com.examples.dilemma.engine;

import com.examples.dilemma.strategy.Move;
import com.examples.dilemma.strategy.Payoff;
import com.examples.dilemma.strategy.impl.AlwaysCooperate;
import com.examples.dilemma.strategy.impl.AlwaysDefect;
import com.examples.dilemma.strategy.impl.TitForTat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Game")
class GameTest {

    @Test
    @DisplayName("game constructor rejects non-positive round counts")
    void constructorRejectsNonPositiveRounds() {
        assertThrows(IllegalArgumentException.class, () -> new Game(0));
        assertThrows(IllegalArgumentException.class, () -> new Game(-5));
    }

    @Test
    @DisplayName("AlwaysCooperate vs AlwaysCooperate: both earn Reward every round")
    void mutualCooperation() {
        MatchResult r = new Game(10).play(new AlwaysCooperate(), new AlwaysCooperate());
        assertEquals(10 * Payoff.REWARD, r.scoreA());
        assertEquals(10 * Payoff.REWARD, r.scoreB());
        assertTrue(r.isDraw());
        assertEquals("Draw", r.winner());
    }

    @Test
    @DisplayName("AlwaysDefect vs AlwaysCooperate: defector earns Temptation, cooperator earns Sucker")
    void defectorExploitsCooperator() {
        MatchResult r = new Game(10).play(new AlwaysDefect(), new AlwaysCooperate());
        assertEquals(10 * Payoff.TEMPTATION, r.scoreA());
        assertEquals(10 * Payoff.SUCKER,     r.scoreB());
        assertEquals("Always Defect", r.winner());
    }

    @Test
    @DisplayName("AlwaysDefect vs AlwaysDefect: both earn Punishment every round")
    void mutualDefection() {
        MatchResult r = new Game(10).play(new AlwaysDefect(), new AlwaysDefect());
        assertEquals(10 * Payoff.PUNISHMENT, r.scoreA());
        assertEquals(10 * Payoff.PUNISHMENT, r.scoreB());
        assertTrue(r.isDraw());
    }

    @Test
    @DisplayName("TitForTat vs AlwaysCooperate: mutual cooperation after round 1")
    void titForTatVsAlwaysCooperate() {
        MatchResult r = new Game(100).play(new TitForTat(), new AlwaysCooperate());
        // Both cooperate every round => both earn REWARD * 100
        assertEquals(100 * Payoff.REWARD, r.scoreA());
        assertEquals(100 * Payoff.REWARD, r.scoreB());
    }

    @Test
    @DisplayName("TitForTat vs AlwaysDefect: TfT cooperates round 1, then retaliates")
    void titForTatVsAlwaysDefect() {
        MatchResult r = new Game(10).play(new TitForTat(), new AlwaysDefect());
        // Round 1: TfT=C, AD=D  → TfT gets Sucker, AD gets Temptation
        // Rounds 2-10: TfT=D, AD=D → both get Punishment
        int expectedTft = Payoff.SUCKER + 9 * Payoff.PUNISHMENT;
        int expectedAd  = Payoff.TEMPTATION + 9 * Payoff.PUNISHMENT;
        assertEquals(expectedTft, r.scoreA());
        assertEquals(expectedAd,  r.scoreB());
    }

    @Test
    @DisplayName("move lists have the correct length")
    void moveListLength() {
        MatchResult r = new Game(50).play(new TitForTat(), new AlwaysCooperate());
        assertEquals(50, r.movesA().size());
        assertEquals(50, r.movesB().size());
    }

    @Test
    @DisplayName("TitForTat vs AlwaysCooperate move lists are all COOPERATE")
    void titForTatVsCooperateMoves() {
        MatchResult r = new Game(5).play(new TitForTat(), new AlwaysCooperate());
        r.movesA().forEach(m -> assertEquals(Move.COOPERATE, m));
        r.movesB().forEach(m -> assertEquals(Move.COOPERATE, m));
    }

    @Test
    @DisplayName("strategies are reset between games")
    void strategyResetBetweenGames() {
        Game game = new Game(10);
        TitForTat tft = new TitForTat();
        AlwaysCooperate ac = new AlwaysCooperate();
        MatchResult r1 = game.play(tft, ac);
        MatchResult r2 = game.play(tft, ac);
        assertEquals(r1.scoreA(), r2.scoreA());
        assertEquals(r1.scoreB(), r2.scoreB());
    }
}

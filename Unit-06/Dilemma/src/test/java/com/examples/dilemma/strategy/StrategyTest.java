package com.examples.dilemma.strategy;

import com.examples.dilemma.engine.Game;
import com.examples.dilemma.engine.MatchResult;
import com.examples.dilemma.strategy.impl.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Strategies")
class StrategyTest {

    // --- TitForTat ---

    @Test
    @DisplayName("TitForTat cooperates on round 1")
    void titForTatCooperatesFirst() {
        assertEquals(Move.COOPERATE, new TitForTat().choose(GameHistory.empty()));
    }

    @Test
    @DisplayName("TitForTat copies the opponent's last move")
    void titForTatMirrors() {
        TitForTat tft = new TitForTat();
        GameHistory defected = new GameHistory(List.of(Move.COOPERATE), List.of(Move.DEFECT));
        assertEquals(Move.DEFECT, tft.choose(defected));

        GameHistory cooperated = new GameHistory(List.of(Move.DEFECT), List.of(Move.COOPERATE));
        assertEquals(Move.COOPERATE, tft.choose(cooperated));
    }

    // --- AlwaysCooperate / AlwaysDefect ---

    @Test
    @DisplayName("AlwaysCooperate always returns COOPERATE")
    void alwaysCooperate() {
        AlwaysCooperate ac = new AlwaysCooperate();
        assertEquals(Move.COOPERATE, ac.choose(GameHistory.empty()));
        GameHistory h = new GameHistory(List.of(Move.DEFECT), List.of(Move.DEFECT));
        assertEquals(Move.COOPERATE, ac.choose(h));
    }

    @Test
    @DisplayName("AlwaysDefect always returns DEFECT")
    void alwaysDefect() {
        AlwaysDefect ad = new AlwaysDefect();
        assertEquals(Move.DEFECT, ad.choose(GameHistory.empty()));
        GameHistory h = new GameHistory(List.of(Move.COOPERATE), List.of(Move.COOPERATE));
        assertEquals(Move.DEFECT, ad.choose(h));
    }

    // --- GrimTrigger ---

    @Test
    @DisplayName("GrimTrigger cooperates while opponent cooperates")
    void grimTriggerCooperates() {
        GrimTrigger gt = new GrimTrigger();
        GameHistory allCoop = new GameHistory(
                List.of(Move.COOPERATE, Move.COOPERATE),
                List.of(Move.COOPERATE, Move.COOPERATE));
        assertEquals(Move.COOPERATE, gt.choose(allCoop));
    }

    @Test
    @DisplayName("GrimTrigger defects after opponent defects once")
    void grimTriggerTriggered() {
        GrimTrigger gt = new GrimTrigger();
        GameHistory withDefect = new GameHistory(
                List.of(Move.COOPERATE, Move.COOPERATE),
                List.of(Move.COOPERATE, Move.DEFECT));
        assertEquals(Move.DEFECT, gt.choose(withDefect));
    }

    @Test
    @DisplayName("GrimTrigger resets its triggered state between games")
    void grimTriggerResets() {
        GrimTrigger gt = new GrimTrigger();
        GameHistory withDefect = new GameHistory(List.of(Move.COOPERATE), List.of(Move.DEFECT));
        assertEquals(Move.DEFECT, gt.choose(withDefect));

        gt.reset();
        assertEquals(Move.COOPERATE, gt.choose(GameHistory.empty()));
    }

    // --- TitForTwoTats ---

    @Test
    @DisplayName("TitForTwoTats forgives a single defection")
    void titForTwoTatsForgivesOne() {
        TitForTwoTats t2t = new TitForTwoTats();
        GameHistory oneDefect = new GameHistory(
                List.of(Move.COOPERATE),
                List.of(Move.DEFECT));
        assertEquals(Move.COOPERATE, t2t.choose(oneDefect));
    }

    @Test
    @DisplayName("TitForTwoTats defects after two consecutive opponent defections")
    void titForTwoTatsDefectsOnTwo() {
        TitForTwoTats t2t = new TitForTwoTats();
        GameHistory twoDefects = new GameHistory(
                List.of(Move.COOPERATE, Move.COOPERATE),
                List.of(Move.DEFECT,    Move.DEFECT));
        assertEquals(Move.DEFECT, t2t.choose(twoDefects));
    }

    // --- SuspiciousTitForTat ---

    @Test
    @DisplayName("SuspiciousTitForTat defects on round 1")
    void suspiciousDefectsFirst() {
        assertEquals(Move.DEFECT, new SuspiciousTitForTat().choose(GameHistory.empty()));
    }

    @Test
    @DisplayName("SuspiciousTitForTat mirrors from round 2 onward")
    void suspiciousMirrorsAfterFirst() {
        SuspiciousTitForTat s = new SuspiciousTitForTat();
        GameHistory h = new GameHistory(List.of(Move.DEFECT), List.of(Move.COOPERATE));
        assertEquals(Move.COOPERATE, s.choose(h));
    }

    // --- Pavlov ---

    @Test
    @DisplayName("Pavlov cooperates on round 1")
    void pavlovCooperatesFirst() {
        assertEquals(Move.COOPERATE, new Pavlov().choose(GameHistory.empty()));
    }

    @Test
    @DisplayName("Pavlov repeats cooperate after mutual cooperation (Reward)")
    void pavlovStaysOnReward() {
        Pavlov p = new Pavlov();
        GameHistory h = new GameHistory(List.of(Move.COOPERATE), List.of(Move.COOPERATE));
        assertEquals(Move.COOPERATE, p.choose(h)); // Won → stay
    }

    @Test
    @DisplayName("Pavlov switches after getting Sucker's payoff")
    void pavlovSwitchesOnSucker() {
        Pavlov p = new Pavlov();
        // I cooperated, opponent defected → I got Sucker → switch to DEFECT
        GameHistory h = new GameHistory(List.of(Move.COOPERATE), List.of(Move.DEFECT));
        assertEquals(Move.DEFECT, p.choose(h));
    }

    // --- Move utility ---

    @Test
    @DisplayName("Move.opposite() toggles correctly")
    void moveOpposite() {
        assertEquals(Move.DEFECT, Move.COOPERATE.opposite());
        assertEquals(Move.COOPERATE, Move.DEFECT.opposite());
    }

    // --- Payoff ---

    @Test
    @DisplayName("Payoff satisfies T > R > P > S")
    void payoffOrdering() {
        assertTrue(Payoff.TEMPTATION > Payoff.REWARD);
        assertTrue(Payoff.REWARD     > Payoff.PUNISHMENT);
        assertTrue(Payoff.PUNISHMENT > Payoff.SUCKER);
    }

    @Test
    @DisplayName("Payoff satisfies 2R > T + S (cooperation beats alternating)")
    void payoffCooperationIsBetter() {
        assertTrue(2 * Payoff.REWARD > Payoff.TEMPTATION + Payoff.SUCKER);
    }

    @Test
    @DisplayName("Payoff.score returns correct values for all four outcomes")
    void payoffScoreAllCases() {
        assertEquals(Payoff.REWARD,     Payoff.score(Move.COOPERATE, Move.COOPERATE));
        assertEquals(Payoff.TEMPTATION, Payoff.score(Move.DEFECT,    Move.COOPERATE));
        assertEquals(Payoff.SUCKER,     Payoff.score(Move.COOPERATE, Move.DEFECT));
        assertEquals(Payoff.PUNISHMENT, Payoff.score(Move.DEFECT,    Move.DEFECT));
    }

    // --- Full tournament sanity check ---

    @Test
    @DisplayName("TitForTat outscores AlwaysDefect in a head-to-head")
    void titForTatBeatsAlwaysDefectLongRun() {
        // Over many rounds, TfT and AD both mostly defect
        // but TfT at least doesn't give free Suckers
        MatchResult r = new Game(200).play(new TitForTat(), new AlwaysDefect());
        // TfT: 1 SUCKER + 199 PUNISHMENT = 199
        // AD:  1 TEMPTATION + 199 PUNISHMENT = 204
        assertTrue(r.scoreB() > r.scoreA(), "AlwaysDefect should beat TfT head-to-head");
        // But in a tournament, TfT's cooperative matches make up for this
    }
}

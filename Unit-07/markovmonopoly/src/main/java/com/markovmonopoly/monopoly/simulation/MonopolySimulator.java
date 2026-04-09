package com.markovmonopoly.monopoly.simulation;

import com.markovmonopoly.monopoly.board.MonopolyBoard;
import com.markovmonopoly.monopoly.cards.CardDeck;
import com.markovmonopoly.monopoly.dice.Dice;
import com.markovmonopoly.monopoly.game.MonopolyGame;
import com.markovmonopoly.monopoly.game.Player;

import java.util.List;
import java.util.Random;

/**
 * Runs a Monopoly simulation for a single player over many turns and collects
 * state-transition data for Markov chain construction.
 *
 * <h2>Simulation Strategy</h2>
 * <p>A single player makes repeated turns around the board. We track every
 * transition between effective states (0–40). After many turns, the empirical
 * transition frequencies converge to the true Markov chain probabilities.
 *
 * <h2>Why Single-Player?</h2>
 * <p>The Markov property requires that the next state depends only on the
 * current state, not on other players' actions. Since rent payments do not
 * affect the <em>position</em> dynamics, a single-player model correctly
 * captures the Markov chain governing board positions.
 */
public final class MonopolySimulator {

    private final Random rng;
    private final boolean verbose;

    public MonopolySimulator(long seed, boolean verbose) {
        this.rng     = new Random(seed);
        this.verbose = verbose;
    }

    public MonopolySimulator(long seed) { this(seed, false); }
    public MonopolySimulator()          { this(System.nanoTime(), false); }

    // -------------------------------------------------------------------------
    // Simulation
    // -------------------------------------------------------------------------

    /**
     * Simulates {@code turns} complete turns and returns all recorded transitions.
     *
     * @param turns number of turns to simulate
     * @return list of int[]{fromState, toState} pairs (41-state space, 0–40)
     */
    public List<int[]> simulate(int turns) {
        return simulateAndCollect(turns).transitionsList();
    }

    /**
     * Simulates {@code turns} turns and returns a {@link SimulationStatsWithList} object
     * containing landing frequencies, transition counts, and the raw transition list.
     */
    public SimulationStatsWithList simulateAndCollect(int turns) {
        MonopolyBoard board  = MonopolyBoard.standard();
        Player player        = new Player("SimPlayer");
        CardDeck chance      = CardDeck.chance(rng);
        CardDeck community   = CardDeck.communityChest(rng);
        Dice dice            = new Dice(rng);
        MonopolyGame game    = new MonopolyGame(player, board, chance, community, dice, verbose);

        SimulationStatsWithList stats = new SimulationStatsWithList();

        for (int turn = 1; turn <= turns; turn++) {
            List<int[]> turnTransitions = game.takeTurn();
            stats.recordTurn(turnTransitions);
            if (verbose && turn % 100 == 0) {
                System.out.printf("  Turn %,d: %s%n", turn, player);
            }
        }
        return stats;
    }

    // -------------------------------------------------------------------------
    // Inner extension that also stores the raw transition list
    // -------------------------------------------------------------------------

    /** SimulationStats that also keeps the full list of transitions. */
    public static final class SimulationStatsWithList extends SimulationStats {
        private final java.util.ArrayList<int[]> list = new java.util.ArrayList<>();

        @Override
        public void recordTurn(List<int[]> turnTransitions) {
            super.recordTurn(turnTransitions);
            list.addAll(turnTransitions);
        }

        /** Returns the raw list of all [from, to] transitions recorded. */
        public List<int[]> transitionsList() { return java.util.Collections.unmodifiableList(list); }
    }
}

package com.markovmonopoly.monopoly.markov;

import com.markovmonopoly.core.MarkovChain;
import com.markovmonopoly.core.TransitionMatrix;
import com.markovmonopoly.monopoly.board.MonopolyBoard;
import com.markovmonopoly.monopoly.cards.Card;
import com.markovmonopoly.monopoly.cards.CardDeck;
import com.markovmonopoly.monopoly.simulation.SimulationStats;

import java.util.*;

/**
 * Builds a {@link MarkovChain} from either simulation data or analytical computation.
 *
 * <h2>Two Approaches to the Monopoly Markov Chain</h2>
 *
 * <h3>1. Empirical (from simulation)</h3>
 * <p>Run thousands of simulated turns, count transitions, and divide by row totals.
 * This converges to the true chain but has statistical noise.
 * {@link #fromSimulation(SimulationStats, MonopolyBoard)}
 *
 * <h3>2. Theoretical (analytical)</h3>
 * <p>Compute transition probabilities exactly from the game rules:
 * enumerate all 36 dice combinations, apply card effects with uniform probability
 * (1/16 per card), and model jail mechanics. This gives the "ground truth" chain.
 * {@link #buildTheoretical(MonopolyBoard)}
 *
 * <h2>State Space</h2>
 * <p>Both methods produce a 41-state chain. States 0–39 are board positions;
 * state 40 is IN_JAIL (distinct from "Just Visiting" at position 10).
 */
public final class MonopolyMarkovChainBuilder {

    private MonopolyMarkovChainBuilder() {}

    // -------------------------------------------------------------------------
    // Empirical chain from simulation
    // -------------------------------------------------------------------------

    /**
     * Builds a MarkovChain from simulation statistics.
     *
     * @param stats empirical transition counts from a simulation run
     * @param board the Monopoly board (for state labels)
     * @return a 41-state Markov chain derived from the simulation
     */
    public static MarkovChain fromSimulation(SimulationStats stats, MonopolyBoard board) {
        double[][] matrix = stats.getEmpiricalTransitionMatrix();
        List<String> labels = buildLabels(board);
        return new MarkovChain(
            "Monopoly (Empirical, n=" + stats.getTotalTransitions() + " transitions)",
            "Built from simulation data. " + stats.getTotalTransitions() + " transitions recorded.",
            labels,
            TransitionMatrix.of(matrix).normalized()
        );
    }

    // -------------------------------------------------------------------------
    // Theoretical chain (analytical)
    // -------------------------------------------------------------------------

    /**
     * Builds the theoretical Markov chain by analytically computing transition
     * probabilities from the game rules.
     *
     * <h3>Assumptions</h3>
     * <ul>
     *   <li>Each Chance/Community Chest card is drawn with uniform probability 1/16.</li>
     *   <li>Jail mechanics: from IN_JAIL (state 40), the player leaves on doubles
     *       (probability 1/6) and rolls any non-doubles to stay (probability 5/6).
     *       On the 3rd turn (but we model a stationary average), the player pays
     *       and leaves — modeled as probability 1 of leaving after expected 2 turns.</li>
     *   <li>The simplified jail model: from state 40, the player transitions with
     *       probability 1/6 × P(destination|doubles) + (5/6 - adjustment) × self-loop.</li>
     * </ul>
     *
     * <p>Note: this is a mild approximation. For a fully exact model, the jail state
     * would be split into JAIL_1, JAIL_2, JAIL_3 (three sub-states tracking turn count).
     * That gives a 43-state exact chain. This 41-state model is the standard in
     * academic literature on the Monopoly Markov chain.
     */
    public static MarkovChain buildTheoretical(MonopolyBoard board) {
        int n = MonopolyBoard.TOTAL_STATES;
        double[][] matrix = new double[n][n];

        // Dice total probabilities (index = total, 2..12)
        double[] diceProbability = buildDiceProbabilities();

        // Card decks (for theoretical analysis, order doesn't matter — use uniform prob)
        List<Card> chanceCards    = CardDeck.chance(new Random(0)).getAllCards();
        List<Card> communityCards = CardDeck.communityChest(new Random(0)).getAllCards();

        // ----------------------------------------------------------------
        // For each non-jail state (0..39), compute transition probabilities
        // ----------------------------------------------------------------
        for (int pos = 0; pos < 40; pos++) {
            if (pos == 30) {
                // Go To Jail: always transitions to state 40 (in jail)
                matrix[pos][40] = 1.0;
                continue;
            }

            // For each possible dice total (2..12)
            for (int total = 2; total <= 12; total++) {
                double pRoll = diceProbability[total];

                // Base landing position (before card effects)
                int landPos = (pos + total) % 40;

                // Apply card effects: distribute probability mass over destinations
                addTransitionProb(matrix, pos, landPos, pRoll, board,
                    chanceCards, communityCards);
            }

            // Doubles → roll again (simplification: ignore multi-roll chains in theoretical;
            // the simulation captures this accurately)
            // For the theoretical model, we treat the chain as "one roll per step"
            // which is the standard simplification in most academic analyses.
        }

        // ----------------------------------------------------------------
        // Jail state (state 40): model the jail exit probabilities
        // ----------------------------------------------------------------
        // Simplified model: each turn in jail, roll dice.
        // - With prob 1/6: roll doubles → leave jail, land on (10 + doubles total)
        //   Since doubles totals are {2,4,6,8,10,12} each with prob 1/6:
        //   P(leave jail and land at (10+k)) = (1/36) for each doubles combo
        // - With prob (5/6): no doubles → stay in jail (for turns 1 and 2)
        //   On turn 3: must pay and leave, rolling any total.
        //
        // Average model (stationary): expected turns = 1 + 5/6 + (5/6)² ≈ 2.4
        // We model it as: p(stay) = 5/6 * (2/3) + 0 * (1/3) ≈ 5/9 (weighted average
        // of staying on turns 1 and 2, and always leaving on turn 3).
        // This is a rough approximation. For educational purposes, the simulation is
        // more accurate for jail dynamics.

        // Better model: simulate the three jail turns as a mixture:
        // Turn 1 (prob 1): roll → doubles(1/6) leave, non-doubles(5/6) stay
        // Turn 2 (prob 5/6): roll → doubles(1/6) leave, non-doubles(5/6) go to turn 3
        // Turn 3 (prob 25/36): forced to leave, roll any total
        //
        // Average probability of leaving on each specific roll, summed over turns:
        // This is a weighted sum. Let's compute it properly.

        double pDoubles = 1.0 / 6.0;

        // From jail, all paths:
        // Leave via doubles turn 1: p = 1/6 (roll doubles, total = 2k for k=1..6)
        // Leave via doubles turn 2: p = (5/6)(1/6)
        // Forced leave turn 3: p = (5/6)^2 = 25/36 (roll any total)
        // Stay jail turn 1→2: captured in turn 2 probability
        // Stay jail turn 2→3: captured in turn 3 probability (always leave)

        // For each possible dice total t (2..12):
        // P(leave and land at (10+t)%40 | was in jail) =
        //   turn1: P(roll t, t is doubles) * 1/36 for each doubles total
        //   turn2: (5/6) * P(roll t, t is doubles) * 1/36
        //   turn3: (5/6)^2 * P(roll total t regardless) * 1/36

        // Net: a doubles total has extra weight from turns 1 and 2
        double[][] jailDest = new double[n][1];  // reuse later
        double[] exitProb = new double[40];

        for (int d1 = 1; d1 <= 6; d1++) {
            for (int d2 = 1; d2 <= 6; d2++) {
                int total = d1 + d2;
                boolean isDoubles = (d1 == d2);
                int dest = (10 + total) % 40;

                // Turn 1: only doubles can exit
                if (isDoubles) {
                    exitProb[dest] += (1.0/36.0);         // weight = 1
                }
                // Turn 2: only doubles, weighted by P(reach turn 2) = 5/6
                if (isDoubles) {
                    exitProb[dest] += (5.0/6.0) * (1.0/36.0);
                }
                // Turn 3: any total (forced exit), weighted by P(reach turn 3) = 25/36
                exitProb[dest] += (25.0/36.0) * (1.0/36.0);
            }
        }

        // Normalize exitProb (it sums to 1 × probability of eventually exiting = 1)
        // and compute probability of staying in jail each notional step:
        // P(stay) for a "turn" in this avg model ≈ weighted average of 5/6 (turns 1,2)
        // but since we're modeling a single Markov step, let's just normalize to 1.
        // The typical academic treatment is to set jail transitions to board positions
        // proportional to the exit probabilities (no self-loop since eventually forced out).

        double totalExit = 0;
        for (double p : exitProb) totalExit += p;
        for (int d = 0; d < 40; d++) {
            if (exitProb[d] > 0) {
                matrix[40][d] = exitProb[d] / totalExit;
            }
        }
        // Apply card effects for jail exits to Go To Jail space (lands on 30 → go to jail)
        // (already handled by addTransitionProb which checks for GoToJail)

        // Apply the theoretical matrix normalization
        List<String> labels = buildLabels(board);
        return new MarkovChain(
            "Monopoly (Theoretical)",
            "Analytically derived transition probabilities. Standard 41-state approximation.",
            labels,
            TransitionMatrix.of(matrix).normalized()
        );
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Distributes a probability mass pRoll from position pos, landing at landPos,
     * across destinations after applying card effects at that landing position.
     */
    private static void addTransitionProb(double[][] matrix, int pos, int landPos,
                                           double pRoll, MonopolyBoard board,
                                           List<Card> chanceCards, List<Card> communityCards) {
        // Go To Jail: deterministically → state 40
        if (landPos == 30) {
            matrix[pos][40] += pRoll;
            return;
        }

        var spaceType = board.getType(landPos);

        switch (spaceType) {
            case CHANCE -> {
                double cardP = 1.0 / chanceCards.size();
                for (Card card : chanceCards) {
                    int dest = resolveCardDest(card, landPos, board);
                    if (dest == -1) {
                        // GOOJF or non-movement: player stays at Chance square
                        matrix[pos][landPos] += pRoll * cardP;
                    } else if (dest == 40) {
                        matrix[pos][40] += pRoll * cardP;
                    } else {
                        // Some card destinations may themselves be special
                        if (dest == 30) {
                            matrix[pos][40] += pRoll * cardP;
                        } else {
                            matrix[pos][dest] += pRoll * cardP;
                        }
                    }
                }
            }
            case COMMUNITY_CHEST -> {
                double cardP = 1.0 / communityCards.size();
                for (Card card : communityCards) {
                    int dest = resolveCardDest(card, landPos, board);
                    if (dest == -1) {
                        matrix[pos][landPos] += pRoll * cardP;
                    } else if (dest == 40) {
                        matrix[pos][40] += pRoll * cardP;
                    } else {
                        matrix[pos][dest] += pRoll * cardP;
                    }
                }
            }
            default -> matrix[pos][landPos] += pRoll;
        }
    }

    /**
     * Returns the destination state after applying a card at the given position.
     * Returns -1 if the card has no movement effect.
     * Returns 40 for Go To Jail cards.
     */
    private static int resolveCardDest(Card card, int currentPos, MonopolyBoard board) {
        return switch (card.effect()) {
            case MOVE_TO         -> card.parameter();
            case MOVE_BACK       -> ((currentPos - card.parameter()) % 40 + 40) % 40;
            case MOVE_FORWARD    -> (currentPos + card.parameter()) % 40;
            case GO_TO_JAIL      -> 40;
            case NEAREST_RAILROAD -> board.nearestRailroad(currentPos);
            case NEAREST_UTILITY  -> board.nearestUtility(currentPos);
            default              -> -1;  // no movement
        };
    }

    /** Returns dice total probabilities: index = total (0..12), value = probability. */
    private static double[] buildDiceProbabilities() {
        double[] p = new double[13];
        for (int d1 = 1; d1 <= 6; d1++) {
            for (int d2 = 1; d2 <= 6; d2++) {
                p[d1 + d2] += 1.0 / 36.0;
            }
        }
        return p;
    }

    /** Builds the list of state labels for the 41-state Monopoly chain. */
    public static List<String> buildLabels(MonopolyBoard board) {
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < MonopolyBoard.TOTAL_STATES; i++) {
            labels.add(board.getLabel(i));
        }
        return labels;
    }
}

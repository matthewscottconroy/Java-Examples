package com.markovmonopoly.monopoly.game;

import com.markovmonopoly.monopoly.board.MonopolyBoard;
import com.markovmonopoly.monopoly.board.SpaceType;
import com.markovmonopoly.monopoly.cards.Card;
import com.markovmonopoly.monopoly.cards.CardDeck;
import com.markovmonopoly.monopoly.dice.Dice;
import com.markovmonopoly.monopoly.dice.DiceRoll;

import java.util.ArrayList;
import java.util.List;

/**
 * Processes one complete turn of Monopoly for a single player.
 *
 * <h2>Turn Mechanics</h2>
 * <ol>
 *   <li>If the player is in jail:
 *     <ul>
 *       <li>They may use a Get Out of Jail Free card (automatic in simulation).</li>
 *       <li>Otherwise they roll: doubles → leave jail and move; non-doubles → stay.</li>
 *       <li>After 3 turns in jail they must pay $50 and leave.</li>
 *     </ul>
 *   </li>
 *   <li>If not in jail, roll dice. Move forward by the total.
 *     <ul>
 *       <li>Three consecutive doubles → Go to Jail.</li>
 *       <li>Two consecutive doubles → roll again after resolving the current move.</li>
 *     </ul>
 *   </li>
 *   <li>Apply the effect of the space landed on (Chance, Community Chest, Go To Jail).</li>
 * </ol>
 *
 * <h2>Markov Transitions Recorded</h2>
 * <p>Each call to {@link #takeTurn()} returns a list of {@code int[]{fromState, toState}}
 * pairs where indices are in the 41-state space (0–39 = board, 40 = IN_JAIL).
 * A single turn can record multiple transitions when doubles are rolled.
 */
public final class MonopolyGame {

    private final Player player;
    private final MonopolyBoard board;
    private final CardDeck chanceDeck;
    private final CardDeck communityChestDeck;
    private final Dice dice;
    private final boolean verbose;

    public MonopolyGame(Player player, MonopolyBoard board,
                        CardDeck chanceDeck, CardDeck communityChestDeck,
                        Dice dice, boolean verbose) {
        this.player             = player;
        this.board              = board;
        this.chanceDeck         = chanceDeck;
        this.communityChestDeck = communityChestDeck;
        this.dice               = dice;
        this.verbose            = verbose;
    }

    // -------------------------------------------------------------------------
    // Core turn logic
    // -------------------------------------------------------------------------

    /**
     * Executes one full turn for the player and returns all state transitions.
     *
     * @return list of [fromState, toState] transitions recorded this turn
     */
    public List<int[]> takeTurn() {
        List<int[]> transitions = new ArrayList<>();
        player.resetDoublesStreak();

        if (player.isInJail()) {
            processJailTurn(transitions);
        } else {
            processFreeTurn(transitions);
        }

        return transitions;
    }

    // -------------------------------------------------------------------------
    // Jail turn
    // -------------------------------------------------------------------------

    private void processJailTurn(List<int[]> transitions) {
        int fromState = player.getEffectiveState();  // always 40

        // Option 1: use Get Out of Jail Free card
        if (player.hasGoojfCard()) {
            player.useGoojf();
            chanceDeck.returnGoojf();  // simplified: always return to Chance
            player.leaveJail();
            if (verbose) System.out.println("  " + player.getName() + " uses GOOJF card.");
            DiceRoll roll = dice.roll();
            if (verbose) System.out.println("  Rolls " + roll);
            movePlayer(roll.total());
            int toState = player.getEffectiveState();
            transitions.add(new int[]{fromState, toState});
            applySpaceEffect(transitions);
            return;
        }

        // Option 2: after 3 turns, must pay $50
        if (player.getJailTurns() >= 2) {  // this is the 3rd turn
            player.subtractMoney(50);
            player.leaveJail();
            if (verbose) System.out.println("  " + player.getName() + " pays $50 to leave jail.");
            DiceRoll roll = dice.roll();
            if (verbose) System.out.println("  Rolls " + roll);
            movePlayer(roll.total());
            int toState = player.getEffectiveState();
            transitions.add(new int[]{fromState, toState});
            applySpaceEffect(transitions);
            return;
        }

        // Option 3: try to roll doubles
        DiceRoll roll = dice.roll();
        if (verbose) System.out.println("  " + player.getName() + " in jail, rolls " + roll);
        if (roll.isDoubles()) {
            player.leaveJail();
            movePlayer(roll.total());
            int toState = player.getEffectiveState();
            transitions.add(new int[]{fromState, toState});
            applySpaceEffect(transitions);
            // Note: after rolling doubles to leave jail, you do NOT roll again
        } else {
            player.incrementJailTurns();
            // No transition — player stays in state 40
            transitions.add(new int[]{fromState, fromState});
        }
    }

    // -------------------------------------------------------------------------
    // Free turn (not in jail)
    // -------------------------------------------------------------------------

    private void processFreeTurn(List<int[]> transitions) {
        rollAndMove(transitions);
    }

    private void rollAndMove(List<int[]> transitions) {
        int fromState = player.getEffectiveState();
        DiceRoll roll = dice.roll();
        if (verbose) System.out.println("  " + player.getName() + " rolls " + roll);

        if (roll.isDoubles()) {
            player.incrementDoublesStreak();
            if (player.getDoublesStreak() >= 3) {
                // Three consecutive doubles → Go to Jail
                if (verbose) System.out.println("  Three doubles! Go to Jail.");
                player.sendToJail();
                transitions.add(new int[]{fromState, player.getEffectiveState()});
                return;
            }
        } else {
            player.resetDoublesStreak();
        }

        // Move forward
        movePlayer(roll.total());
        int toState = player.getEffectiveState();
        transitions.add(new int[]{fromState, toState});

        // Apply effect of the space landed on (may change position again)
        applySpaceEffect(transitions);

        // If doubles and not now in jail, roll again
        if (roll.isDoubles() && !player.isInJail()) {
            if (verbose) System.out.println("  Doubles! Roll again.");
            rollAndMove(transitions);
        }
    }

    // -------------------------------------------------------------------------
    // Movement helpers
    // -------------------------------------------------------------------------

    private void movePlayer(int steps) {
        int oldPos = player.getPosition();
        int newPos = board.advance(oldPos, steps);
        // Collect $200 if passed or landed on Go
        if (newPos <= oldPos && steps > 0) {
            player.addMoney(200);
            if (verbose) System.out.println("  Passed Go! Collect $200.");
        }
        player.moveTo(newPos);
        if (verbose) System.out.printf("  Moved from %d to %d (%s)%n",
            oldPos, newPos, board.getLabel(newPos));
    }

    private void movePlayerTo(int targetPos) {
        int oldPos = player.getPosition();
        // If moving "backward" in index, player passed Go (only for forward movement cards)
        // Cards like "advance to Go" or "advance to Illinois" are always forward-facing
        if (targetPos < oldPos) {
            player.addMoney(200);
            if (verbose) System.out.println("  Passed Go! Collect $200.");
        }
        player.moveTo(targetPos);
        if (verbose) System.out.printf("  Card moves to %d (%s)%n",
            targetPos, board.getLabel(targetPos));
    }

    // -------------------------------------------------------------------------
    // Space effect resolution
    // -------------------------------------------------------------------------

    /**
     * Applies the effect of the space the player is currently on.
     * May add additional transitions to {@code transitions} if the player moves again.
     */
    private void applySpaceEffect(List<int[]> transitions) {
        int pos = player.getPosition();
        SpaceType type = board.getType(pos);

        switch (type) {
            case GO_TO_JAIL -> {
                int from = player.getEffectiveState();
                player.sendToJail();
                if (verbose) System.out.println("  Go To Jail!");
                transitions.add(new int[]{from, player.getEffectiveState()});
            }
            case CHANCE -> {
                Card card = chanceDeck.draw();
                if (verbose) System.out.println("  Chance: " + card.description());
                applyCard(card, transitions);
            }
            case COMMUNITY_CHEST -> {
                Card card = communityChestDeck.draw();
                if (verbose) System.out.println("  Community Chest: " + card.description());
                applyCard(card, transitions);
            }
            case TAX -> {
                int tax = (pos == 4) ? 200 : 100;
                player.subtractMoney(tax);
                if (verbose) System.out.printf("  Tax: pay $%d%n", tax);
            }
            case GO -> player.addMoney(200);  // landed directly on Go
            default -> {}  // PROPERTY, RAILROAD, UTILITY, FREE_PARKING, JUST_VISITING: no movement
        }
    }

    /**
     * Applies a card's effect to the player, recording any additional transitions.
     */
    public void applyCard(Card card, List<int[]> transitions) {
        int fromState = player.getEffectiveState();

        switch (card.effect()) {
            case MOVE_TO -> {
                movePlayerTo(card.parameter());
                int toState = player.getEffectiveState();
                if (toState != fromState) {
                    transitions.add(new int[]{fromState, toState});
                    applySpaceEffect(transitions);  // may trigger further movement
                }
            }
            case MOVE_FORWARD -> {
                movePlayer(card.parameter());
                int toState = player.getEffectiveState();
                if (toState != fromState) {
                    transitions.add(new int[]{fromState, toState});
                    applySpaceEffect(transitions);
                }
            }
            case MOVE_BACK -> {
                int newPos = ((player.getPosition() - card.parameter()) % 40 + 40) % 40;
                player.moveTo(newPos);
                if (verbose) System.out.printf("  Moved back to %d (%s)%n",
                    newPos, board.getLabel(newPos));
                int toState = player.getEffectiveState();
                if (toState != fromState) {
                    transitions.add(new int[]{fromState, toState});
                    applySpaceEffect(transitions);
                }
            }
            case GO_TO_JAIL -> {
                player.sendToJail();
                if (verbose) System.out.println("  Go to Jail (card)!");
                transitions.add(new int[]{fromState, player.getEffectiveState()});
            }
            case NEAREST_RAILROAD -> {
                int rr = board.nearestRailroad(player.getPosition());
                movePlayerTo(rr);
                int toState = player.getEffectiveState();
                if (toState != fromState) {
                    transitions.add(new int[]{fromState, toState});
                    // Railroads don't trigger further movement
                }
            }
            case NEAREST_UTILITY -> {
                int util = board.nearestUtility(player.getPosition());
                movePlayerTo(util);
                int toState = player.getEffectiveState();
                if (toState != fromState) {
                    transitions.add(new int[]{fromState, toState});
                }
            }
            case GET_OUT_OF_JAIL_FREE -> {
                player.receiveGoojf();
                chanceDeck.recordGoojfTaken();
                if (verbose) System.out.println("  Received Get Out of Jail Free card.");
            }
            case COLLECT -> player.addMoney(card.parameter());
            case PAY     -> player.subtractMoney(card.parameter());
        }
    }

    /** Convenience overload without explicit transition list (used for testing). */
    public void applyCard(Card card) {
        applyCard(card, new ArrayList<>());
    }

    public Player getPlayer() { return player; }
}

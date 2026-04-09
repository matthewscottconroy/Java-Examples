package com.markovmonopoly.monopoly.cards;

import java.util.*;

/**
 * A shuffled deck of Monopoly cards (either Chance or Community Chest).
 *
 * <p>Cards are drawn in order from the shuffled deck. When the deck is exhausted,
 * it is automatically reshuffled. The Get Out of Jail Free card is removed from
 * rotation while a player holds it; it is returned (and the deck reshuffled) when used.
 */
public final class CardDeck {

    private final List<Card> allCards;
    private final List<Card> drawPile;
    private final Random rng;
    private int goojfHeldCount = 0;  // number of Get Out of Jail Free cards held by players

    private CardDeck(List<Card> cards, Random rng) {
        this.allCards = List.copyOf(cards);
        this.drawPile = new ArrayList<>(cards);
        this.rng = rng;
        Collections.shuffle(this.drawPile, rng);
    }

    // -------------------------------------------------------------------------
    // Factory methods
    // -------------------------------------------------------------------------

    /** Creates the standard 16-card Chance deck. */
    public static CardDeck chance(Random rng) {
        List<Card> cards = List.of(
            new Card("Advance to Go (Collect $200)",               CardEffect.MOVE_TO,           0),
            new Card("Advance to Illinois Ave",                    CardEffect.MOVE_TO,           24),
            new Card("Advance to St. Charles Place",               CardEffect.MOVE_TO,           11),
            new Card("Advance to nearest Railroad (pay 2× rent)",  CardEffect.NEAREST_RAILROAD,  0),
            new Card("Advance to nearest Railroad (pay 2× rent)",  CardEffect.NEAREST_RAILROAD,  0),
            new Card("Advance to nearest Utility (pay 10× dice)",  CardEffect.NEAREST_UTILITY,   0),
            new Card("Go back 3 spaces",                           CardEffect.MOVE_BACK,          3),
            new Card("Go to Jail",                                 CardEffect.GO_TO_JAIL,         0),
            new Card("Advance to Boardwalk",                       CardEffect.MOVE_TO,           39),
            new Card("Take a trip to Reading Railroad",            CardEffect.MOVE_TO,            5),
            new Card("Bank pays you dividend of $50",              CardEffect.COLLECT,            50),
            new Card("Get Out of Jail Free",                       CardEffect.GET_OUT_OF_JAIL_FREE, 0),
            new Card("Make general repairs: $25/house, $100/hotel", CardEffect.PAY,               0),
            new Card("Pay poor tax of $15",                        CardEffect.PAY,                15),
            new Card("You have been elected Chairman: pay $50 each", CardEffect.PAY,              0),
            new Card("Your building loan matures: collect $150",   CardEffect.COLLECT,           150)
        );
        return new CardDeck(cards, rng);
    }

    /** Creates the standard 16-card Community Chest deck. */
    public static CardDeck communityChest(Random rng) {
        List<Card> cards = List.of(
            new Card("Advance to Go (Collect $200)",               CardEffect.MOVE_TO,           0),
            new Card("Go to Jail",                                 CardEffect.GO_TO_JAIL,         0),
            new Card("Bank error in your favor: collect $200",     CardEffect.COLLECT,           200),
            new Card("Doctor's fee: pay $50",                      CardEffect.PAY,                50),
            new Card("From sale of stock: collect $50",            CardEffect.COLLECT,            50),
            new Card("Get Out of Jail Free",                       CardEffect.GET_OUT_OF_JAIL_FREE, 0),
            new Card("Holiday Fund matures: collect $100",         CardEffect.COLLECT,           100),
            new Card("Income tax refund: collect $20",             CardEffect.COLLECT,            20),
            new Card("It is your birthday: collect $10 from each", CardEffect.COLLECT,            0),
            new Card("Life insurance matures: collect $100",       CardEffect.COLLECT,           100),
            new Card("Pay hospital fees of $100",                  CardEffect.PAY,               100),
            new Card("Pay school fees of $150",                    CardEffect.PAY,               150),
            new Card("Receive $25 consultancy fee",                CardEffect.COLLECT,            25),
            new Card("Street repairs: $40/house, $115/hotel",      CardEffect.PAY,                0),
            new Card("You have won second prize: collect $10",     CardEffect.COLLECT,            10),
            new Card("You inherit $100",                           CardEffect.COLLECT,           100)
        );
        return new CardDeck(cards, rng);
    }

    // -------------------------------------------------------------------------
    // Drawing
    // -------------------------------------------------------------------------

    /**
     * Draws the next card from the deck. If the deck is empty, it is reshuffled.
     * If a Get Out of Jail Free card is currently held by a player, it is skipped.
     */
    public Card draw() {
        // Skip GOOJF cards that are currently held
        for (int attempts = 0; attempts < drawPile.size() + 1; attempts++) {
            if (drawPile.isEmpty()) reshuffle();
            Card card = drawPile.remove(0);
            if (card.effect() == CardEffect.GET_OUT_OF_JAIL_FREE && goojfHeldCount > 0) {
                // This card is held; put it at the bottom and try next
                drawPile.add(card);
                continue;
            }
            return card;
        }
        // Fallback: return a no-op collect card (shouldn't happen in normal play)
        return new Card("No effect", CardEffect.COLLECT, 0);
    }

    /** Called when a player receives a Get Out of Jail Free card. */
    public void recordGoojfTaken() { goojfHeldCount++; }

    /** Called when a player uses (returns) their Get Out of Jail Free card. */
    public void returnGoojf() {
        if (goojfHeldCount > 0) goojfHeldCount--;
    }

    /** Returns the number of cards remaining in the draw pile. */
    public int remaining() { return drawPile.size(); }

    private void reshuffle() {
        drawPile.clear();
        drawPile.addAll(allCards);
        Collections.shuffle(drawPile, rng);
    }

    /**
     * Returns all cards in this deck (not the current draw order, but the full set).
     * Useful for computing theoretical Markov chain transition probabilities.
     */
    public List<Card> getAllCards() { return allCards; }

    /**
     * Returns the probability of drawing each card from this deck, assuming
     * a uniform shuffle (each card equally likely). Used for the theoretical chain.
     */
    public double cardProbability() { return 1.0 / allCards.size(); }
}

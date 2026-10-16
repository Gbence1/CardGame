import java.util.concurrent.LinkedBlockingQueue;

public class CardDeck {
    // A LinkedBlockingQueue automatically handles locks
    private final LinkedBlockingQueue<Card> cards;
    private final int deckNum;

    public CardDeck(int deckNum) {
        cards = new LinkedBlockingQueue<>();
        this.deckNum = deckNum;
    }

    public int getDeckNum() {
        return deckNum;
    }

    public Card draw() {
        // Using poll instead of take because take could end up in a deadlock
        // where it would never check the flag as it perpetually waits.
        return cards.poll();
    }

    public String getContentsString() {
        StringBuilder out = new StringBuilder();
        for (Card c : cards) {
            out.append(" ").append(c.getValue());
        }
        return out.toString().trim();
    }

    public void insertAtBottom(Card card) {
        // Realistically no issue should ever happen because cards is unbounded so it's never full.
        if (!cards.offer(card)) {
            throw new IllegalStateException("Deck " + deckNum + " rejected a card");
        }
    }
}

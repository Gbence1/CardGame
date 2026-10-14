import java.util.concurrent.LinkedBlockingQueue;

public class CardDeck {
    private final LinkedBlockingQueue<Card> cards;

    public CardDeck(Integer deckNum) {
        cards = new LinkedBlockingQueue<>();
    }

    public Card draw() throws InterruptedException {
        return cards.take();
    }

    public void discard(Card card) throws InterruptedException {
        cards.put(card);
    }
}

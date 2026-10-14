import java.util.concurrent.LinkedBlockingQueue;

public class Deck {
    private final LinkedBlockingQueue<Card> cards;

    public Deck() {
        cards = new LinkedBlockingQueue<>();
    }

    public Card draw() throws InterruptedException {
        return cards.take();
    }

    public void discard(Card card) throws InterruptedException {
        cards.put(card);
    }
}

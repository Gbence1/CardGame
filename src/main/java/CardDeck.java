import java.util.concurrent.LinkedBlockingQueue;

public class CardDeck {
    private final LinkedBlockingQueue<Card> cards;
    private final Integer deckNum;

    public CardDeck(Integer deckNum) {
        cards = new LinkedBlockingQueue<>();
        this.deckNum = deckNum;
    }

    public Integer getDeckNum() {
        return deckNum;
    }

    public Card draw() throws InterruptedException {
        return cards.take();
    }

    public String getContentsString() {
        String out = "";
        for (Card c : cards) {
        out = out + " " + c.getValue();
    }
    return out.trim();
    }

    public void insertAtBottom(Card card) throws InterruptedException {
        cards.put(card);
    }
}

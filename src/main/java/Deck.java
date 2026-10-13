import java.util.ArrayList;

public class Deck {
    private ArrayList<Card> cardsQueue;
    public Deck() {
        cardsQueue = new ArrayList<>();
    }
    public void drawCard(Card card) {
        cardsQueue.add(card);
    }
    public Card getCard() {
        return cardsQueue.get(0);
    }
}

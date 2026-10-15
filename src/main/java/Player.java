import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class Player {
    //private Card[] hand =  new Card[4];
    private List<Card> hand = new ArrayList<>();
    private final int preference;
    private final CardDeck drawDeck;
    private final CardDeck discardDeck;
    private AtomicInteger winner;
    private PrintWriter output;

    public Player(int preference, CardDeck drawDeck, CardDeck discardDeck, AtomicInteger winner, PrintWriter output) {
        if (preference < 1) {
            throw new IllegalArgumentException("Player number must be greater than 0");
        }
        this.preference = preference;
        this.drawDeck = drawDeck;
        this.discardDeck = discardDeck;
        this.winner = winner;
        this.output = output;
    }

    public List<Card> getHand() {
        return new ArrayList<>(hand);
    }

    public String getHandString() {
        String handString = "";
        for (Card c : hand) {
            handString = handString + ' ' + c.getValue();
        }
        return handString.trim();
    }
//    public void setHand(Card[] hand) {
//        this.hand = hand;
//    }

    public void takeTurn() {
        // TODO: lock both decks, draw, pick discard, discard, write current hand
        // DONE?
        CardDeck first;
        CardDeck second;

        if (drawDeck.getDeckNum() < discardDeck.getDeckNum()) {
            // All Lock the smaller ID deck first, to avoid a deadlock
            first = drawDeck;
            second = discardDeck;
        } else {
            first = discardDeck;
            second = drawDeck;
        }
        synchronized(first) {
            synchronized(second) { //Draw + Discard is an atomic action, so both decks need to be locked the whole time
                Card drawn = drawDeck.draw();
                draw(drawn);

                Card toDiscard = chooseDiscard();
                discard(toDiscard);
                discardDeck.discard(toDiscard);

                writeHand();
            }
        }
        
    }

    public void draw(Card card) {
        hand.add(card);
        String message = "player " + preference + " draws a " + card.getValue() + " from deck " + drawDeck.getDeckNum();
        output.println(message);

    }

    public void discard(Card card) {
        // TODO ? is this method supposed to affect the discard deck or does that happen in the takeTurn method
        String message = "player " + preference + " discards a " + card.getValue() + " to deck " + discardDeck.getDeckNum();
        output.println(message);
        hand.remove(card);
    }


    public void writeHand() {
        String message;
        message = "player " + preference + " current hand is " + getHandString();
        output.println(message);
    }


    private Card chooseDiscard() {
        List<Card> discardables = new ArrayList<>()
        for (Card c : hand) {
            if (c.getValue() != preference) {
                discardables.add(c);
            }
        }
        return discardables.get(ThreadLocalRandom.current().nextInt(discardables.size()));
    }

    private int getRandInt(int min, int max) {
        Random rand = new Random();
        return rand.nextInt(max - min + 1) + min;
    }

}

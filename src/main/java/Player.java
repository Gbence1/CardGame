import java.io.PrintWriter;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Player {
    private Card[] hand =  new Card[4];
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

    public Card[] getHand() {
        return hand;
    }

    public String getHandString() {
        String handString = "";
        for (Card c : hand) {
            handString = handString + ' ' + c.getValue();
        }
        return handString;
    }
//    public void setHand(Card[] hand) {
//        this.hand = hand;
//    }

    public void takeTurn() {
        // TODO
    }

    public void draw(Card card) {
        // TODO
    }


    public void drawDiscardWrite(Integer action, Integer cardVal) {
        // action: 1 = draw, 2 = discard
        String message;
        if (action == 1) {
            message = "player " + preference + " draws a " + cardVal + " from deck " + drawDeck;
        } else if (action == 2) {
            message = "player " + preference + " discards a " + cardVal + " to deck " + discardDeck;
        } else {
            System.out.println("Invalid action");
            return;
        }
        output.println(message);
    }

    public void checkHandWrite(Boolean initial) {
        String message;
        if (initial) {
            message = "player " + preference + " initial hand is " + getHandString();
        } else {
        message = "player " + preference + " current hand is " + getHandString();
        }
        output.println(message);
    }

    private void discard() {
        // TODO
    }



    private int getRandInt(int min, int max) {
        Random rand = new Random();
        return rand.nextInt(max - min + 1) + min;
    }

}

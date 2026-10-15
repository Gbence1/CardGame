import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class Player implements Runnable {
    private final ArrayList<Card> hand = new ArrayList<>();
    private final int playerNumber;
    private final CardDeck drawDeck;
    private final CardDeck discardDeck;
    private final AtomicInteger winnerFlag;
    private final PrintWriter output;

    public Player(int playerNumber, CardDeck drawDeck, CardDeck discardDeck, AtomicInteger winnerFlag, PrintWriter output) {
        this.playerNumber = playerNumber;
        this.drawDeck = drawDeck;
        this.discardDeck = discardDeck;
        this.winnerFlag = winnerFlag;
        this.output = output;
    }

    @Override
    public void run() {
        output.println("player " + playerNumber + " initial hand " + getHandString());

        if (hasWinningHand()) {
            declareWin();
        }

        while (winnerFlag.get() == 0) {
            takeTurn();
            if (hasWinningHand()) {
                declareWin();
            }
        }  
        finish();
    }

    private void finish() {
        int winner = winnerFlag.get();
        if (winner != playerNumber) {
            output.println("player " + winner + " has informed player " + playerNumber
                    + " that player " + winner + " has won");
        }
        output.println("player " + playerNumber + " exits");
        output.println("player " + playerNumber + " hand: " + getHandString());
    output.flush();
    }

    public String getHandString() {
        StringBuilder handString = new StringBuilder();
        for (Card c : hand) {
            handString.append(' ' + c.getValue());
        }
        return handString.toString().trim();
    }

    public void takeTurn() {
        // Deck numbers must be unique and immutable.
        // Lock the smaller ID deck first, to avoid a deadlock
        CardDeck first = drawDeck.getDeckNum() < discardDeck.getDeckNum() ? drawDeck : discardDeck;

        CardDeck second = first == drawDeck ? discardDeck : drawDeck;

        //Draw + Discard is an atomic action, so both decks need to be locked the whole time
        synchronized (first) {
            synchronized (second) {
                draw();
                discard(chooseDiscard());
                writeHand();
            }
        }
    }

    public void addToHand(Card card) {
        hand.add(card);
    }

    public void close() {
        output.close();
    }

    public void draw() {
        try {
            Card drawnCard = drawDeck.draw();
            hand.add(drawnCard);
            String message = "player " + playerNumber + " draws a " + drawnCard.getValue() + " from deck " + drawDeck.getDeckNum();
            output.println(message);
        } catch (InterruptedException e) {
            // TODO
        }
    }

    public void discard(Card card) {
        try {
            discardDeck.insertAtBottom(card);
            hand.remove(card);
            String message = "player " + playerNumber + " discards a " + card.getValue() + " to deck " + discardDeck.getDeckNum();
            output.println(message);
        } catch (InterruptedException e) {
            // TODO
        }
    }

    public void writeHand() {
        String message = "player " + playerNumber + " current hand is " + getHandString();
        output.println(message);
    }

    private Card chooseDiscard() {
        List<Card> discardables = new ArrayList<>();
        for (Card c : hand) {
            if (c.getValue() != playerNumber) {
                discardables.add(c);
            }
        }
        return discardables.get(ThreadLocalRandom.current().nextInt(discardables.size()));
    }

    private boolean hasWinningHand() {
        if (hand.size() != 4) {
            return false;
        }
        int necessary = hand.get(0).getValue();
        for (Card c : hand) {
            if (c.getValue() != necessary) {
                return false;
            }
        }
        return true;
    }

    private void declareWin() {
        if (winnerFlag.compareAndSet(0, playerNumber)) {
            System.out.println("player " + playerNumber + "wins");
            output.println("player " + playerNumber + "wins");
        }
    }
}
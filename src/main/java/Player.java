import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class Player implements Runnable {
    private final ArrayList<Card> hand = new ArrayList<>(); // easier to handle removing a specific card than in an array
    private final int playerNumber;
    private final CardDeck drawDeck; // reference to the draw deck to the left of the player
    private final CardDeck discardDeck; // reference to the discard deck to the right of the player
    private final AtomicInteger winnerFlag; // atomic ensures that no race conditions occur
    private final PrintWriter fileOutput;

    public Player(int playerNumber, CardDeck drawDeck, CardDeck discardDeck, AtomicInteger winnerFlag, PrintWriter fileOutput) {
        this.playerNumber = playerNumber;
        this.drawDeck = drawDeck;
        this.discardDeck = discardDeck;
        this.winnerFlag = winnerFlag;
        this.fileOutput = fileOutput;
    }

    @Override
    public void run() {
        // Printing initial hand to file
        fileOutput.println("player " + playerNumber + " initial hand " + getHandString());

        // Checking initial win
        if (hasWinningHand()) {
            declareWin();
        }

        // Player takes turns until someone sets the atomic flag
        // Then, if this instance won, declares, then finishes
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
            fileOutput.println("player " + winner + " has informed player " + playerNumber + " that player " + winner + " has won");
        }
        fileOutput.println("player " + playerNumber + " exits");
        fileOutput.println("player " + playerNumber + (winner == playerNumber ? " final hand: " : " hand: ") + getHandString());
        fileOutput.flush();
        fileOutput.close();
    }

    private String getHandString() {
        StringBuilder handString = new StringBuilder();
        for (Card c : hand) {
            handString.append(" ").append(c.getValue());
        }
        return handString.toString().trim();
    }

    private void takeTurn() {
        // Deck numbers must be unique and final
        // Lock the smaller ID deck first to avoid a deadlock
        CardDeck first = drawDeck.getDeckNum() < discardDeck.getDeckNum() ? drawDeck : discardDeck;
        CardDeck second = first == drawDeck ? discardDeck : drawDeck; // Pick the opposite of first

        //Draw + Discard is an atomic action, so both decks need to be locked the whole time
        boolean drawSuccess = true;
        synchronized (first) {
            synchronized (second) {
                // If for some reason couldn't draw, skip discarding to maintain atomicity
                if (draw()) {
                    discard(chooseDiscard());
                    writeHand();
                } else {
                    drawSuccess = false;
                }
            }
        }
        // Allow other threads to take precedence if we encounter a failed draw
        // so rechecks don't use too much resources
        if (!drawSuccess) {
            Thread.yield();
        }
    }

    public void addToHand(Card card) {
        // A convenience method for setting up the game
        hand.add(card);
    }

    private boolean draw() {
        // Returns true on successful draw
        Card drawnCard = drawDeck.draw();
        if (drawnCard != null) {
            hand.add(drawnCard);
            String message = "player " + playerNumber + " draws a " + drawnCard.getValue() + " from deck " + drawDeck.getDeckNum();
            fileOutput.println(message);
            return true;
        }
        return false;
    }

    private void discard(Card card) {
        discardDeck.insertAtBottom(card); // this should never fail because decks are unbounded (there's always room)
        hand.remove(card);
        String message = "player " + playerNumber + " discards a " + card.getValue() + " to deck " + discardDeck.getDeckNum();
        fileOutput.println(message);
    }

    private void writeHand() {
        String message = "player " + playerNumber + " current hand is " + getHandString();
        fileOutput.println(message);
    }

    private Card chooseDiscard() {
        // Generates a list of non-preferred cards and randomly chooses one
        List<Card> discardables = new ArrayList<>();
        for (Card c : hand) {
            if (c.getValue() != playerNumber) {
                discardables.add(c);
            }
        }
        // Uses thread-isolated random
        return discardables.get(ThreadLocalRandom.current().nextInt(discardables.size()));
    }

    private boolean hasWinningHand() {
        if (hand.size() != 4) {
            throw new IllegalStateException("Whoops, player " + playerNumber + " doesn't have exactly 4 cards! Atomicity requirement failed.");
        }
        // Checks if all 4 cards are the same
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
            System.out.println("Player " + playerNumber + " wins");
            fileOutput.println("player " + playerNumber + " wins");
        }
    }
}
import java.util.Random;

public class Player {
    private Card[] hand =  new Card[4];
    private final int preference;

    public Player(int preference) {
        if (preference < 1) {
            throw new IllegalArgumentException("Player number must be greater than 0");
        }
        this.preference = preference;
    }

    public  Card[] getHand() {
        return hand;
    }
    public void setHand(Card[] hand) {
        this.hand = hand;
    }

    public void drawDiscard() {
        // TODO
    }


    private int getRandInt(int min, int max) {
        Random rand = new Random();
        return rand.nextInt(max - min + 1) + min;
    }

}

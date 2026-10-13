public class Card {
    private final int value;

    public Card(int value) {
        if (value < 1) {
            throw new IllegalArgumentException("Value must be non-negative");
        }
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

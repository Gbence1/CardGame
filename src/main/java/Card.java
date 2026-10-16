public class Card {
    // final + no setter = thread-safe
    private final int value;

    public Card(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

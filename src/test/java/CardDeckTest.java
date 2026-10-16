import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CardDeckTest {

    @Test
    void drawIsFifo() {
        // Insert at bottom, draw from front
        CardDeck deck = new CardDeck(1);
        deck.insertAtBottom(new Card(10));
        deck.insertAtBottom(new Card(20));
        deck.insertAtBottom(new Card(30));

        assertEquals(10, deck.draw().getValue());
        assertEquals(20, deck.draw().getValue());
        assertEquals(30, deck.draw().getValue());
    }

    @Test
    void emptyDeckReturnsNull() {
        // poll() on an empty queue should not block
        CardDeck deck = new CardDeck(2);
        assertNull(deck.draw());
    }

    @Test
    void contentsString() {
        CardDeck deck = new CardDeck(3);
        deck.insertAtBottom(new Card(1));
        deck.insertAtBottom(new Card(3));
        deck.insertAtBottom(new Card(7));

        assertEquals("1 3 7", deck.getContentsString());
        deck.draw();
        assertEquals("3 7", deck.getContentsString());
    }
}

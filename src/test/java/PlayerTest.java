import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class PlayerTest {

    @Test
    void immediateWin() {
        // Four matching cards at the start should win without taking turns
        StringWriter out = new StringWriter();
        AtomicInteger winnerFlag = new AtomicInteger(0);
        CardDeck drawDeck = new CardDeck(1);
        CardDeck discardDeck = new CardDeck(2);
        drawDeck.insertAtBottom(new Card(9)); // shouldn't be used

        Player player = new Player(1, drawDeck, discardDeck, winnerFlag, new PrintWriter(out, true));
        for (int i = 0; i < 4; i++) {
            player.addToHand(new Card(7));
        }

        player.run();

        String log = out.toString();
        assertEquals(1, winnerFlag.get());
        assertTrue(log.contains("player 1 wins"));
        assertTrue(log.contains("player 1 exits"));
        assertTrue(log.contains("final hand: 7 7 7 7"));
        assertFalse(log.contains("draws a"));
        assertFalse(log.contains("discards a"));
    }

    @Test
    void loserGetsInformed() {
        // Flag already set means this player just writes the ending lines and leaves
        StringWriter out = new StringWriter();
        AtomicInteger winnerFlag = new AtomicInteger(2);
        Player player = new Player(1, new CardDeck(1), new CardDeck(2), winnerFlag, new PrintWriter(out, true));
        player.addToHand(new Card(1));
        player.addToHand(new Card(2));
        player.addToHand(new Card(3));
        player.addToHand(new Card(4));

        player.run();

        String log = out.toString();
        assertTrue(log.contains("player 2 has informed player 1 that player 2 has won"));
        assertTrue(log.contains("player 1 exits"));
        assertTrue(log.contains("player 1 hand: 1 2 3 4"));
        assertFalse(log.contains("final hand"));
    }

    @Test
    @Timeout(5)
    void discardSkipsPreferred() throws InterruptedException {
        // Player 1 prefers 1s, so those should not show up in discard lines
        StringWriter out = new StringWriter();
        AtomicInteger winnerFlag = new AtomicInteger(0);
        CardDeck drawDeck = new CardDeck(1);
        CardDeck discardDeck = new CardDeck(2);
        for (int i = 0; i < 20; i++) {
            drawDeck.insertAtBottom(new Card(5));
        }

        Player player = new Player(1, drawDeck, discardDeck, winnerFlag, new PrintWriter(out, true));
        player.addToHand(new Card(1));
        player.addToHand(new Card(2));
        player.addToHand(new Card(3));
        player.addToHand(new Card(4));

        Thread thread = new Thread(player);
        thread.start();
        // Let threads take a few turns, then stop using the flag
        Thread.sleep(100);
        winnerFlag.compareAndSet(0, 99);
        thread.join(3000);
        if (thread.isAlive()) {
            fail("player thread did not stop after the winner flag was set");
        }

        boolean sawDiscard = false;
        for (String line : out.toString().split("\n")) {
            if (line.contains("discards a ")) {
                sawDiscard = true;
                // value is between "discards a " and " to deck"
                int start = line.indexOf("discards a ") + "discards a ".length();
                int end = line.indexOf(" to deck");
                String value = line.substring(start, end).trim();
                assertFalse(value.equals("1"), "discarded a preferred card: " + line);
            }
        }
        assertTrue(sawDiscard, "expected at least one discard before we stopped the player");
    }

    @Test
    @Timeout(5)
    void handStaysSizeFour() throws InterruptedException {
        // After each finished draw+discard, the logged hand should still be 4 cards
        StringWriter out = new StringWriter();
        AtomicInteger winnerFlag = new AtomicInteger(0);
        CardDeck drawDeck = new CardDeck(1);
        CardDeck discardDeck = new CardDeck(2);
        for (int i = 0; i < 30; i++) {
            drawDeck.insertAtBottom(new Card(8));
        }

        Player player = new Player(1, drawDeck, discardDeck, winnerFlag, new PrintWriter(out, true));
        player.addToHand(new Card(1));
        player.addToHand(new Card(2));
        player.addToHand(new Card(3));
        player.addToHand(new Card(4));

        Thread thread = new Thread(player);
        thread.start();
        Thread.sleep(100);
        winnerFlag.compareAndSet(0, 99);
        thread.join(3000);
        if (thread.isAlive()) {
            fail("player thread did not stop after the winner flag was set");
        }

        String log = out.toString();
        boolean sawHand = false;
        for (String line : log.split("\n")) {
            if (line.contains("current hand is ")) {
                sawHand = true;
                String hand = line.substring(line.indexOf("current hand is ") + "current hand is ".length()).trim();
                assertEquals(4, countCards(hand));
            }
        }
        assertTrue(sawHand, "no current-hand lines were logged");
        assertEquals(4, countCards(lastHandLine(log)));
    }

    @Test
    @Timeout(5)
    void emptyDeckDoesNotDiscard() throws InterruptedException {
        // poll() returns null on an empty deck; shouldn't discard in that case
        StringWriter out = new StringWriter();
        AtomicInteger winnerFlag = new AtomicInteger(0);
        CardDeck drawDeck = new CardDeck(1);
        CardDeck discardDeck = new CardDeck(2);

        Player player = new Player(1, drawDeck, discardDeck, winnerFlag, new PrintWriter(out, true));
        player.addToHand(new Card(1));
        player.addToHand(new Card(2));
        player.addToHand(new Card(3));
        player.addToHand(new Card(4));

        Thread thread = new Thread(player);
        thread.start();
        Thread.sleep(50);
        winnerFlag.compareAndSet(0, 2);
        thread.join(3000);
        if (thread.isAlive()) {
            fail("player got stuck on an empty draw deck");
        }

        String log = out.toString();
        assertFalse(log.contains("discards a"));
        assertTrue(log.contains("player 1 hand: 1 2 3 4"));
    }

    @Test
    @Timeout(5)
    void immediateWinTwoPlayers() throws Exception {
        // Player 1 starts with four 7s in the sample pack; both threads should finish,
        // and we should still have 16 cards across hands + decks
        Card[] pack = CardGame.loadPack(packPath("packs/immediate_win_n2.txt"), 2);
        AtomicInteger winnerFlag = new AtomicInteger(0);

        CardDeck deck1 = new CardDeck(1);
        CardDeck deck2 = new CardDeck(2);
        StringWriter out1 = new StringWriter();
        StringWriter out2 = new StringWriter();
        Player player1 = new Player(1, deck1, deck2, winnerFlag, new PrintWriter(out1, true));
        Player player2 = new Player(2, deck2, deck1, winnerFlag, new PrintWriter(out2, true));

        // Same round-robin deal and fill like in CardGame.setUpGame
        int next = 0;
        for (int round = 0; round < 4; round++) {
            player1.addToHand(pack[next++]);
            player2.addToHand(pack[next++]);
        }
        while (next < pack.length) {
            deck1.insertAtBottom(pack[next++]);
            if (next < pack.length) {
                deck2.insertAtBottom(pack[next++]);
            }
        }

        Thread t1 = new Thread(player1);
        Thread t2 = new Thread(player2);
        t1.start();
        t2.start();
        // Wait for both threads to die
        t1.join(3000);
        t2.join(3000);
        if (t1.isAlive() || t2.isAlive()) {
            fail("a player thread was still running after the immediate win");
        }

        assertEquals(1, winnerFlag.get());
        assertTrue(out1.toString().contains("player 1 wins"));
        assertTrue(out2.toString().contains("player 1 has informed player 2"));

        int handCards = countCards(lastHandLine(out1.toString())) + countCards(lastHandLine(out2.toString()));
        int deckCards = countCards(deck1.getContentsString()) + countCards(deck2.getContentsString());
        assertEquals(16, handCards + deckCards);
    }

    private static int countCards(String values) {
        String trimmed = values.trim();
        if (trimmed.isEmpty()) {
            return 0;
        }
        return trimmed.split("\\s+").length;
    }

    // Finds the last "hand:" / "final hand:" line and returns just the card values
    private static String lastHandLine(String log) {
        String found = null;
        for (String line : log.split("\n")) {
            if (line.contains("final hand:")) {
                found = line.substring(line.indexOf("final hand:") + "final hand:".length());
            } else if (line.contains(" hand:")) {
                found = line.substring(line.indexOf(" hand:") + " hand:".length());
            }
        }
        if (found == null) {
            fail("could not find an ending hand line in:\n" + log);
        }
        return found;
    }

    private static String packPath(String name) throws Exception {
        return java.nio.file.Paths.get(
                PlayerTest.class.getClassLoader().getResource(name).toURI()
        ).toString();
    }
}

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class CardGame {
    public static void main(String[] args) {
        AtomicInteger winnerFlag = new AtomicInteger(0); // atomic ensures that no race conditions occur

        Scanner scanner = new Scanner(System.in); // for parsing user input

        int playerCount = getPlayerCount(scanner);

        Card[] pack = getPack(scanner, playerCount);
        CardDeck[] decks = new CardDeck[playerCount];
        Player[] players = new Player[playerCount];

        try {
            setUpGame(playerCount, pack, decks, players, winnerFlag);
        } catch (FileNotFoundException e) {
            // This is thrown if the PrintWriter can't create the player file
            // It shouldn't normally happen so it's just thrown again as a runtime
            throw new RuntimeException(e);
        }

        try {
            // Start up a thread for each player
            Thread[] threads = new Thread[playerCount];
            for (int i = 0; i < playerCount; i++) {
                threads[i] = new Thread(players[i]);
                threads[i].start();
            }

            // Make main wait for all threads to die
            for (Thread t : threads) {
                t.join(); // may throw Interrupted
            }
        } catch (InterruptedException e) {
            // Should never happen for us so just thrown again as a runtime
            throw new RuntimeException(e);
        }

        for (CardDeck d : decks) {
            try (PrintWriter w = new PrintWriter("deck" + d.getDeckNum() + "_output.txt")) {
                w.println("deck" + d.getDeckNum() + " contents: " + d.getContentsString());
                w.flush();
            } catch (FileNotFoundException e) {
                // This is thrown if the PrintWriter can't create the deck file
                // It shouldn't normally happen so it's just thrown again as a runtime
                throw new RuntimeException(e);
            }
        }
    }

    private static void setUpGame(int playerCount, Card[] pack, CardDeck[] decks, Player[] players, AtomicInteger winnerFlag) throws FileNotFoundException {
        // Creates playerCount number of decks
        for (int i = 0; i < playerCount; i++) {
            decks[i] = new CardDeck(i + 1);
        }

        // Sets the properties for each player
        // Assigns deck i to player i (displayed: i+1) for drawing, and deck i+1 to player i (displayed: i+1) for discarding
        for (int i = 0; i < playerCount; i++) {
            CardDeck drawDeck = decks[i];
            CardDeck discardDeck = decks[(i + 1) % playerCount]; // last player discards to first deck
            PrintWriter fileOutput = new PrintWriter("player" + (i + 1) + "_output.txt"); // this can throw Exception
            players[i] = new Player(i + 1, drawDeck, discardDeck, winnerFlag, fileOutput);
        }

        // Deals 4 cards to each player, one card per player at a time
        int next = 0;
        for (int round = 0; round < 4; round++) {
            for (Player p : players) {
                p.addToHand(pack[next++]);
            }
        }

        // Fill decks from remaining cards in pack, one card per deck at a time
        while (next < pack.length) {
            for (CardDeck d : decks) {
                d.insertAtBottom(pack[next++]);
            }
        }
    }

    private static int getPlayerCount(Scanner scanner) {
        // Gets and validates user inputted number of players
        while (true) {
            System.out.println("Please enter the number of players:");
            String line = scanner.nextLine().trim();
            try {
                int n = Integer.parseInt(line);
                if (n >= 1) {
                    return n;
                }
                System.out.println("Number of players must be a positive integer.");
            } catch (NumberFormatException e) {
                System.out.println("Number of players must be an integer.");
            }
        }
    }

    private static Card[] getPack(Scanner scanner, int playerCount) {
        // Gets pack path, validates it, and reads the pack file
        while (true) {
            System.out.println("Please enter full / relative path to pack to load:");
            String path = scanner.nextLine().trim();
            try {
                return readPackFile(path, playerCount);
            } catch (IOException e) {
                System.out.println("Could not read file: " + path);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid pack: " + e.getMessage());
            }
        }
    }

    private static Card[] readPackFile(String path, int playerCount) throws IOException {
        int expectedSize = 8 * playerCount;
        List<String> lines = Files.readAllLines(Path.of(path));

        if (lines.size() != expectedSize) {
            throw new IllegalArgumentException("Expected " + (expectedSize) + " rows but found " + lines.size());
        }
        Card[] pack = new Card[expectedSize];
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            try {
                int value = Integer.parseInt(line);
                if (value < 0) {
                    throw new IllegalArgumentException("Row " + (i + 1) + " is negative. Only non-negative integers are allowed.");
                }
                pack[i] = new Card(value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Row " + (i + 1) + " is not a single non-negative integer.");
            }
        }
        return pack;
    }
}
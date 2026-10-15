import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class CardGame {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        int playerCount = getPlayerCount(scanner);

        Card[] pack = getPack(scanner, playerCount);
        CardDeck[] decks = new CardDeck[playerCount];
        Player[] players = new Player[playerCount];

        setUpGame(playerCount, pack, decks, players);
    }

    private static void setUpGame(int playerCount, Card[] pack, CardDeck[] decks, Player[] players) {
        AtomicInteger winnerFlag = new AtomicInteger(0);

        // Creates playerCount decks
        for (int i = 0; i < playerCount; i++) {
            decks[i] = new CardDeck(i);
        }

        // Sets the properties for each player
        // Assigns deck i to player i for drawing, and deck i+1 to player i for discarding
        for (int i = 0; i < playerCount; i++) {
            CardDeck drawDeck = decks[i];
            CardDeck discardDeck = decks[(i+1) % playerCount];
            PrintWriter output = new PrintWriter(System.out);
            players[i] = new Player(i+1, drawDeck, discardDeck, winnerFlag, output);
        }

        // Deals 4 cards to each player, one card per player at a time
        int next = 0;
        for (int round = 0; round < 4; round++) {
            for (Player p : players) {
                p.addToHand(pack[next++]);
            }
        }

        // Fill decks from remaining cards in pack
        while (next < pack.length) {
            try {
                for (CardDeck d : decks) {
                    d.insertAtBottom(pack[next++]);
                }
            }
            catch (InterruptedException e) {
                // This shouldn't ever happen because we're not doing concurrency yet at this point
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
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
        // Receives pack path location, validates it, and reads the pack file at the path
        while (true) {
            System.out.println("Please enter location of pack to load: ");
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
        int expectedSize = 8*playerCount;
        List<String> lines = Files.readAllLines(Path.of(path));

        if (lines.size() != expectedSize) {
            throw new IllegalArgumentException("Expected " + (expectedSize) + " rows but found " + lines.size());
        }
        Card[] pack = new Card[expectedSize];
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            try {
                int value = Integer.parseInt(line);
                if (value < 1) {
                    throw new IllegalArgumentException("Row " + (i+1) + " is not positive. Only positive integers are allowed.");
                }
                pack[i] = new Card(value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Row " + (i+1) + " is not a single positive integer.");
            }
        }
        return pack;
    }
}
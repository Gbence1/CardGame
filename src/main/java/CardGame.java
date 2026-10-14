import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Collections;


public class CardGame {
    public static void main(String[] args) {
        // TODO
        Scanner scanner = new Scanner(System.in);

        int playerCount = getPlayerCount(scanner);
        List<Integer> pack = getPack(scanner, playerCount);
        List<List<Card>> hands = Collections.emptyList();
        setUpGame(playerCount, pack);
    }

    static void setUpGame(int n, List<Integer> pack) throws IOException {
        List<CardDeck> decks = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            decks.add(new CardDeck(i));
        } // Creates n decks

        AtomicInteger winner = new AtomicInteger(0); 
        List<Player> players = new ArrayList<>();

        for (int i = 1; i <= n; i++) {
            CardDeck drawDeck = decks.get(i - 1);
            CardDeck discardDeck = decks.get(i % n);
            players.add(new Player(i, drawDeck, discardDeck, winner));
        } 
        // Creates n players
        // Assigns deck i to player i for drawing, and deck i+1 to player i for discarding

        int next = 0;
        // deals 4 cards to each player, one card per player at a time
        for (int round = 0; round < 4; round++) {
            for (Player p : players) {
                p.draw(new Card(pack.get(next++)));
            }
        }

        // Fill decks from remaining cards in pack
        while (next < pack.size()) {
            for (CardDeck d : decks) {
                d.discard(new Card(pack.get(next++)));
        }
    }

    

    }

    private static int getPlayerCount(Scanner scanner){
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
            } catch (NumberFormatException e);
                System.out.println("Number of players must be a whole number/integer");
    }

    private static List<Integer> getPack(Scanner scanner, int n) {
        // Recieves pack path location, validates it, and reads the pack file at the path
            while (true) {
                System.out.println("Please enter location of pack to load");
                String path = scanner.nextLine().trim();
                try {
                    return readPack(path, n)
                } catch (IOException e) {
                    System.out.println("Could not read file: " + path);
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid pack: " + e.getMessage());
                }
            }
    }

    private static List<Integer> readPack(String path, int n) throws IOException {
        List<String> lines = Files.readAllLines(Path.of(path));

        if (lines.size() != 8 * n) {
            throw new IllegalArgumentException(
                    "expected " + (8 * n) + " rows but found " + lines.size());
        }

        List<Integer> pack = new ArrayList<>(lines.size());
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            try {
                int value = Integer.parseInt(line);
                if (value < 0) {
                    throw new IllegalArgumentException(
                            "row " + (i + 1) + " is negative");
                }
                pack.add(value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "row " + (i + 1) + " is not a single non-negative integer");
            }
        }
        return pack;
    }

}
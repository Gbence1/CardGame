import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CardGame {
    public static void main(String[] args) {
        // TODO
        Scanner scanner = new Scanner(System.in);

        int playerCount = getPlayerCount(scanner);
        List<Integer> pack = getPack(scanner, playerCount)

    }

    private static int getPlayerCount(Scanner scanner)
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
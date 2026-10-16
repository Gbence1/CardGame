# CardGame
ECM2414_SD coursework

## Requirements

- JDK 17
- Maven (test compilation and run)
- JUnit 5.10.2 (unit tests)

## Compiling and running

From the folder containing the pom.xml file:

```bash
mvn compile
java -cp target/classes CardGame
```

### To run the jar file:
```bash
java -jar cards.jar
```

## Playing the game

1. Enter the number of players
2. Enter the path to a pack file (full or relative path).

Once the game finishes player output files (`player1_output.txt`, etc.) and deck output files (`deck1_output.txt`, etc.) are generated in the current directory. The winner also gets printed to the terminal, e.g. `Player 4 wins`.

## Pack files

A pack is a text file with exactly 8n lines for n players. Each line is one non-negative integer (0 is allowed).

Example packs can be found under `src/test/resources/packs/` that the tests use.
These can be used for the game as well (e.g. `valid_n4.txt` with n = 4).

## Tests

JUnit 5: to run all tests:

```bash
mvn test
```

Builds the main code, the tests, then runs them. Test classes are in `src/test/java/`, and the sample / invalid packs they load are in `src/test/resources/packs/`.

## Main classes

- `CardGame` — entry point, reads input, sets up players/decks, starts threads, writes deck files at the end
- `Player` — each player is a thread: draw, discard, check for win, writes own output file
- `CardDeck` — FIFO deck (LinkedBlockingQueue)
- `Card` — immutable card value (basically a record)

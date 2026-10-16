import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PackLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    void validPack() throws Exception {
        Card[] pack = CardGame.loadPack(packPath("packs/valid_n2.txt"), 2);
        assertEquals(16, pack.length);
        assertEquals(1, pack[0].getValue());
        assertEquals(8, pack[15].getValue());
    }

    @Test
    void validPackOnePlayer() throws Exception {
        // 8n rows when n = 1
        Card[] pack = CardGame.loadPack(packPath("packs/valid_n1.txt"), 1);
        assertEquals(8, pack.length);
    }

    @Test
    void validPackFourPlayers() throws Exception {
        // 8n rows when n = 4
        Card[] pack = CardGame.loadPack(packPath("packs/valid_n4.txt"), 4);
        assertEquals(32, pack.length);
    }

    @Test
    void wrongLength() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> CardGame.loadPack(packPath("packs/wrong_length_n2.txt"), 2)
        );
        assertTrue(ex.getMessage().contains("Expected 16"));
    }

    @Test
    void negativeValue() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> CardGame.loadPack(packPath("packs/negative_n2.txt"), 2)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("negative"));
    }

    @Test
    void nonInteger() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> CardGame.loadPack(packPath("packs/non_integer_n2.txt"), 2)
        );
        assertTrue(ex.getMessage().contains("non-negative integer"));
    }

    @Test
    void missingFile() {
        Path missing = tempDir.resolve("does_not_exist.txt");
        assertThrows(IOException.class, () -> CardGame.loadPack(missing.toString(), 2));
    }

    @Test
    void zeroAllowed() throws IOException {
        // Based on spec, 0 should be fine
        Path packFile = tempDir.resolve("zeros.txt");
        Files.writeString(packFile, "0\n0\n0\n0\n0\n0\n0\n0\n");
        Card[] pack = CardGame.loadPack(packFile.toString(), 1);
        assertEquals(0, pack[0].getValue());
    }

    private static String packPath(String name) throws Exception {
        return java.nio.file.Paths.get(
                Objects.requireNonNull(PackLoaderTest.class.getClassLoader().getResource(name)).toURI()
        ).toString();
    }
}

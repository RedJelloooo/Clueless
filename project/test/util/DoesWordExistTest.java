package util;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Brandon
 */
class DoesWordExistTest {

    private static ArrayList<String> words = new ArrayList<>();


    @BeforeAll
    static void setup() {
        words = WordFile.readWordFile();
    }

    @Test
    void testValidWords() {
        assertTrue(DoesWordExist.execute(words, "all"));
        assertTrue(DoesWordExist.execute(words, "adeem"));
        assertTrue(DoesWordExist.execute(words, "deave"));
        assertTrue(DoesWordExist.execute(words, "eaved"));
        assertTrue(DoesWordExist.execute(words, "edema"));
        assertTrue(DoesWordExist.execute(words, "embed"));
        assertTrue(DoesWordExist.execute(words, "evade"));
        assertTrue(DoesWordExist.execute(words, "famed"));
        assertTrue(DoesWordExist.execute(words, "femme"));
        assertTrue(DoesWordExist.execute(words, "mead"));
        assertTrue(DoesWordExist.execute(words, "abed"));
        assertTrue(DoesWordExist.execute(words, "bade"));
        assertTrue(DoesWordExist.execute(words, "beam"));
        assertTrue(DoesWordExist.execute(words, "beef"));
        assertTrue(DoesWordExist.execute(words, "bema"));
        assertTrue(DoesWordExist.execute(words, "deaf"));
        assertTrue(DoesWordExist.execute(words, "made"));
        assertTrue(DoesWordExist.execute(words, "cade"));
    }

    @Test
    void testInvalidWords() {
        assertFalse(DoesWordExist.execute(words, "allasdasd"));
        assertFalse(DoesWordExist.execute(words, "AiusaghfbkjLL"));
        assertFalse(DoesWordExist.execute(words, "AiusaghfbkjLL"));
        assertFalse(DoesWordExist.execute(words, "Ai234jLL"));
        assertFalse(DoesWordExist.execute(words, "Aiusa?.ad"));
        assertFalse(DoesWordExist.execute(words, "23v-aLL"));
        assertFalse(DoesWordExist.execute(words, "Aiuskao"));
    }
}
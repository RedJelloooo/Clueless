package util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Brandon
 */
class ScoreTest {

    private static final char[] letters = {'A', 'P', 'G', 'E'}; // length doesn't really matter here

    @Test
    void testAllLettersUsed() {
        int expectedResult = (1 + 7 + 1 + 8 + 1) * 2;
        int actualResult = Score.calculate("page", letters);
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void testNotAWord() {
        int expectedResult = 0;
        int actualResult = Score.calculate("notaword123", letters);
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void testValidWord1() {
        int expectedResult = 1 + 1 + 8 + 1;
        int actualResult = Score.calculate("age", letters);
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void testValidWord2() {
        int expectedResult = 1 + 1 + 8;
        int actualResult = Score.calculate("ag", letters);
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void testWordWithDuplicateLetters() {
        int expectedResult = 0;
        int actualResult = Score.calculate("agape", letters);
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void testOneLetterWordToDeductPoint() {
        int expectedResult = -1;
        int actualResult = Score.calculate("a", letters);
        assertEquals(expectedResult, actualResult);
    }
}
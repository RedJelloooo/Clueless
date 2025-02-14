package util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Brandon
 */
class RandomLettersTest {

    @Test
    void testLetterCount1() {
        int expectedResult = 10;
        int actualResult = RandomLetters.letterGetter(10).length();
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void testLetterCount2() {
        int expectedResult = 7;
        int actualResult = RandomLetters.letterGetter(7).length();
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void testLetterCount3() {
        int expectedResult = 15;
        int actualResult = RandomLetters.letterGetter(15).length();
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void testLetterCount4() {
        int expectedResult = 5;
        int actualResult = RandomLetters.letterGetter(5).length();
        assertEquals(expectedResult, actualResult);
    }
}
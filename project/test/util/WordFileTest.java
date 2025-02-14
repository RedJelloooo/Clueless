package util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Brandon
 */
class WordFileTest {

    @Test
    void wordCount() {
        int expectedResult = 370105;
        int actualResult = WordFile.readWordFile().size();
        assertEquals(expectedResult, actualResult);
    }
}
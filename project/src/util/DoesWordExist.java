package util;

import java.util.ArrayList;

/**
 * @author Brandon
 * This class is used ot determine if a word exists in the words_alpha file
 */
public class DoesWordExist {

    /**
     * This will test to see if a given word is valid
     * @param words list of valid words
     * @param word word to check if its valid
     * @return boolean if the word is valid or not
     */
    public static boolean execute(ArrayList<String> words, String word) {

        // This is a binary search method that will only work with a sorted list, with runtime of O(log n).
        // the Java contains method has run time of O(n), but we would need to sort the words_alpha.txt since its only
        // mostly sorted

        int lowerBound = 0;
        int upperBound = words.size() - 1;

        while (lowerBound <= upperBound) {
            int middle = lowerBound + (upperBound - lowerBound) / 2;
            int comparison = word.compareToIgnoreCase(words.get(middle));

            if (comparison == 0) {
                return true;
            }

            if (comparison > 0) {
                lowerBound = middle + 1;
            } else {
                upperBound = middle - 1;
            }
        }
        return false;
    }
}

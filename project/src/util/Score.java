package util;

import java.util.ArrayList;

/**
 * @author Brandon
 * This score class is our way of determining how many points a word is worth
 */
public class Score {

    private static final ArrayList<String> words = WordFile.readWordFile();

    /**
     * calculates how many points a word is worth. A correct word will earn one point. There is a penalty for entering
     * a single letter. If the word is not found or if it not actually a word 0 will be returned.
     * Scoring is based off of the pointsPerLetter function
     * @param word - word to be scored
     * @param letters - legal given letters.txt
     * @return - score of the word
     */
    public static int calculate(String word, char[] letters) {
        word = word.toUpperCase();

        // if you enter a single character you lose a point no matter what :)
        if (word.length() == 1) {
            return -1;
        }

        if (!words.contains(word.toLowerCase())) {
            System.out.println("Not a word");
            return 0; //if it is not a valid word
        }

        if (!onlyValidLetters(word.split(""), letters)) {
            return 0;
        }

        // a correct word will default to at least one point
        int score = 1;

        // each letter will have a specified amount of points
        String[] letter = word.split("");
        for (String l : letter) {
            score += pointsPerLetter(l);
        }

        // if the length of the word is equal to the amount of letters.txt, double the points
        if (word.length() == letters.length) {
            score = 2*score;
        }

        return score;
    }

    /**
     * this checks if the input word uses valid letters.txt given by the game
     * @param word - given word turned into a string
     * @param letters - legal letters.txt that can be used in the word
     * @return - true or false on legality of word
     */
    private static boolean onlyValidLetters(String[] word, char[] letters) {
        char[] lettersCopy = letters.clone();
        for (String s : word) {
            boolean hasLetter = false;
            for (int i = 0; i< lettersCopy.length; i++) {
                if (s.equals(String.valueOf(lettersCopy[i]))) {
                    lettersCopy[i] = '-';
                    hasLetter = true;
                    break;
                }
            }
            if (!hasLetter) return false; // if at any point there is an illegal letter its false
        }

        return true; //if all letters.txt pass true is returned
    }

    /**
     * This is a method that uses a switch statement to determine how many points each letter is worth
     * @param letter - input letter to score
     * @return - score of that letter
     */
    private static int pointsPerLetter(String letter) {
        return switch (letter) {
            case "E", "A" -> 1;
            case "R", "I", "O" -> 2;
            case "T", "N" -> 3;
            case "S", "L", "C" -> 5;
            case "U", "D", "P", "M", "H" -> 7;
            case "G", "B" -> 8;
            case "F", "Y", "W", "K", "V" -> 9;
            case "X", "Z", "J", "Q" -> 11;
            default -> 0;
        };
    }
}

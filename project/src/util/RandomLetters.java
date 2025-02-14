package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * @author Rogelio
 * this function creates an array of random characters to use
 * there are always at least 3 vowels created with the remaining amount
 * of letters.txt being the available consonants
 */
public class RandomLetters {
    private final static char[] vowels = {'A', 'E', 'I', 'O', 'U'}; // legal vowels
    private final static char[] consonants = { 'B', 'C', 'D', // legal consonants
            'F', 'G', 'H', 'J',
            'K', 'L', 'M', 'N', 'P',
            'Q', 'R', 'S', 'T',
            'V', 'W', 'X', 'Y', 'Z'};

    /**
     * This creates a character array of the legal letters.txt that can be used in the word game
     * 3 consonants are randomly decided and the rest of the characters are consonants
     *
     * @param amount - the total amount of characters to be used
     * @return - character array of legal characters
     */
    public static String letterGetter(int amount) {
        Random rand = new Random();
        char[] returnLetters = new char[amount]; // creates array with length of amount

        //gets three vowels
        for(int i = 0; i < 3; i++){
            char vowel = vowels[rand.nextInt(5)];
            returnLetters[i] = vowel;
        }
        //the rest of the characters are consonants
        for(int j = 3; j < amount; j++) {
            returnLetters[j] = consonants[rand.nextInt(consonants.length)];
        }

        return String.valueOf(returnLetters);
    }

    /**
     * This takes in a character array of letters.txt and shuffles it
     * @param letters - input character array
     * @return - arrayList of shuffled letters.txt
     */
    public static ArrayList<Character> letterShuffler(char[] letters) {

        ArrayList<Character> shuffled = new ArrayList<>(); // creates arraylist to return
        for(char x: letters){
            shuffled.add(x); // adds each element to the new list
        }

        Collections.shuffle(shuffled); // shuffles the letters.txt

        return  shuffled;
    }
}

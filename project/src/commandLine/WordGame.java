package commandLine;

import util.RandomLetters;
import util.Score;

import java.util.Scanner;

/**
 * Command Line word game
 * This is the simplest version of our game
 */
public class WordGame {

    /**
     * prints out the letters.txt on the command line
     * @param letters - given char array to display
     */
    private static void printLetters(char[] letters) {
        StringBuilder display = new StringBuilder();
        for (char i : letters) {
            display.append(i).append(" ");
        }
        System.out.println(display);
    }

    public static void main(String[] args) {
        System.out.println("--- Command Line Implementation ---");
        System.out.println(" 1. Play Single Player\n 2. Exit");

        Scanner input = new Scanner(System.in);
        String selection = input.next();

        if (selection.equals("2")) {
            return;
        }

        int points = 0;

        System.out.println("Commands:\n /shuffle - new puzzle\n /leave - exit");
        String letters = RandomLetters.letterGetter(10);
        printLetters(letters.toCharArray());
        while (!selection.equals("/leave")) {
            selection = input.next();
            if (selection.equals("/shuffle")) {
                System.out.println("Your score was: " + points);
                points = 0;
                letters = RandomLetters.letterGetter(10);
                System.out.println("Commands:\n /shuffle - new puzzle\n /leave - exit");
                printLetters(letters.toCharArray());
            } else {
                int pointsForWord = Score.calculate(selection, letters.toCharArray());
                points += pointsForWord;
                System.out.println(pointsForWord + " points.");
            }
        }

        System.out.println("Your score was: " + points);
    }
}

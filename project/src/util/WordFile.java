package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author Brandon
 * reads the text file with legal words and adds them into an arraylist to be sorted
 * also reads in a given file of scrambles and turns them into an ArrayList
 */
public class WordFile {

    /**
     * Reads the Word file and returns an ArrayList of the given legal words
     * throws a not found exception if a Word file is not found
     * @return - Arraylist of legal words
     */
    public static ArrayList<String> readWordFile() {
        ArrayList<String> list = new ArrayList<>(); // list to return
        try {
            File keyFile = new File("words_alpha.txt");
            Scanner keyReader = new Scanner(keyFile); // file reader and scanner to read the inputs

            while (keyReader.hasNext()) {
                list.add(keyReader.nextLine()); // while there are indecision left the reader goes on
            }

            keyReader.close();

        } catch (FileNotFoundException e) { // if the file is not found an error is thrown
            System.out.println("File not found: words_alpha.txt");
            e.printStackTrace();
        }

        return list;
    }

    /**
     * this method reads a given file of scrambles and outputs an arraylist of
     * those scrambles
     * @param wordFile - input file of scrambles
     * @return - arrayList of scrambles
     */
    public static String[] readLetterFile(File wordFile) {
        ArrayList<String> list = new ArrayList<>(); // list to return
        String[] retList = new String[5];
        try {
            File keyFile = new File(wordFile.getPath());
            Scanner keyReader = new Scanner(keyFile); // file reader and scanner to read the inputs

            while (keyReader.hasNext()) {
                list.add(keyReader.nextLine()); // while there are indecision left the reader goes on
            }

            retList = new String[list.size()];

            for(int x = 0; x < list.size(); x++){
                retList[x] = list.get(x);
            }

            keyReader.close();

        } catch (FileNotFoundException e) { // if the file is not found an error is thrown
            System.out.println("File not found: " + wordFile.getPath());
            for(int x = 0; x < 5; x++){
                retList[x] = RandomLetters.letterGetter(7);
            }
        }

        return retList;
    }
}

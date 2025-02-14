package util;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;

// The main bulk of this file and algorithms came from:
// https://javaconceptoftheday.com/how-to-sort-a-text-file-in-java/#:~:text=How%20To%20Sort%20A%20Text%20File%20Having%20Single,lines%20using%20Collections.sort%20%28%29%20method.%20...%20More%20items

/**
 * This class is used to create information to be displayed on the leaderboard
 */
public class TournamentScoreboard {

    /**
     * this inner class sets the player's information
     */
    private class PlayerInformation {
        String name;
        int score;
        int round;

        public PlayerInformation(String name, int score, int round) {
            this.name = name;
            this.score = score;
            this.round = round;
        }
    }

    /**
     * compares scores and determines which player should be higher up
     */
    private static class scoreCompare implements Comparator<PlayerInformation> {
        @Override
        public synchronized int compare(PlayerInformation playerOne, PlayerInformation playerTwo) {
            return playerTwo.score - playerOne.score;
        }
    }

    /**
     * Creates the string to be displayed on the leaderboard and sorts it
     * @param endName - name of the player
     * @param endScore - score for the round
     * @param endRound - round number
     * @return - string that is separated by new lines
     * @throws IOException - if writing to the file fails
     */
    public synchronized String SortTextFile(String endName, int endScore, int endRound) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("tournament.txt"));

        ArrayList<PlayerInformation> scoreboardRecord = new ArrayList<>();
        scoreboardRecord.add(new PlayerInformation(endName, endScore, endRound));
        String currentLine = reader.readLine();

        while (currentLine != null) {
            String[] studentDetail = currentLine.split(" ");
            String name = studentDetail[0];

            int score = Integer.parseInt(studentDetail[1]);
            int round = Integer.parseInt(studentDetail[2]);

            scoreboardRecord.add(new PlayerInformation(name, score, round));
            currentLine = reader.readLine();
        }

        scoreboardRecord.sort(new scoreCompare());

        //Creating BufferedWriter object to write into output text file
        BufferedWriter writer = new BufferedWriter(new FileWriter("tournament.txt"));

        //Writing every studentRecords into output text file
        StringBuilder tournamentString = new StringBuilder();
        for (PlayerInformation player : scoreboardRecord) {
            tournamentString.append(player.name) //lines 56 - 61 was code we developed, it was not copied
                    .append(" - score: ")
                    .append(player.score)
                    .append(" ; rounds completed: ")
                    .append(player.round)
                    .append("\n");

            writer.write(player.name);
            writer.write(" " + player.score);
            writer.write(" " + player.round);

            writer.newLine();
        }

        //Closing the resources
        reader.close();
        writer.close();

        return tournamentString.toString();
    }
}
